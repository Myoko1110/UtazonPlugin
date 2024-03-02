package work.utakatanet.utazonplugin.listener;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import work.utakatanet.utazonplugin.util.WaitingStockHelper;

public class InventoryCloseListener implements Listener {

    // GUIを閉じた時
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Inventory inv = e.getInventory();

        if (inv.getHolder() instanceof Block || !e.getView().getTitle().equals("待機ストック")) {
            return;
        }

        Player player = (Player) e.getPlayer();
        ItemStack[] itemStacks = inv.getContents();

        WaitingStockHelper.post(player, itemStacks);
    }
}
