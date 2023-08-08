package work.utakatanet.utazonplugin.post;

import org.bukkit.scheduler.BukkitRunnable;
import work.utakatanet.utazonplugin.UtazonPlugin;

public class detectOrder extends BukkitRunnable {

    private static final UtazonPlugin utazonPlugin = UtazonPlugin.plugin;

    @Override
    public void run(){
        utazonPlugin.getLogger().info("1分ごとの処理が実行されました。");
    }
}
