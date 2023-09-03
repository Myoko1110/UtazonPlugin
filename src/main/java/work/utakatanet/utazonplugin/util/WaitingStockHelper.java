package work.utakatanet.utazonplugin.util;

import com.google.common.reflect.TypeToken;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import work.utakatanet.utazonplugin.UtazonPlugin;
import work.utakatanet.utazonplugin.data.WaitingStock;

import java.util.*;

import static org.bukkit.Bukkit.createInventory;
import static org.bukkit.Bukkit.removeRecipe;
import static work.utakatanet.utazonplugin.UtazonPlugin.gson;

public class WaitingStockHelper {

    private static final UtazonPlugin plugin = UtazonPlugin.plugin;

    public static Inventory createGUI(Player player){
        Inventory inv = createInventory(null,54,"WaitingStock");

        ItemStack[] waitingStock = get(player);
        if (waitingStock != null){
            for (int i = 0; i < waitingStock.length; i++){
                inv.setItem(i, waitingStock[i]);
            }
        }

        return inv;
        // https://qiita.com/yt0f1/items/a11fe0e2ac068d005309
        // https://www.spigotmc.org/wiki/creating-a-gui-inventory/
    }

    public static ItemStack[] get(Player player){
        UUID uuid = player.getUniqueId();

        String waitingStockJson = DatabaseHelper.getWaitingStock(uuid);
        ArrayList<Map<String, Object>> waitingStocks = gson.fromJson(waitingStockJson, new TypeToken<ArrayList<Map<String, Object>>>(){}.getType());

        if (waitingStocks != null){

            ArrayList<ItemStack> ItemStacksArrayList = new ArrayList<>();
            for (Map<String, Object> waitingStockInfo : waitingStocks){
                if (waitingStockInfo != null){
                    String itemDisplayName = (String) waitingStockInfo.get("item_display_name");
                    String itemMaterialString = (String) waitingStockInfo.get("item_material");
                    String itemEnchantmentsJson = (String) waitingStockInfo.get("item_enchantments");
                    int itemAmount = (int) Math.round((double) waitingStockInfo.get("amount"));

                    // マテリアル設定
                    NamespacedKey itemMaterialKey = NamespacedKey.minecraft(itemMaterialString);
                    Material itemMaterial = Material.matchMaterial(itemMaterialKey.getKey());
                    if (itemMaterial == null){
                        plugin.getLogger().warning("マテリアルが見つかりませんでした");
                        return null;
                    }

                    // エンチャント取得
                    Map<String, Integer> itemEnchantmentsString = gson.fromJson(itemEnchantmentsJson, new TypeToken<Map<String, Integer>>(){}.getType());
                    Map<Enchantment, Integer> itemEnchantments = new HashMap<>();
                    for (String enchantmentString : itemEnchantmentsString.keySet()){
                        NamespacedKey itemEnchantmentKey = NamespacedKey.minecraft(enchantmentString);
                        Enchantment itemEnchantment = Enchantment.getByKey(itemEnchantmentKey);

                        int itemEnchantmentLv = itemEnchantmentsString.get(enchantmentString);

                        if (itemEnchantment == null){
                            plugin.getLogger().warning("エンチャントが見つかりませんでした");
                            return null;
                        }
                        itemEnchantments.put(itemEnchantment, itemEnchantmentLv);
                    }

                    // アイテムにエンチャントを付与
                    ItemStack itemStack = new ItemStack(itemMaterial, itemAmount);
                    itemStack.addUnsafeEnchantments(itemEnchantments);

                    // アイテムに名前をつける
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    if (itemMeta != null && itemDisplayName != null && !itemDisplayName.isEmpty()) {
                        itemMeta.setDisplayName(itemDisplayName);
                    }
                    itemStack.setItemMeta(itemMeta);

                    // ArrayListに追加
                    ItemStacksArrayList.add(itemStack);

                }else{
                    ItemStacksArrayList.add(null);
                }


            }

            ItemStack[] itemStacks = new ItemStack[ItemStacksArrayList.size()];
            itemStacks = ItemStacksArrayList.toArray(itemStacks);

            return itemStacks;

        }else{
            return null;
        }
    }

    public static boolean post(Player player, ItemStack[] itemStacks){
        UUID uuid = player.getUniqueId();

        ArrayList<Map<String, Object>> itemInfoList = new ArrayList<>();
        for (ItemStack itemStack : itemStacks){
            WaitingStock waitingStock = ItemStackHelper.getItemStackInfo(itemStack);

            if (waitingStock != null) {
                String itemDisplayName = waitingStock.itemDisplayName;
                String itemMaterial = waitingStock.itemMaterial;
                String itemEnchantmentsJson = waitingStock.itemEnchantments;
                int amount = waitingStock.amount;

                Map<String, Object> itemInfo = new HashMap<>();
                itemInfo.put("item_display_name", itemDisplayName);
                itemInfo.put("item_material", itemMaterial);
                itemInfo.put("item_enchantments", itemEnchantmentsJson);
                itemInfo.put("amount", amount);

                itemInfoList.add(itemInfo);
            }else{
                itemInfoList.add(null);
            }
        }

        String waitingStockJson = gson.toJson(itemInfoList, new TypeToken<ArrayList<Map<String, Object>>>(){}.getType());

        return DatabaseHelper.addWaitingStock(uuid, waitingStockJson);
    }
}
