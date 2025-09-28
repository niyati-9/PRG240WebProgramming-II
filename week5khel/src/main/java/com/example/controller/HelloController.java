package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class HelloController {

    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);


    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String hello(Model model) {
        logger.info("Hello endpoint called");
        model.addAttribute("message", "Hello World from Spring MVC!");
        return "home";
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String test(Model model) {
        logger.info("Test endpoint called");
        model.addAttribute("message", "Khel App Test Page - Spring MVC is working!");
        return "home";
    }

}


