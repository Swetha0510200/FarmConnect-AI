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

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/buyer")
public class BuyerController {

    private final UserService userService;
    private final CropListingService cropListingService;
    private final BuyerRequirementService requirementService;
    private final MatchingService matchingService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final DeliveryService deliveryService;
    private final NotificationService notificationService;

    public BuyerController(UserService userService,
                           CropListingService cropListingService,
                           BuyerRequirementService requirementService,
                           MatchingService matchingService,
                           OrderService orderService,
                           PaymentService paymentService,
                           DeliveryService deliveryService,
                           NotificationService notificationService) {
        this.userService = userService;
        this.cropListingService = cropListingService;
        this.requirementService = requirementService;
        this.matchingService = matchingService;
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.deliveryService = deliveryService;
        this.notificationService = notificationService;
    }

    private User getAuthenticatedBuyer(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Buyer user not found"));
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User buyer = getAuthenticatedBuyer(userDetails);
        List<BuyerRequirement> reqs = requirementService.getBuyerRequirements(buyer);
        List<Order> orders = orderService.getBuyerOrders(buyer);
        List<CropListing> recentListings = cropListingService.getActiveListings().stream().limit(6).toList();

        model.addAttribute("buyer", buyer);
        model.addAttribute("profile", buyer.getBuyerProfile());
        model.addAttribute("requirements", reqs);
        model.addAttribute("orders", orders);
        model.addAttribute("recentListings", recentListings);
        model.addAttribute("savedCount", cropListingService.getSavedListings(buyer).size());
        model.addAttribute("notifications", notificationService.getRecentUserNotifications(buyer));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(buyer));

        return "buyer/dashboard";
    }

    @GetMapping("/search-crops")
    public String searchCrops(@RequestParam(value = "crop", required = false) String crop,
                              @RequestParam(value = "district", required = false) String district,
                              @RequestParam(value = "minPrice", required = false) Double minPrice,
                              @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                              @RequestParam(value = "minQuantity", required = false) Double minQuantity,
                              @RequestParam(value = "qualityGrade", required = false) String qualityGrade,
                              Model model) {
        List<CropListing> listings = cropListingService.searchListings(crop, district, minPrice, maxPrice, minQuantity, qualityGrade);

        model.addAttribute("listings", listings);
        model.addAttribute("selectedCrop", crop);
        model.addAttribute("selectedDistrict", district);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("minQuantity", minQuantity);
        model.addAttribute("qualityGrade", qualityGrade);

        return "buyer/search-crops";
    }

    @GetMapping("/crops/{id}")
    public String cropDetails(@PathVariable("id") Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        User buyer = getAuthenticatedBuyer(userDetails);
        CropListing listing = cropListingService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crop listing not found"));

        boolean isSaved = cropListingService.isListingSaved(buyer, listing);

        model.addAttribute("listing", listing);
        model.addAttribute("farmer", listing.getFarmer());
        model.addAttribute("farmerProfile", listing.getFarmer().getFarmerProfile());
        model.addAttribute("isSaved", isSaved);

        return "buyer/crop-details";
    }

    @GetMapping("/place-order/{cropId}")
    public String placeOrderPage(@PathVariable("cropId") Long cropId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        User buyer = getAuthenticatedBuyer(userDetails);
        CropListing listing = cropListingService.findById(cropId)
                .orElseThrow(() -> new ResourceNotFoundException("Crop listing not found"));

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            return "redirect:/buyer/search-crops";
        }

        if (!model.containsAttribute("orderDto")) {
            OrderPlacementDto dto = new OrderPlacementDto();
            dto.setCropListingId(listing.getId());
            dto.setQuantity(listing.getQuantity());
            dto.setAgreedPrice(listing.getExpectedPrice());
            model.addAttribute("orderDto", dto);
        }

        model.addAttribute("listing", listing);
        model.addAttribute("buyer", buyer);
        model.addAttribute("buyerProfile", buyer.getBuyerProfile());

        return "buyer/place-order";
    }

    @PostMapping("/place-order")
    public String handlePlaceOrder(@AuthenticationPrincipal UserDetails userDetails,
                                   @Valid @ModelAttribute("orderDto") OrderPlacementDto dto,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        User buyer = getAuthenticatedBuyer(userDetails);

        if (bindingResult.hasErrors()) {
            CropListing listing = cropListingService.findById(dto.getCropListingId()).orElseThrow();
            model.addAttribute("listing", listing);
            model.addAttribute("buyer", buyer);
            model.addAttribute("buyerProfile", buyer.getBuyerProfile());
            return "buyer/place-order";
        }

        try {
            Order placed = orderService.placeOrder(buyer, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Your order " + placed.getOrderNumber() + " has been placed successfully!");
            return "redirect:/buyer/orders/" + placed.getId();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/buyer/crops/" + dto.getCropListingId();
        }
    }

    @GetMapping("/requirements")
    public String myRequirements(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User buyer = getAuthenticatedBuyer(userDetails);
        model.addAttribute("requirements", requirementService.getBuyerRequirements(buyer));
        return "buyer/requirements";
    }

    @GetMapping("/add-requirement")
    public String addRequirementPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User buyer = getAuthenticatedBuyer(userDetails);
        if (!model.containsAttribute("reqDto")) {
            BuyerRequirementDto dto = new BuyerRequirementDto();
            if (buyer.getBuyerProfile() != null) {
                dto.setLocation(buyer.getBuyerProfile().getAddress() != null ? buyer.getBuyerProfile().getAddress() : "");
                dto.setDistrict(buyer.getBuyerProfile().getDistrict());
                dto.setState(buyer.getBuyerProfile().getState());
            }
            model.addAttribute("reqDto", dto);
        }
        return "buyer/add-requirement";
    }

    @PostMapping("/add-requirement")
    public String handleAddRequirement(@AuthenticationPrincipal UserDetails userDetails,
                                       @Valid @ModelAttribute("reqDto") BuyerRequirementDto dto,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {
        User buyer = getAuthenticatedBuyer(userDetails);
        if (dto.getMaximumPrice() != null && dto.getMinimumPrice() != null && dto.getMaximumPrice() < dto.getMinimumPrice()) {
            bindingResult.rejectValue("maximumPrice", "error.reqDto", "Maximum price must be greater than or equal to minimum price.");
        }
        if (dto.getRequiredUntil() != null && dto.getRequiredFrom() != null && dto.getRequiredUntil().isBefore(dto.getRequiredFrom())) {
            bindingResult.rejectValue("requiredUntil", "error.reqDto", "Required until date must be on or after required from date.");
        }

        if (bindingResult.hasErrors()) {
            return "buyer/add-requirement";
        }

        BuyerRequirement saved = requirementService.createRequirement(buyer, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Requirement for " + saved.getCropName() + " posted successfully!");
        return "redirect:/buyer/requirements";
    }

    @GetMapping("/edit-requirement/{id}")
    public String editRequirementPage(@PathVariable("id") Long id,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      Model model) {
        User buyer = getAuthenticatedBuyer(userDetails);
        BuyerRequirement req = requirementService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found"));

        if (!req.getBuyer().getId().equals(buyer.getId())) {
            return "redirect:/buyer/requirements";
        }

        BuyerRequirementDto dto = new BuyerRequirementDto();
        dto.setId(req.getId());
        dto.setCropName(req.getCropName());
        dto.setRequiredQuantity(req.getRequiredQuantity());
        dto.setUnit(req.getUnit());
        dto.setMinimumPrice(req.getMinimumPrice());
        dto.setMaximumPrice(req.getMaximumPrice());
        dto.setRequiredFrom(req.getRequiredFrom());
        dto.setRequiredUntil(req.getRequiredUntil());
        dto.setLocation(req.getLocation());
        dto.setDistrict(req.getDistrict());
        dto.setState(req.getState());
        dto.setQualityRequirement(req.getQualityRequirement());
        dto.setDescription(req.getDescription());

        model.addAttribute("reqDto", dto);
        model.addAttribute("reqId", id);
        return "buyer/edit-requirement";
    }

    @PostMapping("/edit-requirement/{id}")
    public String handleEditRequirement(@PathVariable("id") Long id,
                                        @AuthenticationPrincipal UserDetails userDetails,
                                        @Valid @ModelAttribute("reqDto") BuyerRequirementDto dto,
                                        BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {
        User buyer = getAuthenticatedBuyer(userDetails);
        if (bindingResult.hasErrors()) {
            model.addAttribute("reqId", id);
            return "buyer/edit-requirement";
        }

        requirementService.updateRequirement(id, buyer, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Requirement updated successfully!");
        return "redirect:/buyer/requirements";
    }

    @PostMapping("/delete-requirement/{id}")
    public String deleteRequirement(@PathVariable("id") Long id,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        User buyer = getAuthenticatedBuyer(userDetails);
        requirementService.deleteRequirement(id, buyer);
        redirectAttributes.addFlashAttribute("successMessage", "Requirement deleted.");
        return "redirect:/buyer/requirements";
    }

    @PostMapping("/toggle-requirement/{id}")
    public String toggleRequirement(@PathVariable("id") Long id,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        User buyer = getAuthenticatedBuyer(userDetails);
        requirementService.toggleStatus(id, buyer);
        redirectAttributes.addFlashAttribute("successMessage", "Requirement status updated.");
        return "redirect:/buyer/requirements";
    }

    @GetMapping("/matched-farmers/{reqId}")
    public String matchedFarmers(@PathVariable("reqId") Long reqId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        User buyer = getAuthenticatedBuyer(userDetails);
        BuyerRequirement req = requirementService.findById(reqId)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found"));

        if (!req.getBuyer().getId().equals(buyer.getId())) {
            return "redirect:/buyer/requirements";
        }

        List<MatchResultDto> matches = matchingService.findMatchesForBuyerRequirement(req);

        model.addAttribute("requirement", req);
        model.addAttribute("matches", matches);
        return "buyer/matched-farmers";
    }

    @GetMapping("/orders")
    public String myOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User buyer = getAuthenticatedBuyer(userDetails);
        model.addAttribute("orders", orderService.getBuyerOrders(buyer));
        return "buyer/orders";
    }

    @GetMapping("/orders/{orderId}")
    public String orderDetails(@PathVariable("orderId") Long orderId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User buyer = getAuthenticatedBuyer(userDetails);
        Order order = orderService.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getBuyer().getId().equals(buyer.getId())) {
            return "redirect:/buyer/orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("farmer", order.getFarmer());
        model.addAttribute("farmerProfile", order.getFarmer().getFarmerProfile());
        model.addAttribute("payment", order.getPaymentRecord());
        model.addAttribute("delivery", order.getDeliveryArrangement());
        return "buyer/order-details";
    }

    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        User buyer = getAuthenticatedBuyer(userDetails);
        Order order = orderService.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.PLACED) {
            orderService.updateOrderStatus(orderId, OrderStatus.CANCELLED, buyer);
            redirectAttributes.addFlashAttribute("successMessage", "Order " + order.getOrderNumber() + " has been cancelled.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Order cannot be cancelled in its current state.");
        }

        return "redirect:/buyer/orders/" + orderId;
    }

    @PostMapping("/orders/{orderId}/payment")
    public String updatePayment(@PathVariable("orderId") Long orderId,
                                @ModelAttribute PaymentUpdateDto dto,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User buyer = getAuthenticatedBuyer(userDetails);
        paymentService.updatePaymentDetails(orderId, dto, buyer);
        redirectAttributes.addFlashAttribute("successMessage", "Payment method updated.");
        return "redirect:/buyer/orders/" + orderId;
    }

    @PostMapping("/orders/{orderId}/delivery")
    public String updateDelivery(@PathVariable("orderId") Long orderId,
                                 @ModelAttribute DeliveryUpdateDto dto,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User buyer = getAuthenticatedBuyer(userDetails);
        deliveryService.updateDelivery(orderId, dto, buyer);
        redirectAttributes.addFlashAttribute("successMessage", "Transportation details updated.");
        return "redirect:/buyer/orders/" + orderId;
    }

    @GetMapping("/saved-listings")
    public String savedListings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User buyer = getAuthenticatedBuyer(userDetails);
        model.addAttribute("savedListings", cropListingService.getSavedListings(buyer));
        return "buyer/saved-listings";
    }

    @PostMapping("/toggle-save/{cropId}")
    public String toggleSaveListing(@PathVariable("cropId") Long cropId,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes,
                                    @RequestHeader(value = "referer", required = false) String referer) {
        User buyer = getAuthenticatedBuyer(userDetails);
        boolean saved = cropListingService.toggleSaveListing(buyer, cropId);
        redirectAttributes.addFlashAttribute("successMessage", saved ? "Listing saved to favorites." : "Listing removed from favorites.");
        return referer != null ? "redirect:" + referer : "redirect:/buyer/saved-listings";
    }

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User buyer = getAuthenticatedBuyer(userDetails);
        model.addAttribute("user", buyer);
        model.addAttribute("profile", buyer.getBuyerProfile());
        return "buyer/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @ModelAttribute UserProfileDto dto,
                                RedirectAttributes redirectAttributes) {
        User buyer = getAuthenticatedBuyer(userDetails);
        userService.updateUserProfile(buyer, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Business profile updated successfully!");
        return "redirect:/buyer/profile";
    }
}
