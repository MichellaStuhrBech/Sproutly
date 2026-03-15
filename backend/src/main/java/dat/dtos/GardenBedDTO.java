package dat.dtos;

import dat.entities.GardenBed;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GardenBedDTO {

    private Long id;
    private String name;
    private String contents;

    public GardenBedDTO(String name, String contents) {
        this.name = name;
        this.contents = contents != null ? contents : "";
    }

    public GardenBedDTO(GardenBed bed) {
        this.id = bed.getId();
        this.name = bed.getName();
        this.contents = bed.getContents() != null ? bed.getContents() : "";
    }
}
