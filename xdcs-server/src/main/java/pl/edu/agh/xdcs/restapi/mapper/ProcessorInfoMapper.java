package pl.edu.agh.xdcs.restapi.mapper;

import pl.edu.agh.xdcs.agents.Agent;
import pl.edu.agh.xdcs.api.ProcessorInfo;
import pl.edu.agh.xdcs.restapi.model.AdditionalPropertyDto;
import pl.edu.agh.xdcs.restapi.model.ProcessorInfoDto;

import java.util.List;

/**
 * @author Krystian Życiński
 */
public class ProcessorInfoMapper extends AgentDetailsInfoMapper<ProcessorInfoDto> {

    public ProcessorInfoDto toRestEntity(Agent agent) {
        ProcessorInfoDto processorInfoDto = new ProcessorInfoDto();
        ProcessorInfo processorInfo = agentDetailsProvider.getProcessorInfo(agent);
        processorInfoDto.setCpuBrand(processorInfo.getCpuBrand());
        processorInfoDto.setCpuClock(processorInfo.getCpuClock());
        processorInfoDto.setCpuCount(processorInfo.getCpuCount());
        processorInfoDto.setCpuCache(getCpuCache(processorInfo));
        processorInfoDto.setCpuBits(processorInfo.getCpuBits());
        processorInfoDto.setAdditionalProperties(getAdditionalProperties(processorInfo));
        return processorInfoDto;
    }

    private String getCpuCache(ProcessorInfo processorInfo) {
        return processorInfo.getCpuCacheL1() + "/" + processorInfo.getCpuCacheL3();
    }

    private List<AdditionalPropertyDto> getAdditionalProperties(ProcessorInfo processorInfo) {
        return null;
    }
}
