package work.utakatanet.utazonplugin.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import work.utakatanet.utazonplugin.data.ItemMaterial;
import work.utakatanet.utazonplugin.UtazonPlugin;
import work.utakatanet.utazonplugin.data.WaitingStock;

import java.util.HashMap;
import java.util.Map;

public class ItemStackHelper {

    private static final UtazonPlugin plugin = UtazonPlugin.plugin;
    private static final Gson gson = UtazonPlugin.gson;

    public static ItemStack getItemStack(int itemID) {
        ItemMaterial itemMaterialInfo = DatabaseHelper.getMaterial(itemID);
        if (itemMaterialInfo == null){
            return null;
        }

        String itemDisplayName = itemMaterialInfo.itemDisplayName;
        String itemMaterialString = itemMaterialInfo.itemMaterial;
        String itemEnchantmentsString = itemMaterialInfo.itemEnchantments;
        int amount = itemMaterialInfo.amount;


        // マテリアル設定
        NamespacedKey itemMaterialKey = NamespacedKey.minecraft(itemMaterialString);
        Material itemMaterial = Material.matchMaterial(itemMaterialKey.getKey());
        if (itemMaterial == null){
            plugin.getLogger().warning("マテリアルが見つかりませんでした");
            return null;
        }

        // エンチャント取得
        Map<String, Integer> itemEnchantmentsJson = gson.fromJson(itemEnchantmentsString, new TypeToken<Map<String, Integer>>(){}.getType());
        Map<Enchantment, Integer> itemEnchantments = new HashMap<>();
        for (String enchantmentString : itemEnchantmentsJson.keySet()){
            NamespacedKey itemEnchantmentKey = NamespacedKey.minecraft(enchantmentString);
            Enchantment itemEnchantment = Enchantment.getByKey(itemEnchantmentKey);
            int itemEnchantmentLv = itemEnchantmentsJson.get(enchantmentString);

            if (itemEnchantment == null){
                plugin.getLogger().warning("エンチャントが見つかりませんでした");
                return null;
            }
            itemEnchantments.put(itemEnchantment, itemEnchantmentLv);
        }

        // アイテムにエンチャントを付与
        ItemStack itemStack = new ItemStack(itemMaterial, amount);
        itemStack.addEnchantments(itemEnchantments);

        // アイテムに名前をつける
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null && !itemDisplayName.isEmpty()) {
            itemMeta.setDisplayName(itemDisplayName);
        }
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public static boolean addItemStack(int itemID, ItemStack[] itemStacks){
        if (!isAllSame(itemStacks)){
            return false;
        }
        // 量を取得
        int itemStock = itemStacks.length;

        // エンチャントを取得しMapに保存
        ItemStack itemStack = itemStacks[0];
        WaitingStock itemStackInfo = getItemStackInfo(itemStack);

        String itemDisplayName = itemStackInfo.itemDisplayName;
        String itemMaterial = itemStackInfo.itemMaterial;
        String itemEnchantmentsJson = itemStackInfo.itemEnchantments;
        int itemAmount = itemStackInfo.amount;

        ItemMaterial MaterialInfo = new ItemMaterial(itemID, itemDisplayName, itemMaterial, itemEnchantmentsJson, itemAmount, itemStock);
        return DatabaseHelper.addMaterial(MaterialInfo);
    }

    public static WaitingStock getItemStackInfo(ItemStack itemStack){
        if (itemStack != null){
            Map<String, Integer> itemEnchantments = new HashMap<>();

            String itemEnchantmentsJson;
            for (Enchantment enchantment : itemStack.getEnchantments().keySet()){
                int enchantmentLevel = itemStack.getEnchantments().get(enchantment);
                String enchantmentKey = enchantment.getKey().getKey();
                itemEnchantments.put(enchantmentKey, enchantmentLevel);
            }
            itemEnchantmentsJson = gson.toJson(itemEnchantments, new TypeToken<Map<Enchantment, Integer>>(){}.getType());

            // アイテム名取得
            String itemDisplayName = itemStack.getItemMeta().getDisplayName();
            int itemAmount = itemStack.getAmount();
            String itemMaterial = itemStack.getType().getKey().getKey();

            return new WaitingStock(itemDisplayName, itemMaterial, itemEnchantmentsJson, itemAmount);
        }else{
            return null;
        }

    }

    public static boolean isAllSame(ItemStack[] list) {
        if (list.length == 0) {
            return false;
        }

        ItemStack first = list[0];
        for (int i = 1; i < list.length; i++) {
            if (list[i] != first) {
                return false;
            }
        }

        return true;
    }
}
