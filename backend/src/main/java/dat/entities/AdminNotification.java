package dat.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Admin-created message shown to all users as a notification on the given date.
 */
@Entity
@Table(name = "admin_notifications")
public class AdminNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false)
    private LocalDate showDate;

    public AdminNotification() {
    }

    public AdminNotification(String message, LocalDate showDate) {
        this.message = message;
        this.showDate = showDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDate getShowDate() {
        return showDate;
    }

    public void setShowDate(LocalDate showDate) {
        this.showDate = showDate;
    }
}
