package dat.dtos;

import dat.entities.Task;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TaskDTO {

    private Integer id;
    private String title;
    private String notes;

    public TaskDTO(String title, String notes) {
        this.title = title;
        this.notes = notes;
    }

    public TaskDTO(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.notes = task.getNotes();
    }
}
