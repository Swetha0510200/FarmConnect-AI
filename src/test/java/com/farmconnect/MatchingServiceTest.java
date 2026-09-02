package com.farmconnect;

import com.farmconnect.dto.MatchResultDto;
import com.farmconnect.entity.BuyerRequirement;
import com.farmconnect.entity.CropListing;
import com.farmconnect.entity.User;
import com.farmconnect.repository.BuyerRequirementRepository;
import com.farmconnect.repository.CropListingRepository;
import com.farmconnect.service.LocationService;
import com.farmconnect.service.MatchingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MatchingServiceTest {

    private MatchingService matchingService;
    private BuyerRequirementRepository requirementRepository;
    private CropListingRepository cropListingRepository;
    private LocationService locationService;

    @BeforeEach
    void setUp() {
        requirementRepository = Mockito.mock(BuyerRequirementRepository.class);
        cropListingRepository = Mockito.mock(CropListingRepository.class);
        locationService = new LocationService();
        matchingService = new MatchingService(requirementRepository, cropListingRepository, locationService);
    }

    @Test
    void testExactCropMatchSuitability() {
        User farmer = new User();
        farmer.setName("Farmer Ramesh");

        CropListing listing = new CropListing();
        listing.setFarmer(farmer);
        listing.setCropName("Tomato");
        listing.setQuantity(500.0);
        listing.setUnit("kg");
        listing.setExpectedPrice(24.0);
        listing.setQualityGrade("Grade A");
        listing.setLocation("Tiruvallur");
        listing.setDistrict("Tiruvallur");
        listing.setAvailableFrom(LocalDate.now());
        listing.setAvailableUntil(LocalDate.now().plusDays(15));

        User buyer = new User();
        buyer.setName("Buyer Kumar");

        BuyerRequirement req = new BuyerRequirement();
        req.setBuyer(buyer);
        req.setCropName("Tomato");
        req.setRequiredQuantity(500.0);
        req.setUnit("kg");
        req.setMinimumPrice(22.0);
        req.setMaximumPrice(26.0);
        req.setQualityRequirement("Grade A");
        req.setLocation("Chennai");
        req.setDistrict("Chennai");
        req.setRequiredFrom(LocalDate.now());
        req.setRequiredUntil(LocalDate.now().plusDays(10));

        MatchResultDto result = matchingService.evaluateSuitability(listing, req);

        assertNotNull(result);
        assertTrue(result.getSuitabilityScore() >= 80, "Expected suitability score >= 80% for close match, got: " + result.getSuitabilityScore());
        assertFalse(result.getReasons().isEmpty(), "Expected clear match reasons");
    }

    @Test
    void testMismatchedCropReturnsLowScore() {
        CropListing listing = new CropListing();
        listing.setCropName("Tomato");
        listing.setQuantity(100.0);
        listing.setExpectedPrice(30.0);
        listing.setLocation("Salem");

        BuyerRequirement req = new BuyerRequirement();
        req.setCropName("Wheat");
        req.setRequiredQuantity(1000.0);
        req.setMinimumPrice(20.0);
        req.setMaximumPrice(25.0);
        req.setLocation("Chennai");

        MatchResultDto result = matchingService.evaluateSuitability(listing, req);

        assertNotNull(result);
        assertTrue(result.getSuitabilityScore() < 30, "Expected low score for mismatched crop");
    }
}
