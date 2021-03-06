package pl.edu.agh.xdcs.or.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnvironmentVariable {
    @JsonProperty("name")
    private String name;

    @JsonProperty("value")
    private String value;
}
