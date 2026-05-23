package com.camping.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping({"/", "/index", "/login", "/register", "/shop", "/explore", "/budget", "/admin", "/profile"})
    public String index() {
        return "index";
    }
}
