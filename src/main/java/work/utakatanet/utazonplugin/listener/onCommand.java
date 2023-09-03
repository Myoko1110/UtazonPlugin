package work.utakatanet.utazonplugin.listener;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import work.utakatanet.utazonplugin.UtazonPlugin;
import work.utakatanet.utazonplugin.util.DatabaseHelper;
import work.utakatanet.utazonplugin.util.SocketServer;
import work.utakatanet.utazonplugin.util.WaitingStockHelper;

import java.util.List;

public class onCommand implements CommandExecutor, TabCompleter {

    private static final UtazonPlugin plugin = UtazonPlugin.plugin;
    private static final SocketServer socketServer = UtazonPlugin.socketServer;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (command.getName().equalsIgnoreCase("utazon")) {
            if (args.length == 0){
                sender.sendMessage(ChatColor.RED + "[UtazonPlugin] 引数を入力してください。");
                return true;
            }
            switch (args[0]) {
                case "socket" -> {
                    return socket(sender, command, label, args);
                }
                case "reload" -> {
                    return reload(sender, command, label, args);
                }
                case "stockgui" -> {
                    return stockGUI(sender, command, label, args);
                }
            }
        }
        return false;
    }

    public boolean startSocket(){
        if (!socketServer.isRunning) {
            socketServer.start();
            return true;
        }else{
            return false;
        }
    }
    public boolean stopSocket(){
        if (socketServer.isRunning) {
            socketServer.stopServer();
            return true;
        }else{
            return false;
        }
    }

    public boolean socket(CommandSender sender, Command command, String label, String[] args){
        if (!sender.hasPermission("utazon.socket")){
            sender.sendMessage(ChatColor.RED + command.getPermissionMessage());
            return true;
        }
        if (args.length != 2){
            sender.sendMessage(ChatColor.RED + "[UtazonPlugin] 引数が不足しています: /utazon socket <mode>");
            return true;
        }
        switch (args[1]) {
            case "start" -> {
                if (startSocket()) {
                    sender.sendMessage("[UtazonPlugin] Socketサーバーを起動しました");
                } else {
                    sender.sendMessage("[UtazonPlugin] Socketサーバーはすでに起動しています");
                }
            }
            case "stop" -> {
                if (stopSocket()) {
                    sender.sendMessage("[UtazonPlugin] Socketサーバーを停止しました");
                } else {
                    sender.sendMessage("[UtazonPlugin] Socketサーバーはすでに停止しています");
                }
            }
            case "restart" -> {
                stopSocket();
                startSocket();
                sender.sendMessage("[UtazonPlugin] Socketサーバーを再起動しました");
                return true;
            }
            default -> {
                sender.sendMessage(ChatColor.RED + "[UtazonPlugin] 引数が不明です: /utazon socket <mode>");
                return true;
            }
        }
        return false;
    }

    public boolean reload(CommandSender sender, Command command, String label, String[] args){
        if (!sender.hasPermission("utazon.reload")){
            sender.sendMessage(ChatColor.RED + command.getPermissionMessage());
            return true;
        }

        plugin.reloadConfig();
        new DatabaseHelper().loadSettings();
        new DatabaseHelper().createTable();
        new SocketServer().loadSettings();

        stopSocket();
        startSocket();

        sender.sendMessage("[UtazonPlugin] リロードが完了しました");
        return true;
    }

    public boolean stockGUI(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args){
        if (!sender.hasPermission("utazon.stockgui")){
            sender.sendMessage(ChatColor.RED + command.getPermissionMessage());
        }
        if (sender instanceof Player player){
            Inventory inv = WaitingStockHelper.createGUI(player);
            player.openInventory(inv);
        }
        else{
            sender.sendMessage(ChatColor.RED + "[UtazonPlugin] このコマンドはコンソールから使用できません");
        }
        return true;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new tabComplete().onTabComplete(sender,command,alias,args);
    }
}
