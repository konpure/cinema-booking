package com.cinema.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cinema.annotation.ReadOnly;
import com.cinema.entity.Movie;
import com.cinema.entity.Screening;
import com.cinema.mapper.MovieMapper;
import com.cinema.mapper.ScreeningMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieMapper movieMapper;
    private final ScreeningMapper screeningMapper;

    @ReadOnly
    public List<Movie> listShowing(Long cinemaId) {
        if (cinemaId == null) {
            return listAllShowing();
        }
        List<Long> movieIds = screeningMapper.selectList(new LambdaQueryWrapper<Screening>()
                        .eq(Screening::getCinemaId, cinemaId)
                        .eq(Screening::getStatus, "OPEN")
                        .select(Screening::getMovieId))
                .stream()
                .map(Screening::getMovieId)
                .distinct()
                .toList();
        if (movieIds.isEmpty()) {
            return Collections.emptyList();
        }
        return movieMapper.selectList(new LambdaQueryWrapper<Movie>()
                .in(Movie::getId, movieIds)
                .eq(Movie::getStatus, "SHOWING")
                .orderByDesc(Movie::getRating));
    }

    private List<Movie> listAllShowing() {
        return movieMapper.selectList(new LambdaQueryWrapper<Movie>()
                .eq(Movie::getStatus, "SHOWING")
                .orderByDesc(Movie::getRating));
    }

    @ReadOnly
    public Movie getById(Long id) {
        Movie movie = movieMapper.selectById(id);
        if (movie == null) throw new IllegalArgumentException("影片不存在");
        return movie;
    }

    @ReadOnly
    public List<Movie> listAll() {
        return movieMapper.selectList(new LambdaQueryWrapper<Movie>().orderByDesc(Movie::getCreatedAt));
    }

    public Movie save(Movie movie) {
        if (movie.getStatus() == null) movie.setStatus("SHOWING");
        if (movie.getId() == null) {
            movieMapper.insert(movie);
        } else {
            movieMapper.updateById(movie);
        }
        return movie;
    }

    public void delete(Long id) {
        movieMapper.deleteById(id);
    }
}
