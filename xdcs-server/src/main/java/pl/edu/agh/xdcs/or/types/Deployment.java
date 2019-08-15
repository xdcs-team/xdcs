package pl.edu.agh.xdcs.or.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.edu.agh.xdcs.or.ObjectBase;

/**
 * @author Kamil Jarosz
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class Deployment implements ObjectBase {
    @JsonProperty("def")
    private String definitionId;

    @JsonProperty("root")
    private String root;
}
