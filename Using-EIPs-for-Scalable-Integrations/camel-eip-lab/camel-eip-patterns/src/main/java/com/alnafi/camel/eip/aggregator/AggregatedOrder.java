package com.alnafi.camel.eip.aggregator;

import com.alnafi.camel.eip.splitter.OrderItem;
import java.util.List;

public class AggregatedOrder {
    private String orderId;
    private List<OrderItem> items;
    private double totalAmount;
    private int itemCount;
    private long aggregationStartTime;
    private long aggregationEndTime;

    public AggregatedOrder() {
        this.aggregationStartTime = System.currentTimeMillis();
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }

    public long getAggregationStartTime() { return aggregationStartTime; }
    public void setAggregationStartTime(long aggregationStartTime) {
        this.aggregationStartTime = aggregationStartTime;
    }

    public long getAggregationEndTime() { return aggregationEndTime; }
    public void setAggregationEndTime(long aggregationEndTime) {
        this.aggregationEndTime = aggregationEndTime;
    }

    public long getAggregationDuration() {
        return aggregationEndTime - aggregationStartTime;
    }

    @Override
    public String toString() {
        return "AggregatedOrder{" +
                "orderId='" + orderId + '\'' +
                ", itemCount=" + itemCount +
                ", totalAmount=" + totalAmount +
                ", aggregationDuration=" + getAggregationDuration() + "ms" +
                '}';
    }
}
