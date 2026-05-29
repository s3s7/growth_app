package com.sg.GrowthApp.controller;

import com.sg.GrowthApp.entity.GrowthRecord;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StopwatchController {

    @GetMapping("/stopwatch")
    public String index(Model model) {
        model.addAttribute("growthRecord", new GrowthRecord());
        return "stopwatch/index";
    }
}
