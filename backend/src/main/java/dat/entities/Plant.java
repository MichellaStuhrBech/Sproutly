package dat.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Entity
@Setter
@Getter
@Table(name = "plants")
    public class Plant {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;
        private String latinName;

        private int sowingMonth; // 1-12

        @Column(length = 2000)
        private String note;

        @Column(nullable = false, columnDefinition = "boolean not null default false")
        private boolean completed;

        @ManyToOne
        @JoinColumn(name = "sowing_plan_id")
        private SowingPlan sowingPlan;

    }
