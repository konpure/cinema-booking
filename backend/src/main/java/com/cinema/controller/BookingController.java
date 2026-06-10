package com.cinema.controller;

import com.cinema.dto.*;
import com.cinema.entity.User;
import com.cinema.service.AuthService;
import com.cinema.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final AuthService authService;

    @GetMapping("/{screeningId}/seats")
    public ApiResponse<SeatMapResponse> seats(@PathVariable Long screeningId, Authentication auth) {
        Long userId = auth != null ? requireUser(auth).getId() : null;
        return ApiResponse.ok(bookingService.getSeatMap(screeningId, userId));
    }

    @PostMapping("/lock")
    public ApiResponse<Map<String, String>> lock(@Valid @RequestBody LockSeatsRequest request, Authentication auth) {
        User user = requireUser(auth);
        return ApiResponse.ok(bookingService.lockSeats(user.getId(), request));
    }

    @PostMapping("/submit")
    public ApiResponse<Map<String, String>> submit(@Valid @RequestBody SubmitOrderRequest request, Authentication auth) {
        User user = requireUser(auth);
        LockSeatsRequest lockRequest = new LockSeatsRequest();
        lockRequest.setScreeningId(request.getScreeningId());
        lockRequest.setSeats(request.getSeats());
        return ApiResponse.ok(bookingService.submitOrder(
                user.getId(), lockRequest, request.getLockToken(), request.getSnacks()));
    }

    @PostMapping("/release")
    public ApiResponse<Void> release(@Valid @RequestBody SubmitOrderRequest request, Authentication auth) {
        User user = requireUser(auth);
        bookingService.releaseLock(user.getId(), request.getLockToken(), request.getScreeningId(), request.getSeats());
        return ApiResponse.ok(null);
    }

    private User requireUser(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new IllegalArgumentException("请先登录");
        }
        User user = authService.findByUsername(auth.getName());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }
}
