package com.farmconnect.controller;

import com.farmconnect.dto.DemandInsightDto;
import com.farmconnect.service.DemandInsightsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/insights")
public class DemandInsightsController {

    private final DemandInsightsService demandInsightsService;

    public DemandInsightsController(DemandInsightsService demandInsightsService) {
        this.demandInsightsService = demandInsightsService;
    }

    @GetMapping("/demand")
    public String demandInsights(Model model) {
        List<DemandInsightDto> insights = demandInsightsService.calculateDemandInsights();
        model.addAttribute("insights", insights);
        return "insights/demand";
    }
}
