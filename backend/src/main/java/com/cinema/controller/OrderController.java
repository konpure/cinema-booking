package com.cinema.controller;

import com.cinema.dto.ApiResponse;
import com.cinema.entity.User;
import com.cinema.service.AuthService;
import com.cinema.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final AuthService authService;

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> myOrders(Authentication auth) {
        User user = authService.findByUsername(auth.getName());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return ApiResponse.ok(orderService.listUserOrders(user.getId()));
    }
}
