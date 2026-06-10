package com.cinema.controller;

import com.cinema.dto.ApiResponse;
import com.cinema.entity.Movie;
import com.cinema.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ApiResponse<List<Movie>> list(@RequestParam(required = false) Long cinemaId) {
        return ApiResponse.ok(movieService.listShowing(cinemaId));
    }

    @GetMapping("/{id}")
    public ApiResponse<Movie> detail(@PathVariable Long id) {
        return ApiResponse.ok(movieService.getById(id));
    }
}
