package dat.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * DTO for Perenual API species details response (single plant full info).
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PerenualSpeciesDetailsDTO {

    private Integer id;

    @JsonProperty("common_name")
    private String commonName;

    @JsonProperty("scientific_name")
    private List<String> scientificName;

    @JsonProperty("other_name")
    private List<String> otherName;

    private String family;
    private List<String> origin;
    private String type;
    private String cycle;
    private String watering;

    @JsonProperty("watering_general_benchmark")
    private Map<String, Object> wateringGeneralBenchmark;

    private List<String> sunlight;
    private List<String> soil;

    @JsonProperty("pruning_month")
    private List<String> pruningMonth;

    private String description;

    @JsonProperty("default_image")
    private PerenualImageDTO defaultImage;

    @JsonProperty("growth_rate")
    private String growthRate;

    private String maintenance;

    @JsonProperty("care_level")
    private String careLevel;

    @JsonProperty("poisonous_to_humans")
    private Boolean poisonousToHumans;

    @JsonProperty("poisonous_to_pets")
    private Boolean poisonousToPets;

    @JsonProperty("edible_fruit")
    private Boolean edibleFruit;

    @JsonProperty("edible_leaf")
    private Boolean edibleLeaf;

    private Boolean indoor;

    @JsonProperty("hardiness")
    private Map<String, String> hardiness;

    @JsonProperty("pest_susceptibility")
    private List<String> pestSusceptibility;

    @JsonProperty("flowering_season")
    private String floweringSeason;

    private Boolean flowers;
    private Boolean medicinal;
    private Boolean tropical;
    private Boolean thorny;
    private Boolean invasive;
    private Boolean rare;
    private Boolean drought_tolerant;
    private Boolean salt_tolerant;
}
