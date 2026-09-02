package com.farmconnect.service;

import com.farmconnect.dto.CropListingDto;
import com.farmconnect.entity.*;
import com.farmconnect.exception.ResourceNotFoundException;
import com.farmconnect.repository.CropListingRepository;
import com.farmconnect.repository.FarmerProfileRepository;
import com.farmconnect.repository.SavedListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CropListingService {

    private final CropListingRepository cropListingRepository;
    private final FarmerProfileRepository farmerProfileRepository;
    private final LocationService locationService;
    private final SavedListingRepository savedListingRepository;

    public CropListingService(CropListingRepository cropListingRepository,
                              FarmerProfileRepository farmerProfileRepository,
                              LocationService locationService,
                              SavedListingRepository savedListingRepository) {
        this.cropListingRepository = cropListingRepository;
        this.farmerProfileRepository = farmerProfileRepository;
        this.locationService = locationService;
        this.savedListingRepository = savedListingRepository;
    }

    @Transactional
    public CropListing createListing(User farmer, CropListingDto dto) {
        CropListing listing = new CropListing();
        listing.setFarmer(farmer);
        listing.setCropName(dto.getCropName().trim());
        listing.setQuantity(dto.getQuantity());
        listing.setUnit(dto.getUnit());
        listing.setExpectedPrice(dto.getExpectedPrice());
        listing.setQualityGrade(dto.getQualityGrade());
        listing.setAvailableFrom(dto.getAvailableFrom());
        listing.setAvailableUntil(dto.getAvailableUntil());
        listing.setLocation(dto.getLocation());
        listing.setDescription(dto.getDescription());
        listing.setStatus(ListingStatus.ACTIVE);

        // Derive district and state from farmer profile if not explicitly set
        Optional<FarmerProfile> fp = farmerProfileRepository.findByUser(farmer);
        if (fp.isPresent()) {
            listing.setDistrict(dto.getDistrict() != null ? dto.getDistrict() : fp.get().getDistrict());
            listing.setState(dto.getState() != null ? dto.getState() : fp.get().getState());
            listing.setLatitude(fp.get().getLatitude());
            listing.setLongitude(fp.get().getLongitude());
        } else {
            listing.setDistrict(dto.getDistrict());
            listing.setState(dto.getState());
            double[] coords = locationService.getCoordinates(dto.getLocation());
            listing.setLatitude(coords[0]);
            listing.setLongitude(coords[1]);
        }

        return cropListingRepository.save(listing);
    }

    @Transactional
    public CropListing updateListing(Long listingId, User farmer, CropListingDto dto) {
        CropListing listing = cropListingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Crop listing not found with ID " + listingId));

        if (!listing.getFarmer().getId().equals(farmer.getId())) {
            throw new IllegalArgumentException("You are not authorized to edit this listing.");
        }

        listing.setCropName(dto.getCropName().trim());
        listing.setQuantity(dto.getQuantity());
        listing.setUnit(dto.getUnit());
        listing.setExpectedPrice(dto.getExpectedPrice());
        listing.setQualityGrade(dto.getQualityGrade());
        listing.setAvailableFrom(dto.getAvailableFrom());
        listing.setAvailableUntil(dto.getAvailableUntil());
        listing.setLocation(dto.getLocation());
        listing.setDescription(dto.getDescription());
        if (dto.getDistrict() != null && !dto.getDistrict().isBlank()) {
            listing.setDistrict(dto.getDistrict());
        }
        if (dto.getState() != null && !dto.getState().isBlank()) {
            listing.setState(dto.getState());
        }

        return cropListingRepository.save(listing);
    }

    @Transactional
    public void deleteListing(Long listingId, User farmer) {
        CropListing listing = cropListingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Crop listing not found with ID " + listingId));

        if (!listing.getFarmer().getId().equals(farmer.getId()) && farmer.getRole() != Role.ROLE_ADMIN) {
            throw new IllegalArgumentException("You are not authorized to delete this listing.");
        }

        cropListingRepository.delete(listing);
    }

    @Transactional
    public void toggleStatus(Long listingId, User farmer) {
        CropListing listing = cropListingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Crop listing not found with ID " + listingId));

        if (!listing.getFarmer().getId().equals(farmer.getId()) && farmer.getRole() != Role.ROLE_ADMIN) {
            throw new IllegalArgumentException("You are not authorized to modify this listing.");
        }

        if (listing.getStatus() == ListingStatus.ACTIVE) {
            listing.setStatus(ListingStatus.INACTIVE);
        } else {
            listing.setStatus(ListingStatus.ACTIVE);
        }
        cropListingRepository.save(listing);
    }

    public List<CropListing> getFarmerListings(User farmer) {
        return cropListingRepository.findByFarmerOrderByIdDesc(farmer);
    }

    public List<CropListing> getActiveListings() {
        return cropListingRepository.findByStatusOrderByIdDesc(ListingStatus.ACTIVE);
    }

    public List<CropListing> searchListings(String cropName, String district, Double minPrice, Double maxPrice, Double minQuantity, String qualityGrade) {
        return cropListingRepository.searchActiveListings(cropName, district, minPrice, maxPrice, minQuantity, qualityGrade);
    }

    public Optional<CropListing> findById(Long id) {
        return cropListingRepository.findById(id);
    }

    public long countFarmerActiveListings(User farmer) {
        return cropListingRepository.countByFarmerAndStatus(farmer, ListingStatus.ACTIVE);
    }

    public long countAllActiveListings() {
        return cropListingRepository.countByStatus(ListingStatus.ACTIVE);
    }

    public List<CropListing> getAllListings() {
        return cropListingRepository.findAll();
    }

    @Transactional
    public boolean toggleSaveListing(User user, Long listingId) {
        CropListing listing = cropListingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (savedListingRepository.existsByUserAndCropListing(user, listing)) {
            savedListingRepository.deleteByUserAndCropListing(user, listing);
            return false;
        } else {
            savedListingRepository.save(new SavedListing(user, listing));
            return true;
        }
    }

    public boolean isListingSaved(User user, CropListing listing) {
        return savedListingRepository.existsByUserAndCropListing(user, listing);
    }

    public List<SavedListing> getSavedListings(User user) {
        return savedListingRepository.findByUserOrderByCreatedAtDesc(user);
    }
}
