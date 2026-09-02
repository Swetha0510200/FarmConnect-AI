package com.farmconnect.controller;

import com.farmconnect.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final CropListingService cropListingService;
    private final BuyerRequirementService requirementService;
    private final OrderService orderService;
    private final AnalyticsService analyticsService;
    private final DemandInsightsService demandInsightsService;

    public AdminController(UserService userService,
                           CropListingService cropListingService,
                           BuyerRequirementService requirementService,
                           OrderService orderService,
                           AnalyticsService analyticsService,
                           DemandInsightsService demandInsightsService) {
        this.userService = userService;
        this.cropListingService = cropListingService;
        this.requirementService = requirementService;
        this.orderService = orderService;
        this.analyticsService = analyticsService;
        this.demandInsightsService = demandInsightsService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", analyticsService.getPlatformStats());
        model.addAttribute("recentOrders", orderService.getAllOrders().stream().limit(5).toList());
        model.addAttribute("recentFarmers", userService.getFarmers().stream().limit(5).toList());
        model.addAttribute("recentBuyers", userService.getBuyers().stream().limit(5).toList());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @GetMapping("/farmers")
    public String farmers(Model model) {
        model.addAttribute("farmers", userService.getFarmers());
        return "admin/farmers";
    }

    @GetMapping("/buyers")
    public String buyers(Model model) {
        model.addAttribute("buyers", userService.getBuyers());
        return "admin/buyers";
    }

    @PostMapping("/verify-buyer/{id}")
    public String verifyBuyer(@PathVariable("id") Long buyerProfileId,
                              RedirectAttributes redirectAttributes) {
        userService.verifyBuyer(buyerProfileId);
        redirectAttributes.addFlashAttribute("successMessage", "Buyer account verified successfully!");
        return "redirect:/admin/buyers";
    }

    @PostMapping("/toggle-user/{id}")
    public String toggleUser(@PathVariable("id") Long userId,
                             RedirectAttributes redirectAttributes,
                             @RequestHeader(value = "referer", required = false) String referer) {
        userService.toggleUserStatus(userId);
        redirectAttributes.addFlashAttribute("successMessage", "User account status updated.");
        return referer != null ? "redirect:" + referer : "redirect:/admin/users";
    }

    @GetMapping("/listings")
    public String listings(Model model) {
        model.addAttribute("listings", cropListingService.getAllListings());
        return "admin/listings";
    }

    @GetMapping("/requirements")
    public String requirements(Model model) {
        model.addAttribute("requirements", requirementService.getAllRequirements());
        return "admin/requirements";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "admin/orders";
    }

    @GetMapping("/analytics")
    public String analytics(Model model) {
        model.addAttribute("stats", analyticsService.getPlatformStats());
        model.addAttribute("demandInsights", demandInsightsService.calculateDemandInsights());
        return "admin/analytics";
    }
}
