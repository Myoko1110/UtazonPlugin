package work.utakatanet.utazonplugin.data;

public class WaitingStock {

    public String itemDisplayName;
    public String itemMaterial;
    public String itemEnchantments;
    public int amount;

    public WaitingStock(String itemDisplayName, String itemMaterial, String itemEnchantments, int amount){
        this.itemDisplayName = itemDisplayName;
        this.itemMaterial = itemMaterial;
        this.itemEnchantments = itemEnchantments;
        this.amount = amount;
    }
}
