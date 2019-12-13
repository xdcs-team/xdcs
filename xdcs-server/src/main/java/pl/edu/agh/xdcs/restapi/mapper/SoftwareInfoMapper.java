package pl.edu.agh.xdcs.restapi.mapper;

import pl.edu.agh.xdcs.agents.Agent;
import pl.edu.agh.xdcs.api.SoftwareInfo;
import pl.edu.agh.xdcs.restapi.model.SoftwareInfoDto;

/**
 * @author Krystian Życiński
 */
public class SoftwareInfoMapper extends AgentDetailsInfoMapper<SoftwareInfoDto> {

    public SoftwareInfoDto toRestEntity(Agent agent) {
        SoftwareInfoDto softwareInfoDto = new SoftwareInfoDto();
        SoftwareInfo softwareInfo = agentDetailsProvider.getSoftwareInfo(agent);
        softwareInfoDto.setDockerVersion(softwareInfo.getDockerVersion());
        softwareInfoDto.setPrograms(softwareInfo.getProgramsList());
        return softwareInfoDto;
    }
}
