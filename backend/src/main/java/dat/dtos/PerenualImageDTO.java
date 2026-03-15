package dat.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PerenualImageDTO {

    @JsonProperty("thumbnail")
    private String thumbnail;

    @JsonProperty("small_url")
    private String smallUrl;

    @JsonProperty("regular_url")
    private String regularUrl;

    @JsonProperty("medium_url")
    private String mediumUrl;

    @JsonProperty("original_url")
    private String originalUrl;
}
