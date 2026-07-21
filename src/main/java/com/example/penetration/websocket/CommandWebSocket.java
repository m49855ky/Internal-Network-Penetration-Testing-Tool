package com.example.penetration.websocket;

import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ServerEndpoint("/command")
public class CommandWebSocket {
    private static final AtomicInteger onlineCount = new AtomicInteger(0);
    private static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();
    private static final Object sendLock = new Object(); // 发送同步锁

    @OnOpen
    public void onOpen(Session session) {
        sessionMap.put(session.getId(), session);
        onlineCount.incrementAndGet();
        sendMessage(session, "连接成功，当前在线人数：" + onlineCount.get());
    }

    @OnClose
    public void onClose(Session session) {
        sessionMap.remove(session.getId());
        onlineCount.decrementAndGet();
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    private void sendMessage(Session session, String message) {
        synchronized (sendLock) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendAll(String message) {
        sessionMap.forEach((id, session) -> {
            if (session.isOpen()) {
                sendMessage(session, message);
            } else {
                sessionMap.remove(id);
            }
        });
    }
}