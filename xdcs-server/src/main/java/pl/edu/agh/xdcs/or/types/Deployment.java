package pl.edu.agh.xdcs.or.types;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.edu.agh.xdcs.or.ObjectBase;

import java.util.List;

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

    @JsonProperty("descriptor")
    private String descriptorId;

    @JsonProperty("root")
    private String root;

    @JsonProperty("config")
    private Config config;

    public enum ConfigType {
        @JsonProperty("opencl")
        OPENCL,
        @JsonProperty("cuda")
        CUDA,
        @JsonProperty("docker")
        DOCKER,
        @JsonProperty("script")
        SCRIPT,
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @EqualsAndHashCode
    public static class Config {
        @JsonProperty("type")
        private ConfigType type;

        @JsonProperty("dockerfile")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private String dockerfile;

        @JsonProperty("allocate-tty")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private Boolean allocatePseudoTty;

        @JsonProperty("kernelfile")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private String kernelFile;

        @JsonProperty("kernelname")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private String kernelName;

        @JsonProperty("scriptfile")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private String scriptFile;

        @JsonProperty("kernelparams")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<KernelParameter> kernelParams;

        @JsonProperty("artifacts")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<String> artifacts;
    }

}
