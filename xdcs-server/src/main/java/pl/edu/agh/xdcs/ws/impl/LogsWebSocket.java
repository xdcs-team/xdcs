package pl.edu.agh.xdcs.ws.impl;

import org.slf4j.Logger;
import pl.edu.agh.xdcs.db.entity.LogLineEntity;
import pl.edu.agh.xdcs.events.AgentLoggedEvent;
import pl.edu.agh.xdcs.restapi.mapper.LogLineMapper;
import pl.edu.agh.xdcs.restapi.model.LogDto;
import pl.edu.agh.xdcs.util.WsUtils;
import pl.edu.agh.xdcs.ws.util.JsonEncoder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Kamil Jarosz
 */
@ApplicationScoped
@ServerEndpoint(value = "/ws/tasks/{taskId}/logs", encoders = {JsonEncoder.class})
public class LogsWebSocket {
    private static final CloseReason UNEXPECTED_ERROR_REASON = new CloseReason(
            CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Unexpected error");

    private Map<String, Set<Session>> subscribersByTaskId = new HashMap<>();

    @Inject
    private Logger logger;

    @Inject
    private LogLineMapper logLineMapper;

    private Set<Session> getSubscribers(String taskId) {
        return subscribersByTaskId.computeIfAbsent(taskId, i -> new CopyOnWriteArraySet<>());
    }

    @OnOpen
    public void open(@PathParam("taskId") String taskId, Session session) {
        getSubscribers(taskId).add(session);
        logger.debug("A WS client has subscribed to logs of " + taskId);
    }

    @OnClose
    public void close(@PathParam("taskId") String taskId, Session session) {
        logger.debug("A WS client has unsubscribed from logs of " + taskId);
        getSubscribers(taskId).remove(session);
    }

    public void handleLogEvent(@Observes AgentLoggedEvent event) {
        LogLineEntity logLine = event.getLogLine();
        String taskId = logLine.getTask().getId();
        getSubscribers(taskId).forEach(session -> {
            try {
                LogDto log = logLineMapper.toRestEntity(logLine);
                session.getBasicRemote().sendObject(log);
            } catch (EncodeException | IOException e) {
                logger.error("Error sending websocket event", e);
                getSubscribers(taskId).remove(session);
                WsUtils.tryClose(session, UNEXPECTED_ERROR_REASON);
            }
        });
    }
}
