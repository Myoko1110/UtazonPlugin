package work.utakatanet.utazonplugin.util;

import org.bukkit.inventory.ItemStack;
import work.utakatanet.utazonplugin.data.ProductItem;

public class ProductHelper {

    public static ItemStack getProductStack(int itemID) {
        ProductItem itemMaterialInfo = DatabaseHelper.getItemStack(itemID);

        if (itemMaterialInfo != null) {
            return ItemStackHelper.decodeItemStack(itemMaterialInfo);
        } else {
            return null;
        }
    }

}
