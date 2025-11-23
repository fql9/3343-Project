package model;

public enum TransactionStatus {
    PENDING,        // 待处理
    NEGOTIATING,    // 协商中
    CONFIRMED,      // 已确认
    SHIPPED,        // 已发货
    DELIVERED,      // 已送达
    COMPLETED,      // 已完成
    CANCELLED       // 已取消
}

