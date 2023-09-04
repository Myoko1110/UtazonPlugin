package work.utakatanet.utazonplugin.data;

public class DatabaseItem {

    public String itemDisplayName;
    public String itemMaterial;
    public String itemEnchantments;
    public int amount;

    public DatabaseItem(String itemDisplayName, String itemMaterial, String itemEnchantments, int amount){
        this.itemDisplayName = itemDisplayName;
        this.itemMaterial = itemMaterial;
        this.itemEnchantments = itemEnchantments;
        this.amount = amount;
    }
}
