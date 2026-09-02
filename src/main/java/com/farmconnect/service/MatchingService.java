package com.farmconnect.service;

import com.farmconnect.dto.MatchResultDto;
import com.farmconnect.entity.BuyerRequirement;
import com.farmconnect.entity.CropListing;
import com.farmconnect.entity.ListingStatus;
import com.farmconnect.entity.RequirementStatus;
import com.farmconnect.repository.BuyerRequirementRepository;
import com.farmconnect.repository.CropListingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MatchingService {

    private final BuyerRequirementRepository requirementRepository;
    private final CropListingRepository cropListingRepository;
    private final LocationService locationService;

    public MatchingService(BuyerRequirementRepository requirementRepository,
                           CropListingRepository cropListingRepository,
                           LocationService locationService) {
        this.requirementRepository = requirementRepository;
        this.cropListingRepository = cropListingRepository;
        this.locationService = locationService;
    }

    /**
     * Finds and scores all matching buyers for a given crop listing.
     */
    public List<MatchResultDto> findMatchesForCropListing(CropListing listing) {
        List<BuyerRequirement> openReqs = requirementRepository.findByStatusOrderByIdDesc(RequirementStatus.OPEN);
        List<MatchResultDto> results = new ArrayList<>();

        for (BuyerRequirement req : openReqs) {
            MatchResultDto match = evaluateSuitability(listing, req);
            // Only include matches that have crop similarity and a reasonable suitability score
            if (match.getSuitabilityScore() >= 35) {
                results.add(match);
            }
        }

        results.sort(Comparator.comparingInt(MatchResultDto::getSuitabilityScore).reversed());
        return results;
    }

    /**
     * Finds and scores all matching farmer crop listings for a given buyer requirement.
     */
    public List<MatchResultDto> findMatchesForBuyerRequirement(BuyerRequirement requirement) {
        List<CropListing> activeListings = cropListingRepository.findByStatusOrderByIdDesc(ListingStatus.ACTIVE);
        List<MatchResultDto> results = new ArrayList<>();

        for (CropListing listing : activeListings) {
            MatchResultDto match = evaluateSuitability(listing, requirement);
            if (match.getSuitabilityScore() >= 35) {
                results.add(match);
            }
        }

        results.sort(Comparator.comparingInt(MatchResultDto::getSuitabilityScore).reversed());
        return results;
    }

    /**
     * Evaluates multi-factor compatibility between a crop listing and a buyer requirement.
     */
    public MatchResultDto evaluateSuitability(CropListing listing, BuyerRequirement req) {
        MatchResultDto dto = new MatchResultDto();
        dto.setCropListing(listing);
        dto.setBuyerRequirement(req);

        List<String> reasons = new ArrayList<>();
        int score = 0;

        // 1. Crop Match (Weight: 30%)
        String crop1 = listing.getCropName().trim().toLowerCase();
        String crop2 = req.getCropName().trim().toLowerCase();

        if (crop1.equalsIgnoreCase(crop2)) {
            score += 30;
            reasons.add("Crop matches: " + listing.getCropName());
        } else if (crop1.contains(crop2) || crop2.contains(crop1)) {
            score += 20;
            reasons.add("Crop type compatible: " + listing.getCropName() + " / " + req.getCropName());
        } else {
            // No crop match -> very low score
            dto.setSuitabilityScore(5);
            dto.setReasons(List.of("Crop type does not match (" + listing.getCropName() + " vs " + req.getCropName() + ")"));
            return dto;
        }

        // 2. Quantity Compatibility (Weight: 20%)
        double availQty = listing.getQuantity() != null ? listing.getQuantity() : 0;
        double reqQty = req.getRequiredQuantity() != null ? req.getRequiredQuantity() : 0;

        if (availQty >= reqQty) {
            score += 20;
            reasons.add("Quantity matches (Available " + (int)availQty + " " + listing.getUnit() + " meets " + (int)reqQty + " " + req.getUnit() + " needed)");
        } else if (availQty >= reqQty * 0.7) {
            score += 16;
            reasons.add("Substantial quantity match (" + (int)availQty + " " + listing.getUnit() + " of " + (int)reqQty + " " + req.getUnit() + " needed)");
        } else if (availQty >= reqQty * 0.4) {
            score += 12;
            reasons.add("Partial batch available (" + (int)availQty + " " + listing.getUnit() + " of " + (int)reqQty + " " + req.getUnit() + " needed)");
        } else {
            score += 6;
            reasons.add("Small quantity portion available (" + (int)availQty + " " + listing.getUnit() + ")");
        }

        // 3. Price Compatibility (Weight: 20%)
        double expectedPrice = listing.getExpectedPrice() != null ? listing.getExpectedPrice() : 0;
        double minPrice = req.getMinimumPrice() != null ? req.getMinimumPrice() : 0;
        double maxPrice = req.getMaximumPrice() != null ? req.getMaximumPrice() : 0;

        if (expectedPrice >= minPrice && expectedPrice <= maxPrice) {
            score += 20;
            reasons.add("Price is within buyer's target range (?" + expectedPrice + " in ?" + minPrice + "??" + maxPrice + "/" + listing.getUnit() + ")");
        } else if (expectedPrice < minPrice) {
            score += 20;
            reasons.add("Price is highly competitive for buyer (?" + expectedPrice + " vs minimum ?" + minPrice + "/" + listing.getUnit() + ")");
        } else if (expectedPrice <= maxPrice * 1.15) {
            score += 12;
            reasons.add("Price is slightly above buyer target range (?" + expectedPrice + " vs max ?" + maxPrice + "/" + listing.getUnit() + ")");
        } else {
            score += 4;
            reasons.add("Price difference (?" + expectedPrice + " vs buyer max ?" + maxPrice + "/" + listing.getUnit() + ")");
        }

        // 4. Distance & Geographic Proximity (Weight: 15%)
        double distance = locationService.calculateDistanceBetween(
                listing.getLocation(),
                req.getLocation(),
                listing.getLatitude(),
                listing.getLongitude(),
                req.getLatitude(),
                req.getLongitude()
        );
        dto.setDistanceKm(distance);

        if (distance <= 30.0) {
            score += 15;
            reasons.add("Nearby location (" + distance + " km away in " + (listing.getDistrict() != null ? listing.getDistrict() : "region") + ")");
        } else if (distance <= 75.0) {
            score += 12;
            reasons.add("Regional proximity (" + distance + " km away)");
        } else if (distance <= 150.0) {
            score += 8;
            reasons.add("Inter-district distance (" + distance + " km away)");
        } else {
            score += 4;
            reasons.add("Distance: " + distance + " km away");
        }

        // 5. Availability Date Overlap (Weight: 10%)
        LocalDate listStart = listing.getAvailableFrom();
        LocalDate listEnd = listing.getAvailableUntil();
        LocalDate reqStart = req.getRequiredFrom();
        LocalDate reqEnd = req.getRequiredUntil();

        if (listStart != null && listEnd != null && reqStart != null && reqEnd != null) {
            boolean overlaps = !listStart.isAfter(reqEnd) && !listEnd.isBefore(reqStart);
            if (overlaps) {
                score += 10;
                reasons.add("Availability dates match requirement timeline");
            } else {
                score += 4;
                reasons.add("Dates may require coordination");
            }
        } else {
            score += 6;
        }

        // 6. Quality Grade Compatibility (Weight: 5%)
        String grade = listing.getQualityGrade() != null ? listing.getQualityGrade() : "Grade A";
        String reqQuality = req.getQualityRequirement() != null ? req.getQualityRequirement() : "Any";

        if (reqQuality.equalsIgnoreCase("Any") || reqQuality.equalsIgnoreCase(grade) || grade.toLowerCase().contains(reqQuality.toLowerCase())) {
            score += 5;
            reasons.add("Quality grade matches requirement (" + grade + ")");
        } else {
            score += 2;
        }

        // Cap score at 100
        dto.setSuitabilityScore(Math.min(100, Math.max(10, score)));
        dto.setReasons(reasons);
        dto.setLocationSummary((listing.getDistrict() != null ? listing.getDistrict() : listing.getLocation()) + " to " +
                               (req.getDistrict() != null ? req.getDistrict() : req.getLocation()) + " (" + distance + " km)");

        return dto;
    }
}
