package com.farmconnect.controller;

import com.farmconnect.dto.*;
import com.farmconnect.entity.*;
import com.farmconnect.exception.ResourceNotFoundException;
import com.farmconnect.service.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/farmer")
public class FarmerController {

    private final UserService userService;
    private final CropListingService cropListingService;
    private final BuyerRequirementService buyerRequirementService;
    private final MatchingService matchingService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final DeliveryService deliveryService;
        private final AnalyticsService analyticsService;
    private final NotificationService notificationService;

    public FarmerController(UserService userService,
                            CropListingService cropListingService,
                            BuyerRequirementService buyerRequirementService,
                            MatchingService matchingService,
                            OrderService orderService,
                            PaymentService paymentService,
                            DeliveryService deliveryService,
                                                        AnalyticsService analyticsService,
                            NotificationService notificationService) {
        this.userService = userService;
        this.cropListingService = cropListingService;
        this.buyerRequirementService = buyerRequirementService;
        this.matchingService = matchingService;
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.deliveryService = deliveryService;
                this.analyticsService = analyticsService;
        this.notificationService = notificationService;
    }

    private User getAuthenticatedFarmer(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Farmer user not found"));
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User farmer = getAuthenticatedFarmer(userDetails);
        List<CropListing> listings = cropListingService.getFarmerListings(farmer);
        List<Order> orders = orderService.getFarmerOrders(farmer);

        // Find top buyer opportunities across farmer listings
        List<MatchResultDto> topMatches = new ArrayList<>();
        for (CropListing listing : listings) {
            if (listing.getStatus() == ListingStatus.ACTIVE) {
                List<MatchResultDto> matches = matchingService.findMatchesForCropListing(listing);
                topMatches.addAll(matches);
            }
        }
        topMatches.sort((a, b) -> Integer.compare(b.getSuitabilityScore(), a.getSuitabilityScore()));
        List<MatchResultDto> displayedMatches = topMatches.stream().limit(4).toList();

        model.addAttribute("farmer", farmer);
        model.addAttribute("profile", farmer.getFarmerProfile());
        model.addAttribute("listings", listings);
        model.addAttribute("orders", orders);
        model.addAttribute("topMatches", displayedMatches);
                model.addAttribute("notifications", notificationService.getRecentUserNotifications(farmer));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(farmer));

        return "farmer/dashboard";
    }

    @GetMapping("/crops")
    public String myCrops(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User farmer = getAuthenticatedFarmer(userDetails);
        model.addAttribute("listings", cropListingService.getFarmerListings(farmer));
        return "farmer/crops-list";
    }

    @GetMapping("/add-crop")
    public String addCropPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User farmer = getAuthenticatedFarmer(userDetails);
        if (!model.containsAttribute("cropDto")) {
            CropListingDto dto = new CropListingDto();
            if (farmer.getFarmerProfile() != null) {
                dto.setLocation(farmer.getFarmerProfile().getVillage() != null ? farmer.getFarmerProfile().getVillage() : "");
                dto.setDistrict(farmer.getFarmerProfile().getDistrict());
                dto.setState(farmer.getFarmerProfile().getState());
            }
            model.addAttribute("cropDto", dto);
        }
        return "farmer/add-crop";
    }

    @PostMapping("/add-crop")
    public String handleAddCrop(@AuthenticationPrincipal UserDetails userDetails,
                                @Valid @ModelAttribute("cropDto") CropListingDto dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        User farmer = getAuthenticatedFarmer(userDetails);
        if (dto.getAvailableUntil() != null && dto.getAvailableFrom() != null && dto.getAvailableUntil().isBefore(dto.getAvailableFrom())) {
            bindingResult.rejectValue("availableUntil", "error.cropDto", "Available until date must be on or after available from date.");
        }

        if (bindingResult.hasErrors()) {
            return "farmer/add-crop";
        }

        CropListing saved = cropListingService.createListing(farmer, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Crop listing for " + saved.getCropName() + " added successfully!");
        return "redirect:/farmer/crops";
    }

    @GetMapping("/edit-crop/{id}")
    public String editCropPage(@PathVariable("id") Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User farmer = getAuthenticatedFarmer(userDetails);
        CropListing listing = cropListingService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crop listing not found"));

        if (!listing.getFarmer().getId().equals(farmer.getId())) {
            return "redirect:/farmer/crops";
        }

        CropListingDto dto = new CropListingDto();
        dto.setId(listing.getId());
        dto.setCropName(listing.getCropName());
        dto.setQuantity(listing.getQuantity());
        dto.setUnit(listing.getUnit());
        dto.setExpectedPrice(listing.getExpectedPrice());
        dto.setQualityGrade(listing.getQualityGrade());
        dto.setAvailableFrom(listing.getAvailableFrom());
        dto.setAvailableUntil(listing.getAvailableUntil());
        dto.setLocation(listing.getLocation());
        dto.setDistrict(listing.getDistrict());
        dto.setState(listing.getState());
        dto.setDescription(listing.getDescription());

        model.addAttribute("cropDto", dto);
        model.addAttribute("listingId", id);
        return "farmer/edit-crop";
    }

    @PostMapping("/edit-crop/{id}")
    public String handleEditCrop(@PathVariable("id") Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 @Valid @ModelAttribute("cropDto") CropListingDto dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        User farmer = getAuthenticatedFarmer(userDetails);
        if (bindingResult.hasErrors()) {
            model.addAttribute("listingId", id);
            return "farmer/edit-crop";
        }

        cropListingService.updateListing(id, farmer, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Crop listing updated successfully!");
        return "redirect:/farmer/crops";
    }

    @PostMapping("/delete-crop/{id}")
    public String deleteCrop(@PathVariable("id") Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User farmer = getAuthenticatedFarmer(userDetails);
        cropListingService.deleteListing(id, farmer);
        redirectAttributes.addFlashAttribute("successMessage", "Crop listing removed successfully.");
        return "redirect:/farmer/crops";
    }

    @PostMapping("/toggle-crop/{id}")
    public String toggleCrop(@PathVariable("id") Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User farmer = getAuthenticatedFarmer(userDetails);
        cropListingService.toggleStatus(id, farmer);
        redirectAttributes.addFlashAttribute("successMessage", "Crop listing status updated.");
        return "redirect:/farmer/crops";
    }

    @GetMapping("/buyer-matches")
    public String allBuyerMatches(@AuthenticationPrincipal UserDetails userDetails,
                                  @RequestParam(value = "cropId", required = false) Long cropId,
                                  Model model) {
        User farmer = getAuthenticatedFarmer(userDetails);
        List<CropListing> listings = cropListingService.getFarmerListings(farmer);

        List<MatchResultDto> matches = new ArrayList<>();
        if (cropId != null) {
            cropListingService.findById(cropId).ifPresent(l -> {
                if (l.getFarmer().getId().equals(farmer.getId())) {
                    matches.addAll(matchingService.findMatchesForCropListing(l));
                    model.addAttribute("selectedListing", l);
                }
            });
        } else {
            for (CropListing l : listings) {
                if (l.getStatus() == ListingStatus.ACTIVE) {
                    matches.addAll(matchingService.findMatchesForCropListing(l));
                }
            }
            matches.sort((a, b) -> Integer.compare(b.getSuitabilityScore(), a.getSuitabilityScore()));
        }

        model.addAttribute("listings", listings);
        model.addAttribute("matches", matches);
        model.addAttribute("selectedCropId", cropId);
        return "farmer/buyer-matches";
    }

    @GetMapping("/buyer-details/{reqId}")
    public String buyerDetails(@PathVariable("reqId") Long reqId,
                               @RequestParam(value = "cropId", required = false) Long cropId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User farmer = getAuthenticatedFarmer(userDetails);
        BuyerRequirement req = buyerRequirementService.findById(reqId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer requirement not found"));

        MatchResultDto matchInfo = null;
        if (cropId != null) {
            Optional<CropListing> listing = cropListingService.findById(cropId);
            if (listing.isPresent() && listing.get().getFarmer().getId().equals(farmer.getId())) {
                matchInfo = matchingService.evaluateSuitability(listing.get(), req);
            }
        }

        model.addAttribute("requirement", req);
        model.addAttribute("buyer", req.getBuyer());
        model.addAttribute("buyerProfile", req.getBuyer().getBuyerProfile());
        model.addAttribute("matchInfo", matchInfo);
        model.addAttribute("cropId", cropId);
        return "farmer/buyer-details";
    }

    @GetMapping("/orders")
    public String myOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User farmer = getAuthenticatedFarmer(userDetails);
        model.addAttribute("orders", orderService.getFarmerOrders(farmer));
        return "farmer/orders";
    }

    @GetMapping("/orders/{orderId}")
    public String orderDetails(@PathVariable("orderId") Long orderId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User farmer = getAuthenticatedFarmer(userDetails);
        Order order = orderService.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID " + orderId));

        if (!order.getFarmer().getId().equals(farmer.getId())) {
            return "redirect:/farmer/orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("buyer", order.getBuyer());
        model.addAttribute("buyerProfile", order.getBuyer().getBuyerProfile());
        model.addAttribute("payment", order.getPaymentRecord());
        model.addAttribute("delivery", order.getDeliveryArrangement());
        return "farmer/order-details";
    }

    @PostMapping("/orders/{orderId}/status")
    public String updateOrderStatus(@PathVariable("orderId") Long orderId,
                                    @RequestParam("status") OrderStatus newStatus,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        User farmer = getAuthenticatedFarmer(userDetails);
        orderService.updateOrderStatus(orderId, newStatus, farmer);
        redirectAttributes.addFlashAttribute("successMessage", "Order status updated to: " + newStatus.name().replace("_", " "));
        return "redirect:/farmer/orders/" + orderId;
    }

    @PostMapping("/orders/{orderId}/mark-payment")
    public String markPaymentReceived(@PathVariable("orderId") Long orderId,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      RedirectAttributes redirectAttributes) {
        User farmer = getAuthenticatedFarmer(userDetails);
        paymentService.markPaymentReceived(orderId, farmer);
        redirectAttributes.addFlashAttribute("successMessage", "Payment marked as RECEIVED.");
        return "redirect:/farmer/orders/" + orderId;
    }

    @PostMapping("/orders/{orderId}/delivery")
    public String updateDelivery(@PathVariable("orderId") Long orderId,
                                 @ModelAttribute DeliveryUpdateDto dto,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User farmer = getAuthenticatedFarmer(userDetails);
        deliveryService.updateDelivery(orderId, dto, farmer);
        redirectAttributes.addFlashAttribute("successMessage", "Transportation coordination details updated.");
        return "redirect:/farmer/orders/" + orderId;
    }

    @GetMapping("/analytics")
    public String analytics(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User farmer = getAuthenticatedFarmer(userDetails);
        model.addAttribute("analytics", analyticsService.getFarmerAnalytics(farmer));
        return "farmer/analytics";
    }

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User farmer = getAuthenticatedFarmer(userDetails);
        model.addAttribute("user", farmer);
        model.addAttribute("profile", farmer.getFarmerProfile());
        return "farmer/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @ModelAttribute UserProfileDto dto,
                                RedirectAttributes redirectAttributes) {
        User farmer = getAuthenticatedFarmer(userDetails);
        userService.updateUserProfile(farmer, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/farmer/profile";
    }
}
