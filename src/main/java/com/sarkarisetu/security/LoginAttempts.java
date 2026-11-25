package com.sarkarisetu.security;

import org.springframework.stereotype.Component;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/** Blocks an account for 10 minutes after 5 wrong passwords (kept in memory). */
@Component
public class LoginAttempts {
    private static final int MAX = 5;
    private static final long WINDOW_MS = 10 * 60 * 1000L;
    private final Map<String, Deque<Long>> fails = new ConcurrentHashMap<>();

    public void fail(String user) {
        if (user == null) return;
        if (fails.size() > 10000) fails.clear();
        fails.computeIfAbsent(user.toLowerCase(), k -> new ConcurrentLinkedDeque<>()).addLast(System.currentTimeMillis());
    }
    public void ok(String user) { if (user != null) fails.remove(user.toLowerCase()); }
    public boolean isBlocked(String user) {
        if (user == null) return false;
        Deque<Long> d = fails.get(user.toLowerCase());
        if (d == null) return false;
        long cut = System.currentTimeMillis() - WINDOW_MS;
        d.removeIf(t -> t < cut);
        return d.size() >= MAX;
    }
}
