package com.shorturl.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Home Controller for serving the main page
 */
@Controller
public class HomeController {

    /**
     * Serve the main page
     */
    @GetMapping("/")
    public String home() {
        return "forward:/static/index.html";
    }

    /**
     * Serve the main page for /index
     */
    @GetMapping("/index")
    public String index() {
        return "forward:/static/index.html";
    }
}