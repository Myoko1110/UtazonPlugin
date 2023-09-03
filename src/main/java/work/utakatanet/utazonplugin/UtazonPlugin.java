package work.utakatanet.utazonplugin;

import com.github.kuripasanda.economyutilsapi.EconomyUtilsAPI;
import com.github.kuripasanda.economyutilsapi.api.EconomyUtilsApi;
import com.google.gson.Gson;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;
import work.utakatanet.utazonplugin.listener.WaitingStockClose;
import work.utakatanet.utazonplugin.listener.onCommand;
import work.utakatanet.utazonplugin.post.detectOrder;
import work.utakatanet.utazonplugin.util.*;

import java.util.List;


public final class UtazonPlugin extends JavaPlugin {

    public static EconomyUtilsApi ecoApi = null;
    public static final Gson gson = new Gson();
    public static UtazonPlugin plugin;
    public static SocketServer socketServer;

    @Override
    public void onEnable() {
        plugin = this;
        ecoApi = EconomyUtilsAPI.Companion.getApi();

        getLogger().info("UtazonPlugin が有効になりました");
        saveDefaultConfig();

        socketServer = new SocketServer();
        socketServer.start();

        getCommand("utazon").setExecutor(new onCommand());
        getServer().getPluginManager().registerEvents(new WaitingStockClose(), this);

        new DatabaseHelper().init();

        detectOrder scheduler = new detectOrder();
        scheduler.runTaskTimer(this, 0, 20*60);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        if (socketServer != null) {
            socketServer.stopServer();
        }
    }
}
