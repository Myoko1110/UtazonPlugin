package work.utakatanet.utazonplugin.listener;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class tabComplete implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
        List<String> completions = new ArrayList<>();
        if (args.length == 1){
            if (sender.hasPermission("utazon.stockgui")){
                completions.add("stockgui");
            }
            if (sender.hasPermission("utazon.socket")){
                completions.add("socket");
            }
            if (sender.hasPermission("utazon.reload")){
                completions.add("reload");
            }

        }else if (args.length == 2){
            if (args[0].equalsIgnoreCase("socket") && sender.hasPermission("utazon.socket")){
                completions.add("start");
                completions.add("stop");
                completions.add("restart");
            }
        }
        return completions;
    }
}
