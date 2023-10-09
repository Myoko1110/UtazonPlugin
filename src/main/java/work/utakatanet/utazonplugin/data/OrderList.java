package work.utakatanet.utazonplugin.data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class OrderList {

    public UUID uuid;
    public Map<String, Integer> orderItem;
    public LocalDateTime deliversAt;
    public LocalDateTime ordereAt;
    public String orderID;
    public double amount;
    public int usedPoint;
    public String error;

    public OrderList(UUID uuid, Map<String, Integer> orderItem, LocalDateTime deliveryAt, LocalDateTime orderTime, String orderID, double amount, int usedPoint, String error) {
        this.uuid = uuid;
        this.orderItem = orderItem;
        this.deliversAt = deliveryAt;
        this.ordereAt = orderTime;
        this.orderID = orderID;
        this.amount = amount;
        this.usedPoint = usedPoint;
        this.error = error;
    }

}
