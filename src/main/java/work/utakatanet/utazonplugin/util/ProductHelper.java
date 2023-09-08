package work.utakatanet.utazonplugin.util;

import org.bukkit.inventory.ItemStack;
import work.utakatanet.utazonplugin.data.ProductItem;
import work.utakatanet.utazonplugin.data.DatabaseItem;

public class ProductHelper {

    public static ItemStack getProductStack(int itemID) {
        ProductItem itemMaterialInfo = DatabaseHelper.getItemStack(itemID);
        return ItemStackHelper.decodeItemStack(itemMaterialInfo);
    }

    public static boolean addProductStack(int itemID, ItemStack[] itemStacks){
        if (!isAllSame(itemStacks)){
            return false;
        }
        // 量を取得
        int itemStock = itemStacks.length;

        ItemStack itemStack = itemStacks[0];
        DatabaseItem itemStackInfo = ItemStackHelper.encodeItemStack(itemStack);

        String itemDisplayName = itemStackInfo.itemDisplayName;
        String itemMaterial = itemStackInfo.itemMaterial;
        String itemEnchantmentsJson = itemStackInfo.itemEnchantments;
        int itemAmount = itemStackInfo.amount;

        ProductItem MaterialInfo = new ProductItem(itemID, itemDisplayName, itemMaterial, itemEnchantmentsJson, itemAmount, itemStock);
        return DatabaseHelper.addItemStack(MaterialInfo);
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
