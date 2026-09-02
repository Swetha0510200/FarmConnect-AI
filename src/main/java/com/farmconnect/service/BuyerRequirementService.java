package com.farmconnect.service;

import com.farmconnect.dto.BuyerRequirementDto;
import com.farmconnect.entity.*;
import com.farmconnect.exception.ResourceNotFoundException;
import com.farmconnect.repository.BuyerProfileRepository;
import com.farmconnect.repository.BuyerRequirementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BuyerRequirementService {

    private final BuyerRequirementRepository requirementRepository;
    private final BuyerProfileRepository buyerProfileRepository;
    private final LocationService locationService;

    public BuyerRequirementService(BuyerRequirementRepository requirementRepository,
                                   BuyerProfileRepository buyerProfileRepository,
                                   LocationService locationService) {
        this.requirementRepository = requirementRepository;
        this.buyerProfileRepository = buyerProfileRepository;
        this.locationService = locationService;
    }

    @Transactional
    public BuyerRequirement createRequirement(User buyer, BuyerRequirementDto dto) {
        BuyerRequirement req = new BuyerRequirement();
        req.setBuyer(buyer);
        req.setCropName(dto.getCropName().trim());
        req.setRequiredQuantity(dto.getRequiredQuantity());
        req.setUnit(dto.getUnit());
        req.setMinimumPrice(dto.getMinimumPrice());
        req.setMaximumPrice(dto.getMaximumPrice());
        req.setRequiredFrom(dto.getRequiredFrom());
        req.setRequiredUntil(dto.getRequiredUntil());
        req.setLocation(dto.getLocation());
        req.setQualityRequirement(dto.getQualityRequirement());
        req.setDescription(dto.getDescription());
        req.setStatus(RequirementStatus.OPEN);

        Optional<BuyerProfile> bp = buyerProfileRepository.findByUser(buyer);
        if (bp.isPresent()) {
            req.setDistrict(dto.getDistrict() != null ? dto.getDistrict() : bp.get().getDistrict());
            req.setState(dto.getState() != null ? dto.getState() : bp.get().getState());
            req.setLatitude(bp.get().getLatitude());
            req.setLongitude(bp.get().getLongitude());
        } else {
            req.setDistrict(dto.getDistrict());
            req.setState(dto.getState());
            double[] coords = locationService.getCoordinates(dto.getLocation());
            req.setLatitude(coords[0]);
            req.setLongitude(coords[1]);
        }

        return requirementRepository.save(req);
    }

    @Transactional
    public BuyerRequirement updateRequirement(Long id, User buyer, BuyerRequirementDto dto) {
        BuyerRequirement req = requirementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found with ID " + id));

        if (!req.getBuyer().getId().equals(buyer.getId())) {
            throw new IllegalArgumentException("You are not authorized to modify this requirement.");
        }

        req.setCropName(dto.getCropName().trim());
        req.setRequiredQuantity(dto.getRequiredQuantity());
        req.setUnit(dto.getUnit());
        req.setMinimumPrice(dto.getMinimumPrice());
        req.setMaximumPrice(dto.getMaximumPrice());
        req.setRequiredFrom(dto.getRequiredFrom());
        req.setRequiredUntil(dto.getRequiredUntil());
        req.setLocation(dto.getLocation());
        req.setQualityRequirement(dto.getQualityRequirement());
        req.setDescription(dto.getDescription());
        if (dto.getDistrict() != null) req.setDistrict(dto.getDistrict());
        if (dto.getState() != null) req.setState(dto.getState());

        return requirementRepository.save(req);
    }

    @Transactional
    public void deleteRequirement(Long id, User buyer) {
        BuyerRequirement req = requirementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found with ID " + id));

        if (!req.getBuyer().getId().equals(buyer.getId()) && buyer.getRole() != Role.ROLE_ADMIN) {
            throw new IllegalArgumentException("You are not authorized to delete this requirement.");
        }

        requirementRepository.delete(req);
    }

    @Transactional
    public void toggleStatus(Long id, User buyer) {
        BuyerRequirement req = requirementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found with ID " + id));

        if (!req.getBuyer().getId().equals(buyer.getId()) && buyer.getRole() != Role.ROLE_ADMIN) {
            throw new IllegalArgumentException("You are not authorized to modify this requirement.");
        }

        if (req.getStatus() == RequirementStatus.OPEN) {
            req.setStatus(RequirementStatus.CLOSED);
        } else {
            req.setStatus(RequirementStatus.OPEN);
        }
        requirementRepository.save(req);
    }

    public List<BuyerRequirement> getBuyerRequirements(User buyer) {
        return requirementRepository.findByBuyerOrderByIdDesc(buyer);
    }

    public List<BuyerRequirement> getOpenRequirements() {
        return requirementRepository.findByStatusOrderByIdDesc(RequirementStatus.OPEN);
    }

    public Optional<BuyerRequirement> findById(Long id) {
        return requirementRepository.findById(id);
    }

    public long countBuyerOpenRequirements(User buyer) {
        return requirementRepository.countByBuyerAndStatus(buyer, RequirementStatus.OPEN);
    }

    public long countAllOpenRequirements() {
        return requirementRepository.countByStatus(RequirementStatus.OPEN);
    }

    public List<BuyerRequirement> getAllRequirements() {
        return requirementRepository.findAll();
    }
}
