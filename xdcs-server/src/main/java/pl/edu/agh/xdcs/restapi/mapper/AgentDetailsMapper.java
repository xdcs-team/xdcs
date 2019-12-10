package pl.edu.agh.xdcs.restapi.mapper;

import pl.edu.agh.xdcs.agents.Agent;
import pl.edu.agh.xdcs.restapi.model.NodeDetailsDto;

import javax.inject.Inject;

/**
 * @author Krystian Życiński
 */
public class AgentDetailsMapper {
    @Inject
    private ProcessorInfoMapper processorInfoMapper;

    @Inject
    private SystemInfoMapper systemInfoMapper;

    @Inject
    private GPUInfoMapper gpuInfoMapper;

    @Inject
    private SoftwareInfoMapper softwareInfoMapper;

    public NodeDetailsDto toRestEntity(Agent agent) {
        NodeDetailsDto nodeDetailsDto = new NodeDetailsDto();
        nodeDetailsDto.setNodeId(agent.getName());
        nodeDetailsDto.setProcessorInfo(processorInfoMapper.toRestEntity(agent));
        nodeDetailsDto.setSystemInfo(systemInfoMapper.toRestEntity(agent));
        nodeDetailsDto.setGpuInfo(gpuInfoMapper.toRestEntity(agent));
        nodeDetailsDto.setSoftwareInfo(softwareInfoMapper.toRestEntity(agent));
        return nodeDetailsDto;
    }
}
