package model;

public class Item {
    private Long id;
    private Long sellerId;
    private String title;
    private String description;
    private double price;
    private String category;
    private boolean active;      // 是否仍在售
    private String createdTime;  // 可用 LocalDateTime

}