package work.utakatanet.utazonplugin.data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class OrderList {

    public UUID uuid;
    public Map<String, Integer> orderItem;
    public LocalDateTime orderedAt;
    public LocalDateTime shipsAt;
    public LocalDateTime deliversAt;
    public String orderID;
    public double amount;
    public int usedPoint;
    public String error;
    public boolean dmSent;

    public OrderList(UUID uuid, Map<String, Integer> orderItem, LocalDateTime orderTime, LocalDateTime shipAt, LocalDateTime deliveryAt, String orderID, double amount, int usedPoint, String error, boolean dmSent) {
        this.uuid = uuid;
        this.orderItem = orderItem;
        this.orderedAt = orderTime;
        this.shipsAt = shipAt;
        this.deliversAt = deliveryAt;
        this.orderID = orderID;
        this.amount = amount;
        this.usedPoint = usedPoint;
        this.error = error;
        this.dmSent = dmSent;
    }

}
