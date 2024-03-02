package work.utakatanet.utazonplugin;

import com.github.kuripasanda.economyutilsapi.EconomyUtilsAPI;
import com.github.kuripasanda.economyutilsapi.api.EconomyUtilsApi;
import com.google.gson.Gson;
import org.bukkit.plugin.java.JavaPlugin;
import work.utakatanet.utazonplugin.listener.BlockPlaceListener;
import work.utakatanet.utazonplugin.listener.InventoryCloseListener;
import work.utakatanet.utazonplugin.listener.CommandListener;
import work.utakatanet.utazonplugin.task.DetectOrder;
import work.utakatanet.utazonplugin.task.DetectReturnStock;
import work.utakatanet.utazonplugin.util.DatabaseHelper;
import work.utakatanet.utazonplugin.util.SocketServer;


public final class UtazonPlugin extends JavaPlugin {

    public static EconomyUtilsApi ecoApi = null;
    public static final Gson gson = new Gson();
    public static UtazonPlugin plugin;
    public static SocketServer socketServer;

    public static String webHost;
    public static String webPass;
    public static String webEmbedHost;

    @Override
    public void onEnable() {
        // Plugin
        plugin = this;
        getLogger().info("UtazonPlugin が有効になりました");

        // Settings
        saveDefaultConfig();
        webHost = getConfig().getString("web.host");
        webPass = getConfig().getString("web.pass");
        webEmbedHost = getConfig().getString("web.embed");

        // Initialize
        ecoApi = EconomyUtilsAPI.Companion.getApi();
        socketServer = new SocketServer();
        socketServer.start();
        new DatabaseHelper().init();

        // Command
        getCommand("utazon").setExecutor(new CommandListener());

        // Event
        getServer().getPluginManager().registerEvents(new InventoryCloseListener(), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);

        // Task
        new DetectOrder().runTaskTimer(this, 0, 20*60);
        new DetectReturnStock().runTaskTimer(this, 0, 20*60);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        if (socketServer != null) {
            socketServer.stopServer();
        }
    }
}
