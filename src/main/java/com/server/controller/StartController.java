package com.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StartController {

    @GetMapping("/")
    public String index() {
        return "index"; // Spring sucht nach index.html im templates-Ordner
    }
}
