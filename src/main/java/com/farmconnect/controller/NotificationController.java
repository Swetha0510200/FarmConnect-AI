package com.farmconnect.controller;

import com.farmconnect.entity.User;
import com.farmconnect.service.NotificationService;
import com.farmconnect.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public String listNotifications(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        model.addAttribute("notifications", notificationService.getUserNotifications(user));
        return "notifications/list";
    }

    @PostMapping("/read/{id}")
    @ResponseBody
    public ResponseEntity<?> markAsRead(@PathVariable("id") Long id,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        notificationService.markAsRead(id, user);
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @PostMapping("/read-all")
    public String markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        notificationService.markAllAsRead(user);
        return "redirect:/notifications";
    }

    @GetMapping("/unread-count")
    @ResponseBody
    public Map<String, Object> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return Collections.singletonMap("unreadCount", 0);
        }
        User user = userService.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return Collections.singletonMap("unreadCount", 0);
        }
        return Map.of(
                "unreadCount", notificationService.getUnreadCount(user),
                "recent", notificationService.getRecentUserNotifications(user)
        );
    }
}
