package work.utakatanet.utazonplugin;

import com.github.kuripasanda.economyutilsapi.EconomyUtilsAPI;
import com.github.kuripasanda.economyutilsapi.api.EconomyUtilsApi;
import com.google.gson.Gson;
import org.bukkit.plugin.java.JavaPlugin;
import work.utakatanet.utazonplugin.listener.EventListener;
import work.utakatanet.utazonplugin.listener.onCommand;
import work.utakatanet.utazonplugin.post.detectOrder;
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
        plugin = this;
        ecoApi = EconomyUtilsAPI.Companion.getApi();

        getLogger().info("UtazonPlugin が有効になりました");
        saveDefaultConfig();

        webHost = getConfig().getString("web.host");
        webPass = getConfig().getString("web.pass");
        webEmbedHost = getConfig().getString("web.embed");

        socketServer = new SocketServer();
        socketServer.start();

        getCommand("utazon").setExecutor(new onCommand());
        getServer().getPluginManager().registerEvents(new EventListener(), this);

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
