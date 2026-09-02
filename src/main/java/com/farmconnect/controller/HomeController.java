package com.farmconnect.controller;

import com.farmconnect.service.AnalyticsService;
import com.farmconnect.service.CropListingService;
import com.farmconnect.service.DemandInsightsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final CropListingService cropListingService;
    private final DemandInsightsService demandInsightsService;
    private final AnalyticsService analyticsService;

    public HomeController(CropListingService cropListingService,
                          DemandInsightsService demandInsightsService,
                          AnalyticsService analyticsService) {
        this.cropListingService = cropListingService;
        this.demandInsightsService = demandInsightsService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("recentListings", cropListingService.getActiveListings().stream().limit(6).toList());
        model.addAttribute("demandInsights", demandInsightsService.calculateDemandInsights().stream().limit(4).toList());
        model.addAttribute("platformStats", analyticsService.getPlatformStats());
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}
