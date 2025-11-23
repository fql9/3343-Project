package model;

public class Transaction {
    private Long id;
    private Long itemId;
    private Long buyerId;
    private Long sellerId;
    private double agreedPrice;        // 协商后的价格
    private String status;              // PENDING, NEGOTIATING, CONFIRMED, SHIPPED, DELIVERED, COMPLETED, CANCELLED
    private String deliveryMethod;      // LOCAL_PICKUP, SHIP
    private String shippingAddress;     // 邮寄地址（如果选择邮寄）
    private String trackingNumber;      // 物流单号
    private boolean itemReceived;       // 买家是否已收到商品
    private boolean itemVerified;       // 买家是否已验证商品状况
    private boolean fundsReleased;      // 资金是否已释放
    private String createdTime;
    private String updatedTime;

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public double getAgreedPrice() { return agreedPrice; }
    public void setAgreedPrice(double agreedPrice) { this.agreedPrice = agreedPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(String deliveryMethod) { this.deliveryMethod = deliveryMethod; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public boolean isItemReceived() { return itemReceived; }
    public void setItemReceived(boolean itemReceived) { this.itemReceived = itemReceived; }

    public boolean isItemVerified() { return itemVerified; }
    public void setItemVerified(boolean itemVerified) { this.itemVerified = itemVerified; }

    public boolean isFundsReleased() { return fundsReleased; }
    public void setFundsReleased(boolean fundsReleased) { this.fundsReleased = fundsReleased; }

    public String getCreatedTime() { return createdTime; }
    public void setCreatedTime(String createdTime) { this.createdTime = createdTime; }

    public String getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(String updatedTime) { this.updatedTime = updatedTime; }
}

