package com.farmconnect.config;

import com.farmconnect.entity.Role;
import com.farmconnect.entity.User;
import com.farmconnect.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSetupInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSetupInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${farmconnect.admin.initial.create:true}")
    private boolean createInitialAdmin;

    @Value("${farmconnect.admin.initial.name:System Administrator}")
    private String adminName;

    @Value("${farmconnect.admin.initial.email:admin@farmconnect.ai}")
    private String adminEmail;

    @Value("${farmconnect.admin.initial.mobile:9876543210}")
    private String adminMobile;

    @Value("${farmconnect.admin.initial.password:Admin@FarmConnect2026}")
    private String adminPassword;

    public AdminSetupInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Only initializes initial admin account if no admin exists in MySQL
        if (createInitialAdmin && userRepository.countByRole(Role.ROLE_ADMIN) == 0) {
            log.info("Initializing system administrator account ({})", adminEmail);
            User admin = new User();
            admin.setName(adminName);
            admin.setEmail(adminEmail);
            admin.setMobile(adminMobile);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ROLE_ADMIN);
            admin.setAccountStatus(true);
            userRepository.save(admin);
            log.info("System administrator account created successfully. Ready for management.");
        }
    }
}
