package work.utakatanet.utazonplugin.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import work.utakatanet.utazonplugin.UtazonPlugin;
import work.utakatanet.utazonplugin.data.DatabaseItem;
import work.utakatanet.utazonplugin.data.ProductItem;

import java.util.HashMap;
import java.util.Map;

public class ItemStackHelper {

    private static final UtazonPlugin plugin = UtazonPlugin.plugin;
    private static final Gson gson = UtazonPlugin.gson;

    public static DatabaseItem encodeItemStack(ItemStack itemStack) {
        if (itemStack != null) {
            Map<String, Integer> itemEnchantments = new HashMap<>();

            String itemEnchantmentsJson;
            for (Enchantment enchantment : itemStack.getEnchantments().keySet()) {
                int enchantmentLevel = itemStack.getEnchantments().get(enchantment);
                String enchantmentKey = enchantment.getKey().getKey();
                itemEnchantments.put(enchantmentKey, enchantmentLevel);
            }
            itemEnchantmentsJson = gson.toJson(itemEnchantments, new TypeToken<Map<Enchantment, Integer>>(){}.getType());

            // アイテム名取得
            String itemDisplayName = itemStack.getItemMeta().getDisplayName();
            int itemAmount = itemStack.getAmount();
            String itemMaterial = itemStack.getType().getKey().getKey();

            return new DatabaseItem(itemDisplayName, itemMaterial, itemEnchantmentsJson, itemAmount);
        } else {
            return null;
        }

    }

    public static ItemStack decodeItemStack(ProductItem productItem) {

        String itemDisplayName = productItem.itemDisplayName;
        String itemMaterialString = productItem.itemMaterial;
        String itemEnchantmentsString = productItem.itemEnchantments;
        int amount = productItem.amount;


        // マテリアル設定
        NamespacedKey itemMaterialKey = NamespacedKey.minecraft(itemMaterialString);
        Material itemMaterial = Material.matchMaterial(itemMaterialKey.getKey());
        if (itemMaterial == null) {
            plugin.getLogger().warning("マテリアルが見つかりませんでした");
            return null;
        }

        // エンチャント取得
        Map<String, Integer> itemEnchantmentsJson = gson.fromJson(itemEnchantmentsString, new TypeToken<Map<String, Integer>>(){}.getType());
        Map<Enchantment, Integer> itemEnchantments = new HashMap<>();
        for (String enchantmentString : itemEnchantmentsJson.keySet()) {
            NamespacedKey itemEnchantmentKey = NamespacedKey.minecraft(enchantmentString);
            Enchantment itemEnchantment = Enchantment.getByKey(itemEnchantmentKey);
            int itemEnchantmentLv = itemEnchantmentsJson.get(enchantmentString);

            if (itemEnchantment == null) {
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

    public static ItemStack decodeItemStack(DatabaseItem waitingStock) {

        String itemDisplayName = waitingStock.itemDisplayName;
        String itemMaterialString = waitingStock.itemMaterial;
        String itemEnchantmentsString = waitingStock.itemEnchantments;
        int amount = waitingStock.amount;


        // マテリアル設定
        NamespacedKey itemMaterialKey = NamespacedKey.minecraft(itemMaterialString);
        Material itemMaterial = Material.matchMaterial(itemMaterialKey.getKey());
        if (itemMaterial == null) {
            plugin.getLogger().warning("マテリアルが見つかりませんでした");
            return null;
        }

        // エンチャント取得
        Map<String, Integer> itemEnchantmentsJson = gson.fromJson(itemEnchantmentsString, new TypeToken<Map<String, Integer>>(){}.getType());
        Map<Enchantment, Integer> itemEnchantments = new HashMap<>();
        for (String enchantmentString : itemEnchantmentsJson.keySet()) {
            NamespacedKey itemEnchantmentKey = NamespacedKey.minecraft(enchantmentString);
            Enchantment itemEnchantment = Enchantment.getByKey(itemEnchantmentKey);
            int itemEnchantmentLv = itemEnchantmentsJson.get(enchantmentString);

            if (itemEnchantment == null) {
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
}
