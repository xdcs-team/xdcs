package pl.edu.agh.xdcs.restapi.mapper;

import pl.edu.agh.xdcs.agents.Agent;
import pl.edu.agh.xdcs.api.GPUInfo;
import pl.edu.agh.xdcs.restapi.model.GPUInfoDto;

import javax.inject.Inject;
import java.util.stream.Collectors;

/**
 * @author Krystian Życiński
 */
public class GPUInfoMapper extends AgentDetailsInfoMapper<GPUInfoDto> {
    @Inject
    private CUDADeviceMapper cudaDeviceMapper;

    public GPUInfoDto toRestEntity(Agent agent) {
        GPUInfoDto gpuInfoDto = new GPUInfoDto();
        GPUInfo gpuInfo = agentDetailsProvider.getGPUInfo(agent);
        boolean isAvailable = gpuInfo.getCudaInfo().getCudaAvailable();
        gpuInfoDto.setIsAvailable(isAvailable);
        if (isAvailable) {
            gpuInfoDto.setCudaVersion(gpuInfo.getCudaInfo().getPycudaVersionText());
            gpuInfoDto.setDevices(gpuInfo.getCudaInfo().getDevicesList().stream()
                    .map(cudaDeviceMapper::toRestEntity)
                    .collect(Collectors.toList()));
        }
        return gpuInfoDto;
    }
}
