package work.utakatanet.utazonplugin;

import com.github.kuripasanda.economyutilsapi.api.EconomyUtilsApi;
import com.github.kuripasanda.economyutilsapi.api.impl.EconomyUtilsApiImpl;
import org.bukkit.plugin.java.JavaPlugin;
import work.utakatanet.utazonplugin.utils.SocketServer;

import java.util.UUID;

public final class UtazonPlugin extends JavaPlugin {

    public static EconomyUtilsApi ecoApi;
    private static UtazonPlugin plugin;
    private SocketServer socketServer;

    @Override
    public void onEnable() {
        plugin = this;

        getLogger().info("UtazonPluginが有効になりました。");
        saveDefaultConfig();

        socketServer = new SocketServer();
        socketServer.start();

        ecoApi = new EconomyUtilsApiImpl();

        double balance = ecoApi.getBalance(UUID.fromString("305d2e94-608f-4198-8381-5dc7bcf70f27"));
        getLogger().info(String.valueOf(balance));

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
    public static EconomyUtilsApi getEcoApi(){
        return ecoApi;
    }
}
