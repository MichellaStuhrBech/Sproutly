package dat.dtos;

import dat.entities.Plant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PlantDTO {

    private Long id;
    private String name;
    private String latinName;
    private int sowingMonth; // 1-12
    private String note;
    private boolean completed;

    public PlantDTO(Plant plant) {
        this.id = plant.getId();
        this.name = plant.getName();
        this.latinName = plant.getLatinName();
        this.sowingMonth = plant.getSowingMonth();
        this.note = plant.getNote();
        this.completed = plant.isCompleted();
    }

}