package model;

/**
 * Message entity class representing a chat message between users.
 */
public class Message {
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private String content;
    private String createdTime;
    private boolean read;

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFromUserId() { return fromUserId; }
    public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }

    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long toUserId) { this.toUserId = toUserId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCreatedTime() { return createdTime; }
    public void setCreatedTime(String createdTime) { this.createdTime = createdTime; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}