package work.utakatanet.utazonplugin.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import work.utakatanet.utazonplugin.UtazonPlugin;
import work.utakatanet.utazonplugin.util.DBHelper;
import work.utakatanet.utazonplugin.util.SocketServer;

import java.util.List;

public class onCommand implements CommandExecutor, TabCompleter {

    private static final UtazonPlugin utazonPlugin = UtazonPlugin.plugin;
    private static final SocketServer socketServer = utazonPlugin.socketServer;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, @NotNull String[] args) {

        if (command.getName().equalsIgnoreCase("utazonplugin")) {
            if (args.length == 0){
                sender.sendMessage(ChatColor.RED + "[UtazonPlugin] 引数を入力してください。");
                return true;
            }
            if (args[0].equals("socket")){
                return socket(sender, command, label, args);
            }else if (args[0].equals("reload")){
                return reload(sender, command, label, args);
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
        if(!sender.hasPermission("utazonplugin.socket")){
            sender.sendMessage(ChatColor.RED + command.getPermissionMessage());
            return true;
        }
        if(args.length != 2){
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
        if(!sender.hasPermission("utazonplugin.reload")){
            sender.sendMessage(ChatColor.RED + command.getPermissionMessage());
            return true;
        }

        utazonPlugin.reloadConfig();
        new DBHelper().loadDBSettings();
        new DBHelper().createTable();
        new SocketServer().loadSocketSettings();
        sender.sendMessage("[UtazonPlugin] リロードが完了しました");
        return true;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new tabComplete().onTabComplete(sender,command,alias,args);
    }
}
