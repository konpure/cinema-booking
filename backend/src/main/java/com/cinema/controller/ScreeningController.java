package com.cinema.controller;

import com.cinema.dto.ApiResponse;
import com.cinema.entity.Screening;
import com.cinema.service.ScreeningService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screenings")
@RequiredArgsConstructor
public class ScreeningController {

    private final ScreeningService screeningService;

    @GetMapping
    public ApiResponse<List<Screening>> list(@RequestParam Long movieId,
                                             @RequestParam(required = false) Long cinemaId) {
        return ApiResponse.ok(screeningService.listByMovie(movieId, cinemaId));
    }

    @GetMapping("/{id}")
    public ApiResponse<Screening> detail(@PathVariable Long id) {
        return ApiResponse.ok(screeningService.getById(id));
    }
}
