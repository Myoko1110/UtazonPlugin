package work.utakatanet.utazonplugin.util;

import org.bukkit.configuration.file.FileConfiguration;
import work.utakatanet.utazonplugin.UtazonPlugin;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDateTime;


public class DBHelper {

    private static final UtazonPlugin utazonPlugin = UtazonPlugin.plugin;

    private String host;
    private String port;
    private String db;
    private String args;
    private String user;
    private String pass;

    public DBHelper(){
        loadDBSettings();
        createTable();
    }


    public ArrayList<Map<String, Object>> GetOrder() {
        loadDBSettings();
        createTable();

        Connection cnx = null;
        PreparedStatement pstmt;
        ResultSet rs = null;

        ArrayList<Map<String, Object>> orderList = new ArrayList<>();
        try {
            cnx = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s", host, port, db),
                    user,
                    pass
            );
            pstmt = cnx.prepareStatement("SELECT * FROM utazon_order");

            rs = pstmt.executeQuery();
            while (rs.next()) {
                UUID mc_uuid = UUID.fromString(rs.getString("mc_uuid"));
                String order_item = rs.getString("order_item");

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime delivery_time = LocalDateTime.parse(rs.getString("delivery_time"), formatter);
                LocalDateTime order_time = LocalDateTime.parse(rs.getString("order_time"), formatter);
                String order_id = rs.getString("order_id");

                Map<String, Object> array = new HashMap<>();
                array.put("mc_uuid", mc_uuid);
                array.put("order_item", order_item);
                array.put("delivery_time", delivery_time);
                array.put("order_time", order_time);
                array.put("order_id", order_id);

                orderList.add(array);
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


    public void loadDBSettings() {
        FileConfiguration section = utazonPlugin.getConfig();
        this.host = section.getString("database.host");
        this.port = section.getString("database.port");
        this.db = section.getString("database.db");
        this.args = section.getString("database.args");
        this.user = section.getString("database.user");
        this.pass = section.getString("database.pass");
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
            pstmt = cnx.prepareStatement("CREATE TABLE IF NOT EXISTS `utazon_order` (mc_uuid VARCHAR(36), order_item JSON, delivery_time DATETIME, order_time DATETIME, order_id VARCHAR(18) UNIQUE)");
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();

        }
    }
}

