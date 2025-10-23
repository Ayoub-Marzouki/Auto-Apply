package ai.autoapply.presentation.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/")
public class WebController {
    
    @GetMapping
    public String index() {
        return "index";
    }

    @GetMapping("start")
    public String landing() {
        return "start";
    }

    @GetMapping("guide")
    public String guide() {
        return "guide";
    }
    
}
