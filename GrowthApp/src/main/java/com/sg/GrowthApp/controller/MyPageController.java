package com.sg.GrowthApp.controller;

import com.sg.GrowthApp.entity.GrowthRecord;
import com.sg.GrowthApp.service.GrowthRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final GrowthRecordService growthRecordService;

    @GetMapping
    public String index(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        List<GrowthRecord> records = growthRecordService.findAll();

        long totalSeconds = records.stream()
                .filter(r -> r.getDurationSeconds() != null)
                .mapToLong(GrowthRecord::getDurationSeconds)
                .sum();

        Map<String, Long> categoryCount = records.stream()
                .filter(r -> r.getCategory() != null && !r.getCategory().isEmpty())
                .collect(Collectors.groupingBy(GrowthRecord::getCategory, Collectors.counting()));

        int totalLikes = records.stream().mapToInt(GrowthRecord::getLikeCount).sum();

        List<GrowthRecord> recentRecords = records.stream().limit(5).toList();

        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("totalRecords", records.size());
        model.addAttribute("totalSeconds", totalSeconds);
        model.addAttribute("categoryCount", categoryCount);
        model.addAttribute("totalLikes", totalLikes);
        model.addAttribute("recentRecords", recentRecords);
        return "mypage";
    }
}
