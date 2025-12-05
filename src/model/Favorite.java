package model;

/**
 * Favorite entity class representing a user's favorite item.
 * Links users to their bookmarked items for quick access.
 * Note: Uses composite primary key (userId, itemId) in database.
 */
public class Favorite {
    private Long userId;
    private Long itemId;
    private String createdTime;

    // Getter & Setter
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public String getCreatedTime() { return createdTime; }
    public void setCreatedTime(String createdTime) { this.createdTime = createdTime; }
}