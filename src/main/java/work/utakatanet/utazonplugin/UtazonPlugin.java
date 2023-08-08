package work.utakatanet.utazonplugin;

import com.github.kuripasanda.economyutilsapi.EconomyUtilsAPI;
import com.github.kuripasanda.economyutilsapi.api.EconomyUtilsApi;
import com.google.gson.Gson;
import org.bukkit.plugin.java.JavaPlugin;
import work.utakatanet.utazonplugin.command.onCommand;
import work.utakatanet.utazonplugin.post.detectOrder;
import work.utakatanet.utazonplugin.util.*;

import java.util.ArrayList;
import java.util.Map;


public final class UtazonPlugin extends JavaPlugin {

    public static EconomyUtilsApi ecoApi = null;
    public static final Gson gson = new Gson();
    public static UtazonPlugin plugin;
    public SocketServer socketServer;

    @Override
    public void onEnable() {
        plugin = this;
        ecoApi = EconomyUtilsAPI.Companion.getApi();

        getLogger().info("UtazonPlugin が有効になりました");
        saveDefaultConfig();

        socketServer = new SocketServer();
        socketServer.start();

        getCommand("utazonplugin").setExecutor(new onCommand());

        DBHelper dbHelper = new DBHelper();
        ArrayList<Map<String, Object>> order = dbHelper.GetOrder();
        getLogger().info(order.get(0).get("order_id").toString());

        detectOrder scheduler = new detectOrder();
        scheduler.runTaskTimer(this, 20, 20);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        if (socketServer != null) {
            socketServer.stopServer();
        }
    }
}
