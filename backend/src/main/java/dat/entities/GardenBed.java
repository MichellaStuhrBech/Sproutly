package dat.entities;

import dat.security.entities.User;
import jakarta.persistence.*;

/**
 * A garden bed (square) belonging to a user. Contents describe what is planted, e.g. "peas, tomatoes and cauliflower".
 */
@Entity
@Table(name = "garden_beds")
public class GardenBed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200)
    private String name;

    @Column(length = 2000, nullable = false)
    private String contents = "";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_email", referencedColumnName = "username", nullable = false)
    private User user;

    public GardenBed() {
    }

    public GardenBed(String name, String contents, User user) {
        this.name = name;
        this.contents = contents != null ? contents : "";
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents != null ? contents : "";
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
