package work.utakatanet.utazonplugin.data;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReturnStockList {

    public int id;
    public UUID uuid;
    public int itemID;
    public int amount;
    public LocalDateTime deliveryAt;
    public boolean status;
    public String error;

    public ReturnStockList(int id, UUID uuid, int itemID, int amount, LocalDateTime deliveryAt, boolean status, String error){
        this.id = id;
        this.uuid = uuid;
        this.itemID = itemID;
        this.amount = amount;
        this.deliveryAt = deliveryAt;
        this.status = status;
        this.error = error;
    }
}
