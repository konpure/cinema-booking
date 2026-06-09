package com.cinema.controller;

import com.cinema.dto.ApiResponse;
import com.cinema.entity.Cinema;
import com.cinema.entity.Movie;
import com.cinema.entity.Screening;
import com.cinema.entity.Snack;
import com.cinema.service.CinemaService;
import com.cinema.service.MovieService;
import com.cinema.service.OrderService;
import com.cinema.service.ScreeningService;
import com.cinema.service.SnackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MovieService movieService;
    private final ScreeningService screeningService;
    private final OrderService orderService;
    private final SnackService snackService;
    private final CinemaService cinemaService;

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        return ApiResponse.ok(orderService.dashboardStats());
    }

    @GetMapping("/orders")
    public ApiResponse<List<Map<String, Object>>> orders() {
        return ApiResponse.ok(orderService.listAllOrders());
    }

    @GetMapping("/movies")
    public ApiResponse<List<Movie>> movies() {
        return ApiResponse.ok(movieService.listAll());
    }

    @PostMapping("/movies")
    public ApiResponse<Movie> saveMovie(@RequestBody Movie movie) {
        if (movie.getTitle() == null || movie.getTitle().isBlank()) {
            throw new IllegalArgumentException("片名不能为空");
        }
        return ApiResponse.ok(movieService.save(movie));
    }

    @DeleteMapping("/movies/{id}")
    public ApiResponse<Void> deleteMovie(@PathVariable Long id) {
        movieService.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/screenings")
    public ApiResponse<List<Screening>> screenings() {
        return ApiResponse.ok(screeningService.listAll());
    }

    @PostMapping("/screenings")
    public ApiResponse<Screening> saveScreening(@RequestBody Screening screening) {
        if (screening.getMovieId() == null) {
            throw new IllegalArgumentException("请选择影片");
        }
        if (screening.getCinemaId() == null) {
            throw new IllegalArgumentException("请选择影城");
        }
        return ApiResponse.ok(screeningService.save(screening));
    }

    @DeleteMapping("/screenings/{id}")
    public ApiResponse<Void> deleteScreening(@PathVariable Long id) {
        screeningService.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/snacks")
    public ApiResponse<List<Snack>> snacks() {
        return ApiResponse.ok(snackService.listAll());
    }

    @PostMapping("/snacks")
    public ApiResponse<Snack> saveSnack(@RequestBody Snack snack) {
        if (snack.getName() == null || snack.getName().isBlank()) {
            throw new IllegalArgumentException("卖品名称不能为空");
        }
        return ApiResponse.ok(snackService.save(snack));
    }

    @DeleteMapping("/snacks/{id}")
    public ApiResponse<Void> deleteSnack(@PathVariable Long id) {
        snackService.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/cinemas")
    public ApiResponse<List<Cinema>> cinemas() {
        return ApiResponse.ok(cinemaService.listAll());
    }

    @PostMapping("/cinemas")
    public ApiResponse<Cinema> saveCinema(@RequestBody Cinema cinema) {
        if (cinema.getName() == null || cinema.getName().isBlank()) {
            throw new IllegalArgumentException("影城名称不能为空");
        }
        return ApiResponse.ok(cinemaService.save(cinema));
    }
}
