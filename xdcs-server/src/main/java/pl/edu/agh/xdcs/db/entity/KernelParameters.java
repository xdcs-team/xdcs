package pl.edu.agh.xdcs.db.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import pl.edu.agh.xdcs.db.conf.JsonConverter;
import pl.edu.agh.xdcs.or.types.KernelParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kamil Jarosz
 */
public class KernelParameters {
    private List<KernelParameter> parameters;

    @JsonCreator
    public KernelParameters(List<KernelParameter> parameters) {
        this.parameters = new ArrayList<>(parameters);
    }

    @JsonValue
    public List<KernelParameter> getParameters() {
        return parameters;
    }

    public static class Converter extends JsonConverter<KernelParameters> {
        public Converter() {
            super(KernelParameters.class);
        }
    }
}
