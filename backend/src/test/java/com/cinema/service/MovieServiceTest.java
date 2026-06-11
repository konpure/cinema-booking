package com.cinema.service;

import com.cinema.entity.Movie;
import com.cinema.entity.Screening;
import com.cinema.mapper.MovieMapper;
import com.cinema.mapper.ScreeningMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @BeforeAll
    static void initMybatisLambdaCache() {
        // 手动初始化实体元数据以填充 COLUMN_CACHE_MAP。
        var config = new org.apache.ibatis.session.Configuration();
        config.setMapUnderscoreToCamelCase(true);
        var assistant = new org.apache.ibatis.builder.MapperBuilderAssistant(config, "");
        com.baomidou.mybatisplus.core.metadata.TableInfoHelper.initTableInfo(
                assistant, com.cinema.entity.Screening.class);
        com.baomidou.mybatisplus.core.metadata.TableInfoHelper.initTableInfo(
                assistant, com.cinema.entity.Movie.class);
    }

    @Mock
    private MovieMapper movieMapper;

    @Mock
    private ScreeningMapper screeningMapper;

    @InjectMocks
    private MovieService movieService;

    // ==================== listShowing (no cinemaId) ====================

    @Test
    void listShowingWithoutCinemaIdReturnsAllShowing() {
        Movie m1 = new Movie();
        m1.setId(1L);
        m1.setTitle("流浪地球");
        m1.setStatus("SHOWING");

        when(movieMapper.selectList(any())).thenReturn(List.of(m1));

        List<Movie> result = movieService.listShowing(null);
        assertEquals(1, result.size());
        assertEquals("流浪地球", result.get(0).getTitle());
    }

    @Test
    void listShowingWithCinemaIdReturnsFilteredMovies() {
        Screening s = new Screening();
        s.setMovieId(1L);

        Movie m = new Movie();
        m.setId(1L);
        m.setTitle("流浪地球");
        m.setStatus("SHOWING");

        when(screeningMapper.selectList(any())).thenReturn(List.of(s));
        when(movieMapper.selectList(any())).thenReturn(List.of(m));

        List<Movie> result = movieService.listShowing(1L);
        assertEquals(1, result.size());
        assertEquals("流浪地球", result.get(0).getTitle());
    }

    @Test
    void listShowingWithCinemaIdReturnsEmptyWhenNoScreenings() {
        when(screeningMapper.selectList(any())).thenReturn(List.of());

        List<Movie> result = movieService.listShowing(1L);
        assertTrue(result.isEmpty());
        verify(movieMapper, never()).selectList(any());
    }

    // ==================== getById ====================

    @Test
    void getByIdReturnsMovie() {
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("流浪地球");

        when(movieMapper.selectById(1L)).thenReturn(movie);

        Movie result = movieService.getById(1L);
        assertNotNull(result);
        assertEquals("流浪地球", result.getTitle());
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        when(movieMapper.selectById(999L)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> movieService.getById(999L));
        assertEquals("影片不存在", ex.getMessage());
    }

    // ==================== listAll ====================

    @Test
    void listAllReturnsAllMovies() {
        Movie m1 = new Movie();
        m1.setId(1L);
        m1.setTitle("电影A");
        Movie m2 = new Movie();
        m2.setId(2L);
        m2.setTitle("电影B");

        when(movieMapper.selectList(any())).thenReturn(List.of(m1, m2));

        List<Movie> result = movieService.listAll();
        assertEquals(2, result.size());
    }

    // ==================== save ====================

    @Test
    void saveInsertsNewMovieWithDefaultStatus() {
        Movie movie = new Movie();
        movie.setTitle("新电影");

        Movie result = movieService.save(movie);

        assertEquals("SHOWING", result.getStatus());
        verify(movieMapper).insert(movie);
        verify(movieMapper, never()).updateById(any());
    }

    @Test
    void saveUpdatesExistingMovie() {
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("更新电影");

        movieService.save(movie);

        verify(movieMapper).updateById(movie);
        verify(movieMapper, never()).insert(any());
    }

    // ==================== delete ====================

    @Test
    void deleteCallsMapperDelete() {
        movieService.delete(1L);
        verify(movieMapper).deleteById(1L);
    }
}
