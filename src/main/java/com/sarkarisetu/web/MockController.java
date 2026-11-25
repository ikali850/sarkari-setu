package com.sarkarisetu.web;

import com.sarkarisetu.service.MockTestService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Controller
public class MockController {
    private final MockTestService mocks;

    public MockController(MockTestService mocks) { this.mocks = mocks; }

    @GetMapping("/mock-tests")
    public String list(Model m) {
        m.addAttribute("tests", mocks.all());
        return "mock/index";
    }

    @GetMapping("/mock-tests/{slug}")
    public String test(@PathVariable String slug, Model m) {
        MockTestService.Test t = mocks.find(slug).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        m.addAttribute("test", t);
        m.addAttribute("letters", List.of("A", "B", "C", "D"));
        m.addAttribute("metaDescription", t.getDescription());
        return "mock/test";
    }
}
