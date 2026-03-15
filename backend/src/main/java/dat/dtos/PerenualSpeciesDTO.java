package dat.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PerenualSpeciesDTO {

    private Integer id;

    @JsonProperty("common_name")
    private String commonName;

    @JsonProperty("scientific_name")
    private List<String> scientificName;

    @JsonProperty("default_image")
    private PerenualImageDTO defaultImage;
}
