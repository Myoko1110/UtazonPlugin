package work.utakatanet.utazonplugin.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.configuration.file.FileConfiguration;
import work.utakatanet.utazonplugin.UtazonPlugin;
import work.utakatanet.utazonplugin.data.ProductItem;
import work.utakatanet.utazonplugin.data.OrderList;
import work.utakatanet.utazonplugin.data.ReturnStockList;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDateTime;


public class DatabaseHelper {

    private static final UtazonPlugin plugin = UtazonPlugin.plugin;
    private static final Gson gson = UtazonPlugin.gson;

    private static String host;
    private static String port;
    private static String db;
    private static String args;
    private static String user;
    private static String pass;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void init(){
        loadSettings();
        createTable();
    }


    public static ArrayList<OrderList> getOrder() {
        Connection cnx = null;
        PreparedStatement pstmt;
        ResultSet rs = null;

        ArrayList<OrderList> orderList = new ArrayList<>();
        try {
            cnx = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s", host, port, db),
                    user,
                    pass
            );
            pstmt = cnx.prepareStatement("SELECT * FROM utazon_order WHERE status=TRUE AND canceled=FALSE");

            rs = pstmt.executeQuery();
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("mc_uuid"));
                Map<String, Integer> orderItem = gson.fromJson(rs.getString("order_item"), new TypeToken<Map<String, Integer>>(){}.getType());
                LocalDateTime orderTime = LocalDateTime.parse(rs.getString("ordered_at"), formatter);
                LocalDateTime shipTime = LocalDateTime.parse(rs.getString("ships_at"), formatter);
                LocalDateTime deliveryTime = LocalDateTime.parse(rs.getString("delivers_at"), formatter);
                String orderID = rs.getString("order_id");
                double amount = rs.getDouble("amount");
                int usedPoint = rs.getInt("used_point");
                String error = rs.getString("error");
                boolean dmSent = rs.getBoolean("dm_sent");

                OrderList orderListChild = new OrderList(uuid, orderItem, orderTime, shipTime, deliveryTime, orderID, amount, usedPoint, error, dmSent);
                orderList.add(orderListChild);
            }

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (cnx != null) {
                try {
                    cnx.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return orderList;

    }

    public static void completeOrder(String orderID) {
        try {
            Connection cnx = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s", host, port, db),
                    user,
                    pass
            );

            String sql = "UPDATE utazon_order SET status=?, error=null WHERE order_id=?";
            try (PreparedStatement pstmt = cnx.prepareStatement(sql)){
                pstmt.setBoolean(1, false);
                pstmt.setString(2, orderID);

                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void completeDMSent(String orderID) {
        try {
            Connection cnx = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s", host, port, db),
                    user,
                    pass
            );

            String sql = "UPDATE utazon_order SET dm_sent=TRUE WHERE order_id=?";
            try (PreparedStatement pstmt = cnx.prepareStatement(sql)){
                pstmt.setString(1, orderID);

                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void errorOrder(String orderID, String error) {
        try {
            Connection cnx = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s", host, port, db),
                    user,
                    pass
            );

            String sql = "UPDATE utazon_order SET error=? WHERE order_id=?";
            try (PreparedStatement pstmt = cnx.prepareStatement(sql)){
                pstmt.setString(1, error);
                pstmt.setString(2, orderID);

                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ProductItem getItemStack(int itemID) {
        try {
            Connection cnx = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s", host, port, db),
                    user,
                    pass
            );

            String sql = "SELECT * FROM utazon_itemstack WHERE item_id=?";
            try(PreparedStatement pstmt = cnx.prepareStatement(sql)){
                pstmt.setInt(1, itemID);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    String itemName = rs.getString("item_display_name");
                    String itemMaterialString = rs.getString("item_material");
                    String itemEnchantmentsJson = rs.getString("item_enchantments");
                    int itemDamage = rs.getInt("item_damage");
                    int amount = rs.getInt("stack_size");
                    int itemStock = rs.getInt("stock");

                    return new ProductItem(itemID, itemName, itemMaterialString, itemEnchantmentsJson, itemDamage, amount, itemStock);
                }else{
                    return null;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean addItemStack(ProductItem itemMaterial){
        try {
            Connection cnx = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s", host, port, db),
                    user,
                    pass
            );

            String sql = "INSERT INTO utazon_itemstack (item_id, item_display_name, item_material, item_enchantments, stack_size, stock) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = cnx.prepareStatement(sql)){
                pstmt.setInt(1, itemMaterial.itemID);
                pstmt.setString(2, itemMaterial.itemDisplayName);
                pstmt.setString(3, itemMaterial.itemMaterial);
                pstmt.setString(4, itemMaterial.itemEnchantments);
                pstmt.setInt(5, itemMaterial.amount);
                pstmt.setInt(6, itemMaterial.stock);

                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;

            } catch (SQLException e){
                e.printStackTrace();
            }

        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public static boolean addWaitingStock(UUID uuid, String json){
        try {
            Connection cnx = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s", host, port, db),
                    user,
                    pass
            );

            String sql = "INSERT INTO utazon_waitingstock (mc_uuid, value, updated_at) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE value=VALUES(value), updated_at=VALUES(updated_at)";
            try (PreparedStatement pstmt = cnx.prepareStatement(sql)){
                LocalDateTime now = LocalDateTime.now();

                pstmt.setString(1, String.valueOf(uuid));
                pstmt.setString(2, json);
                pstmt.setTimestamp(3, Timestamp.valueOf(now));

                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;

            } catch (SQLException e){
                e.printStackTrace();
            }

        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public static String getWaitingStock(UUID uuid) {
        try {
            Connection cnx = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s", host, port, db),
                    user,
                    pass
            );

            String sql = "SELECT value FROM utazon_waitingstock WHERE mc_uuid=?";
            try(PreparedStatement pstmt = cnx.prepareStatement(sql)){
                pstmt.setString(1, String.valueOf(uuid));
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    return rs.getString("value");

                }else{
                    return null;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<String> getItemInfo(int itemID) {
        try {
            Connection cnx = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s", host, port, db),
                    user,
                    pass
            );

            String sql = "SELECT * FROM utazon_item WHERE item_id=?";
            try(PreparedStatement pstmt = cnx.prepareStatement(sql)){
                pstmt.setInt(1, itemID);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    String itemName = rs.getString("item_name");
                    String itemPrice = rs.getString("price");
                    String uuid = rs.getString("mc_uuid");

                    ArrayList<String> infoList = new ArrayList<>();
                    infoList.add(itemName);
                    infoList.add(itemPrice);
                    infoList.add(uuid);

                    return infoList;
                }else{
                    return null;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<ReturnStockList> getReturnStock() {
        Connection cnx = null;
        PreparedStatement pstmt;
        ResultSet rs = null;

        ArrayList<ReturnStockList> orderList = new ArrayList<>();
        try {
            cnx = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s", host, port, db),
                    user,
                    pass
            );
            pstmt = cnx.prepareStatement("SELECT * FROM utazon_returnstock WHERE status=true");

            rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                UUID uuid = UUID.fromString(rs.getString("mc_uuid"));
                int itemID = rs.getInt("item_id");
                int amount = rs.getInt("amount");
                LocalDateTime deliveryAt = LocalDateTime.parse(rs.getString("delivers_at"), formatter);
                boolean status = rs.getBoolean("status");
                String error = rs.getString("error");

                ReturnStockList orderListChild = new ReturnStockList(id, uuid, itemID, amount, deliveryAt, status, error);
                orderList.add(orderListChild);
            }

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (cnx != null) {
                try {
                    cnx.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return orderList;

    }

    public static void completeReturnStock(int id) {
        try {
            Connection cnx = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s", host, port, db),
                    user,
                    pass
            );

            String sql = "UPDATE utazon_returnstock SET status=?, error=null WHERE id=?";
            try (PreparedStatement pstmt = cnx.prepareStatement(sql)){
                pstmt.setBoolean(1, false);
                pstmt.setInt(2, id);

                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void errorReturnStock(int id, String error) {
        LocalDateTime hourago = LocalDateTime.now().plusHours(1);

        try {
            Connection cnx = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s", host, port, db),
                    user,
                    pass
            );

            String sql = "UPDATE utazon_returnstock SET error=?, deliverys_at=? WHERE id=?";
            try (PreparedStatement pstmt = cnx.prepareStatement(sql)){
                pstmt.setString(1, error);
                pstmt.setTimestamp(2, Timestamp.valueOf(hourago));
                pstmt.setInt(3, id);

                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void loadSettings() {
        FileConfiguration section = plugin.getConfig();
        host = section.getString("database.host");
        port = section.getString("database.port");
        db = section.getString("database.db");
        args = section.getString("database.args");
        user = section.getString("database.user");
        pass = section.getString("database.pass");
    }


    public void createTable() {
        Connection cnx;
        PreparedStatement pstmt;
        try {
            cnx = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s", host, port, db),
                    user,
                    pass
            );
            pstmt = cnx.prepareStatement("CREATE TABLE IF NOT EXISTS `utazon_order` (order_id VARCHAR(18) UNIQUE, mc_uuid VARCHAR(36), order_item JSON, ordered_at DATETIME, ships_at DATETIME, delivers_at DATETIME, amount DOUBLE, used_point INT, canceled BOOLEAN, error VARCHAR(36), status BOOLEAN)");
            pstmt.executeUpdate();

            pstmt = cnx.prepareStatement("CREATE TABLE IF NOT EXISTS `utazon_itemstack` (item_id BIGINT UNIQUE, item_display_name VARCHAR(64), item_material VARCHAR(64), item_enchantments JSON, item_damage INT, stack_size INT, stock BIGINT)");
            pstmt.executeUpdate();

            pstmt = cnx.prepareStatement("CREATE TABLE IF NOT EXISTS `utazon_item` (sale_id INT AUTO_INCREMENT UNIQUE, item_id BIGINT UNIQUE, item_name VARCHAR(256), price DOUBLE, image JSON, kind JSON, category VARCHAR(64), purchases_number BIGINT, mc_uuid VARCHAR(36), search_keyword JSON, created_at DATETIME, updated_at DATETIME, status BOOLEAN, FULLTEXT (item_name) WITH PARSER ngram) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci");
            pstmt.executeUpdate();

            pstmt = cnx.prepareStatement("CREATE TABLE IF NOT EXISTS `utazon_waitingstock` (mc_uuid VARCHAR(36), value JSON, updated_at DATETIME)");
            pstmt.executeUpdate();

            pstmt = cnx.prepareStatement("CREATE TABLE IF NOT EXISTS `utazon_returnstock` (mc_uuid VARCHAR(36), item_id BIGINT, amount INT, created_at DATETIME, delivers_at DATETIME, status BOOLEAN, error VARCHAR(64))");
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}



