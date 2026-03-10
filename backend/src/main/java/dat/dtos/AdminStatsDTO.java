package dat.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AdminStatsDTO {

    private List<TopPlantDTO> topPlants;
    private List<AdminTaskDTO> lastTasks;
}

