package com.farmconnect.service;

import com.farmconnect.dto.BuyerRegistrationDto;
import com.farmconnect.dto.FarmerRegistrationDto;
import com.farmconnect.dto.UserProfileDto;
import com.farmconnect.entity.*;
import com.farmconnect.exception.ResourceNotFoundException;
import com.farmconnect.repository.BuyerProfileRepository;
import com.farmconnect.repository.FarmerProfileRepository;
import com.farmconnect.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FarmerProfileRepository farmerProfileRepository;
    private final BuyerProfileRepository buyerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final LocationService locationService;
    private final NotificationService notificationService;

    public UserService(UserRepository userRepository,
                       FarmerProfileRepository farmerProfileRepository,
                       BuyerProfileRepository buyerProfileRepository,
                       PasswordEncoder passwordEncoder,
                       LocationService locationService,
                       NotificationService notificationService) {
        this.userRepository = userRepository;
        this.farmerProfileRepository = farmerProfileRepository;
        this.buyerProfileRepository = buyerProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.locationService = locationService;
        this.notificationService = notificationService;
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean mobileExists(String mobile) {
        return userRepository.existsByMobile(mobile);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User registerFarmer(FarmerRegistrationDto dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setMobile(dto.getMobile());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.ROLE_FARMER);
        user.setAccountStatus(true);

        User savedUser = userRepository.save(user);

        FarmerProfile profile = new FarmerProfile();
        profile.setUser(savedUser);
        profile.setFarmName(dto.getFarmName());
        profile.setFarmSize(dto.getFarmSize());
        profile.setVillage(dto.getVillage());
        profile.setDistrict(dto.getDistrict());
        profile.setState(dto.getState());
        profile.setFarmingType(dto.getFarmingType());

        double[] coords = locationService.getCoordinates(dto.getDistrict());
        profile.setLatitude(dto.getLatitude() != null ? dto.getLatitude() : coords[0]);
        profile.setLongitude(dto.getLongitude() != null ? dto.getLongitude() : coords[1]);

        farmerProfileRepository.save(profile);

        notificationService.createNotification(
                savedUser,
                "Welcome to FarmConnect AI!",
                "Your farmer account has been created successfully. You can now add your crops to find buyers.",
                "SYSTEM",
                "/farmer/add-crop"
        );

        return savedUser;
    }

    @Transactional
    public User registerBuyer(BuyerRegistrationDto dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setMobile(dto.getMobile());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.ROLE_BUYER);
        user.setAccountStatus(true);

        User savedUser = userRepository.save(user);

        BuyerProfile profile = new BuyerProfile();
        profile.setUser(savedUser);
        profile.setBusinessName(dto.getBusinessName());
        profile.setBusinessType(dto.getBusinessType());
        profile.setAddress(dto.getAddress());
        profile.setDistrict(dto.getDistrict());
        profile.setState(dto.getState());
        profile.setVerificationStatus(false); // pending admin verification

        double[] coords = locationService.getCoordinates(dto.getDistrict());
        profile.setLatitude(dto.getLatitude() != null ? dto.getLatitude() : coords[0]);
        profile.setLongitude(dto.getLongitude() != null ? dto.getLongitude() : coords[1]);

        buyerProfileRepository.save(profile);

        notificationService.createNotification(
                savedUser,
                "Welcome to FarmConnect AI!",
                "Your buyer account has been registered. You can search farmer crops and post your requirements.",
                "SYSTEM",
                "/buyer/dashboard"
        );

        return savedUser;
    }

    @Transactional
    public void updateUserProfile(User user, UserProfileDto dto) {
        user.setName(dto.getName());
        user.setMobile(dto.getMobile());
        userRepository.save(user);

        if (user.getRole() == Role.ROLE_FARMER) {
            FarmerProfile fp = farmerProfileRepository.findByUser(user)
                    .orElseGet(() -> {
                        FarmerProfile p = new FarmerProfile();
                        p.setUser(user);
                        return p;
                    });
            fp.setFarmName(dto.getFarmOrBusinessName());
            if (dto.getFarmSizeOrBusinessType() != null && !dto.getFarmSizeOrBusinessType().isBlank()) {
                try {
                    fp.setFarmSize(Double.parseDouble(dto.getFarmSizeOrBusinessType()));
                } catch (NumberFormatException ignored) {}
            }
            fp.setVillage(dto.getAddressOrVillage());
            fp.setDistrict(dto.getDistrict());
            fp.setState(dto.getState());
            fp.setFarmingType(dto.getFarmingType());
            farmerProfileRepository.save(fp);
        } else if (user.getRole() == Role.ROLE_BUYER) {
            BuyerProfile bp = buyerProfileRepository.findByUser(user)
                    .orElseGet(() -> {
                        BuyerProfile p = new BuyerProfile();
                        p.setUser(user);
                        return p;
                    });
            bp.setBusinessName(dto.getFarmOrBusinessName());
            bp.setBusinessType(dto.getFarmSizeOrBusinessType());
            bp.setAddress(dto.getAddressOrVillage());
            bp.setDistrict(dto.getDistrict());
            bp.setState(dto.getState());
            buyerProfileRepository.save(bp);
        }
    }

    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID " + userId));
        user.setAccountStatus(!user.isAccountStatus());
        userRepository.save(user);
    }

    @Transactional
    public void verifyBuyer(Long buyerProfileId) {
        BuyerProfile profile = buyerProfileRepository.findById(buyerProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer profile not found with ID " + buyerProfileId));
        profile.setVerificationStatus(true);
        buyerProfileRepository.save(profile);

        notificationService.createNotification(
                profile.getUser(),
                "Account Verified",
                "Your business account has been verified by the administrator. Farmers will now see your verified badge.",
                "SYSTEM",
                "/buyer/profile"
        );
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getFarmers() {
        return userRepository.findByRoleOrderByIdDesc(Role.ROLE_FARMER);
    }

    public List<User> getBuyers() {
        return userRepository.findByRoleOrderByIdDesc(Role.ROLE_BUYER);
    }

    public long countFarmers() {
        return userRepository.countByRole(Role.ROLE_FARMER);
    }

    public long countBuyers() {
        return userRepository.countByRole(Role.ROLE_BUYER);
    }

    public Optional<FarmerProfile> getFarmerProfile(User user) {
        return farmerProfileRepository.findByUser(user);
    }

    public Optional<BuyerProfile> getBuyerProfile(User user) {
        return buyerProfileRepository.findByUser(user);
    }
}
