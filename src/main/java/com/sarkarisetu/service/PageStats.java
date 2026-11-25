package com.sarkarisetu.service;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

/** In-memory page hit counters (reset on restart). Every request is also written to the log file. */
@Component
public class PageStats {
    private static final DateTimeFormatter T = DateTimeFormatter.ofPattern("dd MMM HH:mm:ss");

    public static class Row {
        private final String method, pattern, last;
        private final long hits, errors, avgMs;
        private final int status;
        public Row(String method, String pattern, long hits, long errors, long avgMs, int status, String last) {
            this.method = method; this.pattern = pattern; this.hits = hits; this.errors = errors; this.avgMs = avgMs; this.status = status; this.last = last;
        }
        public String getMethod() { return method; } public String getPattern() { return pattern; }
        public long getHits() { return hits; } public long getErrors() { return errors; } public long getAvgMs() { return avgMs; }
        public int getStatus() { return status; } public String getLast() { return last; }
    }

    private static class Counter {
        final AtomicLong hits = new AtomicLong(), errors = new AtomicLong(), ms = new AtomicLong();
        volatile LocalDateTime last = LocalDateTime.now();
        volatile int status;
    }

    private final Map<String, Counter> pages = new ConcurrentHashMap<>();
    private final Map<String, Counter> missing = new ConcurrentHashMap<>();
    private final Deque<String> recent = new ConcurrentLinkedDeque<>();
    private final AtomicLong total = new AtomicLong();
    private final LocalDateTime since = LocalDateTime.now();

    public void record(String method, String pattern, String path, int status, long ms, String user) {
        total.incrementAndGet();
        Map<String, Counter> target = status == 404 ? missing : pages;
        String key = status == 404 ? method + " " + path : method + " " + pattern;
        if (status == 404 && missing.size() > 500 && !missing.containsKey(key)) return;
        Counter c = target.computeIfAbsent(key, k -> new Counter());
        c.hits.incrementAndGet(); c.ms.addAndGet(ms); c.last = LocalDateTime.now(); c.status = status;
        if (status >= 400) c.errors.incrementAndGet();
        recent.addFirst(LocalDateTime.now().format(T) + "  " + method + " " + path + "  " + status + "  " + ms + " ms  " + user);
        while (recent.size() > 100) recent.pollLast();
    }

    public Row row(String method, String pattern) {
        Counter c = pages.get(method + " " + pattern);
        if (c == null) return new Row(method, pattern, 0, 0, 0, 0, "never");
        long h = Math.max(c.hits.get(), 1);
        return new Row(method, pattern, c.hits.get(), c.errors.get(), c.ms.get() / h, c.status, c.last.format(T));
    }

    public List<Row> missingRows() {
        List<Row> out = new ArrayList<>();
        for (Map.Entry<String, Counter> e : missing.entrySet()) {
            Counter c = e.getValue();
            String[] kv = e.getKey().split(" ", 2);
            out.add(new Row(kv[0], kv.length > 1 ? kv[1] : "", c.hits.get(), c.errors.get(), 0, 404, c.last.format(T)));
        }
        out.sort(Comparator.comparingLong(Row::getHits).reversed());
        return out;
    }

    public List<String> recent() { return new ArrayList<>(recent); }
    public long getTotal() { return total.get(); }
    public String getSince() { return since.format(T); }
}
