package work.utakatanet.utazonplugin.data;

import java.time.LocalDateTime;
import java.util.UUID;

public class OrderList {

    public UUID uuid;
    public int[][] orderItem;
    public LocalDateTime deliveryTime;
    public LocalDateTime orderTime;
    public String orderID;
    public double amount;
    public int usedPoint;

    public OrderList(UUID uuid, int[][] orderItem, LocalDateTime deliveryTime, LocalDateTime orderTime, String orderID, double amount, int usedPoint) {
        this.uuid = uuid;
        this.orderItem = orderItem;
        this.deliveryTime = deliveryTime;
        this.orderTime = orderTime;
        this.orderID = orderID;
        this.amount = amount;
        this.usedPoint = usedPoint;

    }

}
