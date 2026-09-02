package com.farmconnect.config;

import com.farmconnect.entity.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if (role.equals(Role.ROLE_ADMIN.name())) {
                response.sendRedirect("/admin/dashboard");
                return;
            } else if (role.equals(Role.ROLE_BUYER.name())) {
                response.sendRedirect("/buyer/dashboard");
                return;
            } else if (role.equals(Role.ROLE_FARMER.name())) {
                response.sendRedirect("/farmer/dashboard");
                return;
            }
        }
        response.sendRedirect("/");
    }
}
