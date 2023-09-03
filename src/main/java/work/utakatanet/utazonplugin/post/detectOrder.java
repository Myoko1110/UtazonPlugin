package work.utakatanet.utazonplugin.post;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import work.utakatanet.utazonplugin.UtazonPlugin;
import work.utakatanet.utazonplugin.util.DatabaseHelper;
import work.utakatanet.utazonplugin.data.OrderList;
import work.utakatanet.utazonplugin.util.ItemStackHelper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class detectOrder extends BukkitRunnable {

    private static final UtazonPlugin plugin = UtazonPlugin.plugin;

    @Override
    public void run(){
        ArrayList<OrderList> order = DatabaseHelper.getOrder();

        for (OrderList i : order) {

            LocalDateTime deliveryTime = i.deliveryTime;
            LocalDateTime now = LocalDateTime.now();

            if (deliveryTime.isBefore(now)) {

                UUID uuid = i.uuid;
                int[][] orderItem = i.orderItem;
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

                World world = Bukkit.getWorld("world");
                if (world == null) {
                    plugin.getLogger().warning("ワールドが見つかりませんでした");
                    return;
                }

                Location chestLocation = new Location(world, 229, 67, -339);
                Block block = chestLocation.getBlock();
                if (block.getType() != Material.CHEST){
                    plugin.getLogger().warning(player.getName() + "のポストが見つかりませんでした");
                    return;
                }

                ArrayList<ItemStack> itemList = new ArrayList<>();
                for (int[] item : orderItem){
                    int itemID = item[0];
                    int itemQty = item[1];

                    // アイテム取得処理
                    ItemStack itemStack = ItemStackHelper.getItemStack(itemID);
                    for (int j = 0; j < itemQty; j++){
                        itemList.add(itemStack);
                    }
                }
                postItem(chestLocation, itemList);
                DatabaseHelper.completeOrder(i.orderID);
            }
        }
    }

    public void postItem(Location location, ArrayList<ItemStack> itemStack){
        Chest chest = (Chest) location.getBlock().getState();
        Inventory chestInventory = chest.getInventory();

        try{
            for (ItemStack i : itemStack){
                chestInventory.addItem(i);
            }
            chest.update();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
