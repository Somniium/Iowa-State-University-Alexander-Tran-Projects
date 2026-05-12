package onetoone.Notifications;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import onetoone.Users.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Schema(description = "A notification delivered to a user when a relevant event occurs (COMMENT, LIKE, REVIEW)")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique notification ID", example = "1")
    private int id;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    @JsonIgnore
    private User recipient;

    @Transient
    @Schema(description = "ID of the user who received this notification", example = "42")
    private int recipientId;

    @Schema(description = "Event type: COMMENT | LIKE | REVIEW", example = "LIKE")
    private String type;

    @Column(length = 500)
    @Schema(description = "Human-readable notification message", example = "Alex liked your review of \"Thriller\"")
    private String message;

    @Schema(description = "Whether the user has dismissed/read this notification", example = "false")
    @Column(name = "is_read")
    private boolean read = false;

    @Schema(description = "Timestamp when the notification was created", example = "2024-11-01T14:30:00")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public Notification() {}

    public Notification(User recipient, String type, String message) {
        this.recipient = recipient;
        this.type = type;
        this.message = message;
    }

    public int getId() { return id; }
    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) {
        this.recipient = recipient;
        this.recipientId = recipient != null ? recipient.getId() : 0;
    }
    public int getRecipientId() { return recipient != null ? recipient.getId() : recipientId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
