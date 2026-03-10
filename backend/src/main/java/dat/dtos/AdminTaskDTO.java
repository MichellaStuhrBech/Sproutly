package dat.dtos;

import dat.entities.Task;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AdminTaskDTO {

    private Integer id;
    private String title;
    private String notes;
    private String userEmail;

    public AdminTaskDTO(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.notes = task.getNotes();
        this.userEmail = task.getUser() != null ? task.getUser().getEmail() : null;
    }
}

