package pl.edu.agh.xdcs.restapi.mapper;

import pl.edu.agh.xdcs.agents.Agent;
import pl.edu.agh.xdcs.api.SystemInfo;
import pl.edu.agh.xdcs.restapi.model.AdditionalPropertyDto;
import pl.edu.agh.xdcs.restapi.model.SystemInfoDto;

import java.util.List;

/**
 * @author Krystian Życiński
 */
public class SystemInfoMapper extends AgentDetailsInfoMapper<SystemInfoDto> {

    public SystemInfoDto toRestEntity(Agent agent) {
        SystemInfoDto systemInfoDto = new SystemInfoDto();
        SystemInfo systemInfo = agentDetailsProvider.getSystemInfo(agent);
        systemInfoDto.setSystemName(systemInfo.getSystemFamilyName());
        systemInfoDto.setPythonVersion(systemInfo.getPythonVersion().getVersionStr());
        systemInfoDto.setPlatform(systemInfo.getPlatform());
        systemInfoDto.setOperatingSystemVersion(systemInfo.getUnameVersion());
        if(systemInfo.getSystemFamilyName().contains("Linux")) {
            systemInfoDto.setDistDescr(systemInfo.getDistDesc());
            systemInfoDto.setDistName(systemInfo.getDistName());
            systemInfoDto.setDistVersion(systemInfo.getDistVersion());
        }
        systemInfoDto.setAdditionalProperties(getAdditionalAttributes(systemInfo));
        return systemInfoDto;
    }

    private List<AdditionalPropertyDto> getAdditionalAttributes(SystemInfo systemInfo) {
        return null;
    }
}
