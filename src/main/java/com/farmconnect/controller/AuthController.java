package com.farmconnect.controller;

import com.farmconnect.dto.BuyerRegistrationDto;
import com.farmconnect.dto.FarmerRegistrationDto;
import com.farmconnect.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email/mobile or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }
        return "auth/login";
    }

    @GetMapping("/register-farmer")
    public String registerFarmerPage(Model model) {
        if (!model.containsAttribute("farmerDto")) {
            model.addAttribute("farmerDto", new FarmerRegistrationDto());
        }
        return "auth/register-farmer";
    }

    @PostMapping("/register-farmer")
    public String handleFarmerRegistration(@Valid @ModelAttribute("farmerDto") FarmerRegistrationDto dto,
                                           BindingResult bindingResult,
                                           RedirectAttributes redirectAttributes) {
        if (userService.emailExists(dto.getEmail())) {
            bindingResult.rejectValue("email", "error.farmerDto", "This email address is already registered.");
        }
        if (userService.mobileExists(dto.getMobile())) {
            bindingResult.rejectValue("mobile", "error.farmerDto", "This mobile number is already registered.");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register-farmer";
        }

        userService.registerFarmer(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Your farmer account has been created successfully! Please login.");
        return "redirect:/auth/login";
    }

    @GetMapping("/register-buyer")
    public String registerBuyerPage(Model model) {
        if (!model.containsAttribute("buyerDto")) {
            model.addAttribute("buyerDto", new BuyerRegistrationDto());
        }
        return "auth/register-buyer";
    }

    @PostMapping("/register-buyer")
    public String handleBuyerRegistration(@Valid @ModelAttribute("buyerDto") BuyerRegistrationDto dto,
                                          BindingResult bindingResult,
                                          RedirectAttributes redirectAttributes) {
        if (userService.emailExists(dto.getEmail())) {
            bindingResult.rejectValue("email", "error.buyerDto", "This email address is already registered.");
        }
        if (userService.mobileExists(dto.getMobile())) {
            bindingResult.rejectValue("mobile", "error.buyerDto", "This mobile number is already registered.");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register-buyer";
        }

        userService.registerBuyer(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Your buyer business account has been registered! Please login.");
        return "redirect:/auth/login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam("identifier") String identifier,
                                       RedirectAttributes redirectAttributes) {
        if (identifier == null || identifier.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please enter your registered email or mobile.");
            return "redirect:/auth/forgot-password";
        }
        // Educational reset confirmation message
        redirectAttributes.addFlashAttribute("successMessage", "Password reset instructions have been sent to " + identifier + ". (Demo verification link enabled)");
        return "redirect:/auth/login";
    }
}
