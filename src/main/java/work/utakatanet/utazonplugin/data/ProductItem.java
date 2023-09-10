package work.utakatanet.utazonplugin.data;

public class ProductItem {

    public int itemID;
    public String itemDisplayName;
    public String itemMaterial;
    public String itemEnchantments;
    public int itemDamage;
    public int amount;
    public int stock;

    public ProductItem(int itemID, String itemDisplayName, String itemMaterial, String itemEnchantments, int itemDamage, int amount, int stock){
        this.itemID = itemID;
        this.itemDisplayName = itemDisplayName;
        this.itemMaterial = itemMaterial;
        this.itemEnchantments = itemEnchantments;
        this.itemDamage = itemDamage;
        this.amount = amount;
        this.stock = stock;
    }
}
