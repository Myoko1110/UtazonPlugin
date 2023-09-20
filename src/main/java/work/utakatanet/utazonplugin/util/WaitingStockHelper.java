package work.utakatanet.utazonplugin.util;

import com.google.common.reflect.TypeToken;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import work.utakatanet.utazonplugin.data.DatabaseItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.Bukkit.createInventory;
import static work.utakatanet.utazonplugin.UtazonPlugin.gson;

public class WaitingStockHelper {

    public static Inventory createGUI(@NotNull Player player) {
        Inventory inv = createInventory(null, 54, "待機ストック");

        ItemStack[] waitingStock = get(player.getUniqueId());
        if (waitingStock != null) {
            for (int i = 0; i < waitingStock.length; i++) {
                inv.setItem(i, waitingStock[i]);
            }
        }

        return inv;
        // https://qiita.com/yt0f1/items/a11fe0e2ac068d005309
        // https://www.spigotmc.org/wiki/creating-a-gui-inventory/
    }

    public static ItemStack[] get(@NotNull UUID uuid) {
        String waitingStockJson = DatabaseHelper.getWaitingStock(uuid);
        ArrayList<Map<String, Object>> waitingStocks = gson.fromJson(waitingStockJson, new TypeToken<ArrayList<Map<String, Object>>>() {
        }.getType());

        if (waitingStocks != null) {

            ArrayList<ItemStack> ItemStacksArrayList = new ArrayList<>();
            for (Map<String, Object> waitingStockInfo : waitingStocks) {
                if (waitingStockInfo != null) {
                    String itemDisplayName = (String) waitingStockInfo.get("item_display_name");
                    String itemMaterialString = (String) waitingStockInfo.get("item_material");
                    String itemEnchantmentsJson = (String) waitingStockInfo.get("item_enchantments");
                    int itemDamage = (int) (double) waitingStockInfo.get("item_damage");
                    int itemAmount = (int) (double) waitingStockInfo.get("amount");

                    DatabaseItem waitingStock = new DatabaseItem(itemDisplayName, itemMaterialString, itemEnchantmentsJson, itemDamage, itemAmount);
                    ItemStack itemStack = ItemStackHelper.decodeItemStack(waitingStock);

                    ItemStacksArrayList.add(itemStack);

                } else {
                    ItemStacksArrayList.add(null);
                }


            }

            ItemStack[] itemStacks = new ItemStack[ItemStacksArrayList.size()];
            itemStacks = ItemStacksArrayList.toArray(itemStacks);

            return itemStacks;

        } else {
            return null;
        }
    }

    public static boolean post(@NotNull Player player, @NotNull ItemStack[] itemStacks) {
        UUID uuid = player.getUniqueId();

        ArrayList<Map<String, Object>> itemInfoList = new ArrayList<>();
        for (ItemStack itemStack : itemStacks) {
            DatabaseItem waitingStock = ItemStackHelper.encodeItemStack(itemStack);

            if (waitingStock != null) {
                Map<String, Object> itemInfo = new HashMap<>();
                itemInfo.put("item_display_name", waitingStock.itemDisplayName);
                itemInfo.put("item_material", waitingStock.itemMaterial);
                itemInfo.put("item_enchantments", waitingStock.itemEnchantments);
                itemInfo.put("item_damage", waitingStock.itemDamage);
                itemInfo.put("amount", waitingStock.amount);

                itemInfoList.add(itemInfo);
            } else {
                itemInfoList.add(null);
            }
        }

        String waitingStockJson = gson.toJson(itemInfoList, new TypeToken<ArrayList<Map<String, Object>>>() {
        }.getType());

        return DatabaseHelper.addWaitingStock(uuid, waitingStockJson);
    }
}
