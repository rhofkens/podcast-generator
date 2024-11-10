package ai.bluefields.podcastgen.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    @GetMapping(value = {"/", "/settings", "/podcasts/**"})
    public String index() {
        return "index.html";
    }
}
