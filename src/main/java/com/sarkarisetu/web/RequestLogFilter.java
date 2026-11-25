package com.sarkarisetu.web;

import com.sarkarisetu.service.PageStats;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import java.io.IOException;

/** Logs every page request: method, path, status, time, user. Static files (css, js, img) are skipped. */
@Component
public class RequestLogFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger("PAGE");
    private final PageStats stats;

    public RequestLogFilter(PageStats stats) { this.stats = stats; }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String p = req.getRequestURI();
        return p.startsWith("/css/") || p.startsWith("/js/") || p.startsWith("/img/") || p.startsWith("/favicon") || p.equals("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            chain.doFilter(req, res);
        } finally {
            long ms = System.currentTimeMillis() - start;
            String path = req.getRequestURI();
            String full = req.getQueryString() == null ? path : path + "?" + req.getQueryString();
            Object best = req.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String pattern = best == null ? path : best.toString();
            String user = req.getUserPrincipal() == null ? "-" : req.getUserPrincipal().getName();
            int status = res.getStatus();
            if (status >= 500) log.error("{} {} -> {} ({} ms) user={} ip={}", req.getMethod(), full, status, ms, user, req.getRemoteAddr());
            else if (status >= 400) log.warn("{} {} -> {} ({} ms) user={} ip={}", req.getMethod(), full, status, ms, user, req.getRemoteAddr());
            else log.info("{} {} -> {} ({} ms) user={} ip={}", req.getMethod(), full, status, ms, user, req.getRemoteAddr());
            stats.record(req.getMethod(), pattern, path, status, ms, user);
        }
    }
}
