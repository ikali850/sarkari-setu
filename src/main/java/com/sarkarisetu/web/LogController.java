package com.sarkarisetu.web;

import com.sarkarisetu.service.PageStats;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** Super Admin page: which pages are called and which never are. */
@Controller
@RequestMapping("/admin")
public class LogController {
    private final PageStats stats;
    private final RequestMappingHandlerMapping mapping;

    public LogController(PageStats stats, @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mapping) {
        this.stats = stats; this.mapping = mapping;
    }

    @GetMapping("/logs")
    public String logs(Model m) {
        List<PageStats.Row> rows = new ArrayList<>();
        for (RequestMappingInfo info : mapping.getHandlerMethods().keySet()) {
            Set<String> patterns = info.getPathPatternsCondition() == null ? Set.of() : info.getPathPatternsCondition().getPatternValues();
            Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
            String method = methods.isEmpty() ? "GET" : methods.iterator().next().name();
            for (String p : patterns) {
                if (p.equals("/error") || p.startsWith("/admin/logs")) continue;
                rows.add(stats.row(method, p));
            }
        }
        rows.sort(Comparator.comparingLong(PageStats.Row::getHits).reversed().thenComparing(PageStats.Row::getPattern));
        long never = rows.stream().filter(r -> r.getHits() == 0).count();
        m.addAttribute("rows", rows);
        m.addAttribute("never", never);
        m.addAttribute("missing", stats.missingRows());
        m.addAttribute("recent", stats.recent());
        m.addAttribute("total", stats.getTotal());
        m.addAttribute("since", stats.getSince());
        return "admin/logs";
    }
}
