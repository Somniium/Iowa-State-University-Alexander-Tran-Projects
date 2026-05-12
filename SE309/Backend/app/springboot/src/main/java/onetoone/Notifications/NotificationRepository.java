package onetoone.Notifications;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByRecipient_IdOrderByCreatedAtDesc(int recipientId);

    List<Notification> findByRecipient_IdAndReadFalseOrderByCreatedAtDesc(int recipientId);

    long countByRecipient_IdAndReadFalse(int recipientId);

    @Transactional
    void deleteByRecipient_Id(int recipientId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Notification n WHERE n.recipient.userId = :recipientId")
    int deleteByRecipientId(@Param("recipientId") int recipientId);
}
