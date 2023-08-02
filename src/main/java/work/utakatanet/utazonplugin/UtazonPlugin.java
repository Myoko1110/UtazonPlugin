package work.utakatanet.utazonplugin;

//import com.github.kuripasanda.economyutilsapi.api.EconomyUtilsApi;
import org.bukkit.plugin.java.JavaPlugin;
import work.utakatanet.utazonplugin.utils.SocketServer;

public final class UtazonPlugin extends JavaPlugin {

    // public static final EconomyUtilsApi ecoApi = EconomyUtilsAPI.api;
    private static UtazonPlugin plugin;
    private SocketServer socketServer;

    @Override
    public void onEnable() {
        plugin = this;

        getLogger().info("UtazonPluginが有効になりました。");
        saveDefaultConfig();

        socketServer = new SocketServer();
        socketServer.start();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        if (socketServer != null) {
            socketServer.stopServer();
        }
    }

    public static UtazonPlugin getPlugin() {
        return plugin;
    }
}
