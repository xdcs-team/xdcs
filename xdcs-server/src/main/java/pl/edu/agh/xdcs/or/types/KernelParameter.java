package pl.edu.agh.xdcs.or.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Kamil Jarosz
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KernelParameter {
    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private Type type;

    @JsonProperty("direction")
    private Direction direction;

    public enum Type {
        SIMPLE,
        POINTER,
        ;
    }

    public enum Direction {
        IN,
        OUT,
        IN_OUT,
        ;
    }
}
