package work.utakatanet.utazonplugin.util;

import com.google.gson.Gson;
import org.bukkit.configuration.file.FileConfiguration;
import work.utakatanet.utazonplugin.UtazonPlugin;
import work.utakatanet.utazonplugin.data.ProductItem;
import work.utakatanet.utazonplugin.data.OrderList;

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
            pstmt = cnx.prepareStatement("SELECT * FROM utazon_order WHERE status=true and canceled=false");

            rs = pstmt.executeQuery();
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("mc_uuid"));
                int[][] orderItem = gson.fromJson(rs.getString("order_item"), int[][].class);
                LocalDateTime deliveryTime = LocalDateTime.parse(rs.getString("delivery_time"), formatter);
                LocalDateTime orderTime = LocalDateTime.parse(rs.getString("order_time"), formatter);
                String orderID = rs.getString("order_id");
                double amount = rs.getDouble("amount");
                int usedPoint = rs.getInt("used_point");
                String error = rs.getString("error");

                OrderList orderListChild = new OrderList(uuid, orderItem, deliveryTime, orderTime, orderID, amount, usedPoint, error);
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
                    int amount = rs.getInt("stack_size");
                    int itemStock = rs.getInt("stock");

                    return new ProductItem(itemID, itemName, itemMaterialString, itemEnchantmentsJson, amount, itemStock);
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

            String sql = "INSERT INTO utazon_waitingstock (mc_uuid, value, updated_date) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE value=VALUES(value), updated_date=VALUES(updated_date)";
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
                    String value = rs.getString("value");
                    return value;

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

    public static ArrayList<String> geItemInfo(int itemID) {
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

                    ArrayList<String> infoList = new ArrayList<>();
                    infoList.add(itemName);
                    infoList.add(itemPrice);

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
            pstmt = cnx.prepareStatement("CREATE TABLE IF NOT EXISTS `utazon_order` (mc_uuid VARCHAR(36), order_item JSON, delivery_time DATETIME, order_time DATETIME, order_id VARCHAR(18) UNIQUE, error VARCHAR(64))");
            pstmt.executeUpdate();

            pstmt = cnx.prepareStatement("CREATE TABLE IF NOT EXISTS `utazon_itemstack` (item_id BIGINT UNIQUE, item_display_name VARCHAR(64), item_material VARCHAR(64), item_enchantments JSON, stack_size INT, stock BIGINT)");
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}



