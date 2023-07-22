package work.utakatanet.utazonplugin;

import com.github.kuripasanda.economyutilsapi.api.EconomyUtilsApi;
import org.bukkit.plugin.java.JavaPlugin;

public final class UtazonPlugin extends JavaPlugin {

    public static final EconomyUtilsApi ecoApi = EconomyUtilsAPI.api;

    @Override
    public void onEnable() {
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
