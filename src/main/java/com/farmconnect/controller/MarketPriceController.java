package com.farmconnect.controller;

import com.farmconnect.entity.MarketPrice;
import com.farmconnect.service.MarketPriceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/market")
public class MarketPriceController {

    private final MarketPriceService marketPriceService;

    public MarketPriceController(MarketPriceService marketPriceService) {
        this.marketPriceService = marketPriceService;
    }

    @GetMapping("/prices")
    public String marketPrices(@RequestParam(value = "crop", required = false) String crop,
                               @RequestParam(value = "district", required = false) String district,
                               @RequestParam(value = "state", required = false) String state,
                               Model model) {
        List<MarketPrice> prices;
        if ((crop != null && !crop.isBlank()) || (district != null && !district.isBlank()) || (state != null && !state.isBlank())) {
            prices = marketPriceService.searchMarketPrices(crop, district, state);
        } else {
            prices = marketPriceService.getAllMarketPrices();
        }

        model.addAttribute("marketPrices", prices);
        model.addAttribute("selectedCrop", crop);
        model.addAttribute("selectedDistrict", district);
        model.addAttribute("selectedState", state);
        return "market/prices";
    }

    @PostMapping("/retry")
    public String retryFetch(RedirectAttributes redirectAttributes) {
        boolean refreshed = marketPriceService.refreshFromExternalSource();
        if (refreshed) {
            redirectAttributes.addFlashAttribute("successMessage", "Market prices successfully refreshed from official government data feed.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Live external API is currently unavailable or unconfigured. Displaying locally verified market records.");
        }
        return "redirect:/market/prices";
    }

    @GetMapping("/compare")
    @ResponseBody
    public Map<String, Object> comparePrice(@RequestParam("crop") String crop,
                                            @RequestParam("price") Double expectedPrice) {
        return marketPriceService.compareWithMarketPrice(crop, expectedPrice);
    }
}
