package com.farmconnect.service;

import com.farmconnect.dto.DemandInsightDto;
import com.farmconnect.entity.BuyerRequirement;
import com.farmconnect.entity.CropListing;
import com.farmconnect.entity.ListingStatus;
import com.farmconnect.entity.RequirementStatus;
import com.farmconnect.repository.BuyerRequirementRepository;
import com.farmconnect.repository.CropListingRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DemandInsightsService {

    private final BuyerRequirementRepository requirementRepository;
    private final CropListingRepository cropListingRepository;

    public DemandInsightsService(BuyerRequirementRepository requirementRepository,
                                 CropListingRepository cropListingRepository) {
        this.requirementRepository = requirementRepository;
        this.cropListingRepository = cropListingRepository;
    }

    /**
     * Aggregates real active buyer demand vs listed farmer supply from the MySQL database.
     * Computes demand ratios without fabricating fake numbers.
     */
    public List<DemandInsightDto> calculateDemandInsights() {
        List<BuyerRequirement> openReqs = requirementRepository.findByStatusOrderByIdDesc(RequirementStatus.OPEN);
        List<CropListing> activeListings = cropListingRepository.findByStatusOrderByIdDesc(ListingStatus.ACTIVE);

        Map<String, List<BuyerRequirement>> cropToReqs = new HashMap<>();
        Map<String, List<CropListing>> cropToListings = new HashMap<>();
        Set<String> allCrops = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        for (BuyerRequirement r : openReqs) {
            String crop = capitalize(r.getCropName().trim());
            allCrops.add(crop);
            cropToReqs.computeIfAbsent(crop, k -> new ArrayList<>()).add(r);
        }

        for (CropListing l : activeListings) {
            String crop = capitalize(l.getCropName().trim());
            allCrops.add(crop);
            cropToListings.computeIfAbsent(crop, k -> new ArrayList<>()).add(l);
        }

        List<DemandInsightDto> insights = new ArrayList<>();

        for (String crop : allCrops) {
            List<BuyerRequirement> reqs = cropToReqs.getOrDefault(crop, Collections.emptyList());
            List<CropListing> listings = cropToListings.getOrDefault(crop, Collections.emptyList());

            double totalDemand = reqs.stream().mapToDouble(BuyerRequirement::getRequiredQuantity).sum();
            double totalSupply = listings.stream().mapToDouble(CropListing::getQuantity).sum();

            double avgBuyerMin = reqs.isEmpty() ? 0.0 : reqs.stream().mapToDouble(BuyerRequirement::getMinimumPrice).average().orElse(0.0);
            double avgBuyerMax = reqs.isEmpty() ? 0.0 : reqs.stream().mapToDouble(BuyerRequirement::getMaximumPrice).average().orElse(0.0);
            double avgFarmerPrice = listings.isEmpty() ? 0.0 : listings.stream().mapToDouble(CropListing::getExpectedPrice).average().orElse(0.0);

            DemandInsightDto dto = new DemandInsightDto();
            dto.setCropName(crop);
            dto.setTotalDemandQuantity(Math.round(totalDemand * 10.0) / 10.0);
            dto.setTotalSupplyQuantity(Math.round(totalSupply * 10.0) / 10.0);
            dto.setUnit(listings.isEmpty() ? (reqs.isEmpty() ? "kg" : reqs.get(0).getUnit()) : listings.get(0).getUnit());
            dto.setAverageBuyerMinPrice(Math.round(avgBuyerMin * 10.0) / 10.0);
            dto.setAverageBuyerMaxPrice(Math.round(avgBuyerMax * 10.0) / 10.0);
            dto.setAverageFarmerPrice(Math.round(avgFarmerPrice * 10.0) / 10.0);

            if (totalSupply == 0 && totalDemand > 0) {
                dto.setDemandLevel("Very High Demand");
                dto.setExplanation("Active buyer requirements exist with 0 farmer listings currently available.");
            } else if (totalDemand >= totalSupply * 1.5) {
                dto.setDemandLevel("High Demand");
                dto.setExplanation("Buyer requirements (" + (int)totalDemand + " " + dto.getUnit() + ") exceed listed farmer supply (" + (int)totalSupply + " " + dto.getUnit() + ").");
            } else if (totalDemand >= totalSupply * 0.8) {
                dto.setDemandLevel("Balanced Market");
                dto.setExplanation("Buyer demand and farmer supply are closely matched.");
            } else if (totalDemand > 0) {
                dto.setDemandLevel("Moderate Demand");
                dto.setExplanation("Farmer supply exceeds current open buyer requirements.");
            } else {
                dto.setDemandLevel("Low Buyer Activity");
                dto.setExplanation("No active buyer requirements posted yet for this crop.");
            }

            insights.add(dto);
        }

        return insights;
    }

    private String capitalize(String text) {
        if (text == null || text.isBlank()) return "";
        return Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase();
    }
}
