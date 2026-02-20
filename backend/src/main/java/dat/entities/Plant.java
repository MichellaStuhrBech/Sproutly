package dat.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "plants")
    public class Plant {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;
        private String latinName;

        private int sowingMonth; // 1-12

        @ManyToOne
        @JoinColumn(name = "sowing_plan_id")
        private SowingPlan sowingPlan;



}
