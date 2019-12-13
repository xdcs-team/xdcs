package pl.edu.agh.xdcs.grpc;

import org.slf4j.Logger;
import pl.edu.agh.xdcs.agents.Agent;
import pl.edu.agh.xdcs.api.AgentInfoGrpc;
import pl.edu.agh.xdcs.api.GPUInfo;
import pl.edu.agh.xdcs.api.GPUInfoRequest;
import pl.edu.agh.xdcs.api.ProcessorInfo;
import pl.edu.agh.xdcs.api.ProcessorInfoRequest;
import pl.edu.agh.xdcs.api.SoftwareInfo;
import pl.edu.agh.xdcs.api.SoftwareInfoRequest;
import pl.edu.agh.xdcs.api.SystemInfo;
import pl.edu.agh.xdcs.api.SystemInfoRequest;
import pl.edu.agh.xdcs.grpc.session.GrpcSessionManager;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Krystian Życiński
 */
@ApplicationScoped
public class AgentDetailsProvider {
    @Inject
    private Logger logger;
    @Inject
    private GrpcSessionManager sessionManager;

    public ProcessorInfo getProcessorInfo(Agent agent) {
        logDebug(agent, "ProcessorInfo");
        AgentInfoGrpc.AgentInfoBlockingStub stub = getStub(agent);
        return stub.getProcessorInfo(ProcessorInfoRequest.newBuilder().build());
    }

    public SystemInfo getSystemInfo(Agent agent) {
        logDebug(agent, "SystemInfo");
        AgentInfoGrpc.AgentInfoBlockingStub stub = getStub(agent);
        return stub.getSystemInfo(SystemInfoRequest.newBuilder().build());
    }

    public GPUInfo getGPUInfo(Agent agent) {
        logDebug(agent, "GPUInfo");
        AgentInfoGrpc.AgentInfoBlockingStub stub = getStub(agent);
        return stub.getGPUInfo(GPUInfoRequest.newBuilder().build());
    }

    public SoftwareInfo getSoftwareInfo(Agent agent) {
        logDebug(agent, "SoftwareInfo");
        AgentInfoGrpc.AgentInfoBlockingStub stub = getStub(agent);
        return stub.getSoftwareInfo(SoftwareInfoRequest.newBuilder().build());
    }

    private AgentInfoGrpc.AgentInfoBlockingStub getStub(Agent agent) {
        return sessionManager.getStubProducer(agent).getAgentInfoBlockingStub();
    }

    private void logDebug(Agent agent, String info) {
        logger.debug("Requesting agent " + agent.getName() + " for " + info);
    }
}
