package dat.entities;

import dat.security.entities.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import dat.security.entities.User;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class SowingPlan {

    @ManyToOne
    @JoinColumn(name="user_email")
    private User user;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "sowingPlan", cascade = CascadeType.ALL)
    private List<Plant> plants = new ArrayList<>();

    public List<Plant> getPlants() {
        return plants;
    }
}

