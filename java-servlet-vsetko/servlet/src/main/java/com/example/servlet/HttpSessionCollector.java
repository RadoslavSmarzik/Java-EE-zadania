package com.example.servlet;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import java.util.HashSet;
import java.util.Set;

@WebListener
public class HttpSessionCollector implements HttpSessionListener {
    public static final Set<String> SESSIONS = new HashSet<String>();

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        SESSIONS.add(session.getId());
    }


    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        SESSIONS.remove(event.getSession().getId());
    }

    public static boolean contain(String sessionId) {
        return SESSIONS.contains(sessionId);
    }
}