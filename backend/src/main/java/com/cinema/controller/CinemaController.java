package com.cinema.controller;

import com.cinema.dto.ApiResponse;
import com.cinema.entity.Cinema;
import com.cinema.service.CinemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cinemas")
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaService cinemaService;

    @GetMapping
    public ApiResponse<List<Cinema>> list() {
        return ApiResponse.ok(cinemaService.listOpen());
    }

    @GetMapping("/{id}")
    public ApiResponse<Cinema> detail(@PathVariable Long id) {
        return ApiResponse.ok(cinemaService.getById(id));
    }
}
