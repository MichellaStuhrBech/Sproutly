package dat.security.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDTO {

    private String email;
    private String password;
    /** Optional display name (e.g. "Mickey Mouse") — stored on register. */
    private String name;
}