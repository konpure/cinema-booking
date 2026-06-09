package com.cinema.controller;

import com.cinema.dto.ApiResponse;
import com.cinema.entity.Snack;
import com.cinema.service.SnackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/snacks")
@RequiredArgsConstructor
public class SnackController {

    private final SnackService snackService;

    @GetMapping
    public ApiResponse<List<Snack>> list() {
        return ApiResponse.ok(snackService.listOnSale());
    }
}
