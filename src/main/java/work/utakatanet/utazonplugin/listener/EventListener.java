package work.utakatanet.utazonplugin.listener;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import work.utakatanet.utazonplugin.util.WaitingStockHelper;

public class EventListener implements Listener {

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

    // シュルカーボックスを置いた時
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Block b = e.getBlock();
        Block bp = e.getBlockPlaced();
        Player p = e.getPlayer();

        ItemMeta bm = p.getInventory().getItemInMainHand().getItemMeta();
        if (bm == null) return;

        if (bm.getDisplayName().equals(ChatColor.AQUA + "Utazonからのお届け物") && bm.getLore() != null && bm.getLore().get(0).startsWith("注文番号: ")
            || bm.getDisplayName().startsWith(ChatColor.AQUA + "Utazonからの在庫返却") || bm.getDisplayName().startsWith(ChatColor.DARK_PURPLE + "Utazonからの在庫返却"))
        {
            if (bp.getType() == Material.BROWN_SHULKER_BOX) {
                ShulkerBox shulkerBox = (ShulkerBox) bp.getState();
                Inventory shulkerInventory = shulkerBox.getInventory();

                ItemStack[] contents = shulkerInventory.getContents();
                boolean hasItems = false;
                for (ItemStack item : contents) {
                    if (item != null && item.getType() != Material.AIR) {
                        hasItems = true;
                        break;
                    }
                }

                if (hasItems) {
                    for (ItemStack item : contents) {
                        if (item == null) continue;
                        b.getWorld().dropItemNaturally(b.getLocation(), item);
                    }
                    bp.setType(Material.AIR);
                    p.spawnParticle(Particle.BLOCK_CRACK, b.getLocation(), 50, 0.5, 0.5, 0.5, 0.1, Material.BROWN_SHULKER_BOX.createBlockData());
                }
            }
        }
    }
}
