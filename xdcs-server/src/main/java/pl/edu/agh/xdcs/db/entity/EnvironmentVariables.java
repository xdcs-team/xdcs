package pl.edu.agh.xdcs.db.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import pl.edu.agh.xdcs.db.conf.JsonConverter;
import pl.edu.agh.xdcs.or.types.EnvironmentVariable;
import java.util.ArrayList;
import java.util.List;

public class EnvironmentVariables {
    private List<EnvironmentVariable> environmentVariables;

    @JsonCreator
    public EnvironmentVariables(List<EnvironmentVariable> variables) {
        this.environmentVariables = new ArrayList<>(variables);
    }

    @JsonValue
    public List<EnvironmentVariable> getVariables() {
        return environmentVariables;
    }

    public static class Converter extends JsonConverter<EnvironmentVariables> {
        public Converter() {
            super(EnvironmentVariables.class);
        }
    }

}
