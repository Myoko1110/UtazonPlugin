package work.utakatanet.utazonplugin.listener;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import work.utakatanet.utazonplugin.UtazonPlugin;
import work.utakatanet.utazonplugin.util.WaitingStockHelper;

import java.util.Arrays;

public class WaitingStockClose implements Listener {

    private static final UtazonPlugin plugin = UtazonPlugin.plugin;

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        Inventory inv = event.getInventory();

        if (inv.getHolder() instanceof Block || !event.getView().getTitle().equals("WaitingStock")){
            return;
        }

        Player player = (Player) event.getPlayer();
        ItemStack[] itemStacks = inv.getContents();

        WaitingStockHelper.post(player, itemStacks);
    }

}
