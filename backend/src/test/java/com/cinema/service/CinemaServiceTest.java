package com.cinema.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cinema.entity.Cinema;
import com.cinema.mapper.CinemaMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CinemaServiceTest {

    @Mock
    private CinemaMapper cinemaMapper;

    @InjectMocks
    private CinemaService cinemaService;

    // ==================== listOpen ====================

    @Test
    void listOpenReturnsOpenCinemas() {
        Cinema c1 = new Cinema();
        c1.setId(1L);
        c1.setName("万达影城");
        c1.setStatus("OPEN");

        when(cinemaMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(c1));

        List<Cinema> result = cinemaService.listOpen();
        assertEquals(1, result.size());
        assertEquals("万达影城", result.get(0).getName());
    }

    @Test
    void listOpenReturnsEmptyList() {
        when(cinemaMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        List<Cinema> result = cinemaService.listOpen();
        assertTrue(result.isEmpty());
    }

    // ==================== listAll ====================

    @Test
    void listAllReturnsAllCinemas() {
        Cinema c1 = new Cinema();
        c1.setId(1L);
        c1.setName("万达影城");
        Cinema c2 = new Cinema();
        c2.setId(2L);
        c2.setName("金逸影城");

        when(cinemaMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(c1, c2));

        List<Cinema> result = cinemaService.listAll();
        assertEquals(2, result.size());
    }

    // ==================== save (insert) ====================

    @Test
    void saveInsertsNewCinema() {
        Cinema cinema = new Cinema();
        cinema.setName("新影城");
        cinema.setCity("深圳");
        cinema.setAddress("南山区");

        Cinema result = cinemaService.save(cinema);

        assertEquals("OPEN", result.getStatus());
        verify(cinemaMapper).insert(cinema);
        verify(cinemaMapper, never()).updateById(any());
    }

    // ==================== save (update) ====================

    @Test
    void saveUpdatesExistingCinema() {
        Cinema cinema = new Cinema();
        cinema.setId(1L);
        cinema.setName("更新影城");
        cinema.setStatus("CLOSED");

        Cinema result = cinemaService.save(cinema);

        assertEquals("CLOSED", result.getStatus());
        verify(cinemaMapper).updateById(cinema);
        verify(cinemaMapper, never()).insert(any());
    }

    // ==================== getById ====================

    @Test
    void getByIdReturnsCinema() {
        Cinema cinema = new Cinema();
        cinema.setId(1L);
        cinema.setName("万达影城");

        when(cinemaMapper.selectById(1L)).thenReturn(cinema);

        Cinema result = cinemaService.getById(1L);
        assertNotNull(result);
        assertEquals("万达影城", result.getName());
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        when(cinemaMapper.selectById(999L)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cinemaService.getById(999L));
        assertEquals("影城不存在", ex.getMessage());
    }
}
