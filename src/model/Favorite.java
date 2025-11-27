package model;

/**
 * Favorite entity class representing a user's favorite item.
 * Links users to their bookmarked items for quick access.
 */
public class Favorite {
    private Long id;
    private Long userId;
    private Long itemId;
    private String createdTime;

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public String getCreatedTime() { return createdTime; }
    public void setCreatedTime(String createdTime) { this.createdTime = createdTime; }
}