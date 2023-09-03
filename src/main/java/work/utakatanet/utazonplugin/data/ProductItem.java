package work.utakatanet.utazonplugin.data;

public class ProductItem {

    public int itemID;
    public String itemDisplayName;
    public String itemMaterial;
    public String itemEnchantments;
    public int amount;
    public int stock;

    public ProductItem(int itemID, String itemDisplayName, String itemMaterial, String itemEnchantments, int amount, int stock){
        this.itemID = itemID;
        this.itemDisplayName = itemDisplayName;
        this.itemMaterial = itemMaterial;
        this.itemEnchantments = itemEnchantments;
        this.amount = amount;
        this.stock = stock;
    }
}
