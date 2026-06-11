package com.cinema.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cinema.entity.Screening;
import com.cinema.mapper.ScreeningMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScreeningServiceTest {

    @Mock
    private ScreeningMapper screeningMapper;

    @InjectMocks
    private ScreeningService screeningService;

    // ==================== listByMovie ====================

    @Test
    void listByMovieWithCinemaId() {
        Screening s = new Screening();
        s.setId(1L);
        s.setMovieId(1L);
        s.setCinemaId(1L);
        s.setHallName("1号厅");

        when(screeningMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(s));

        List<Screening> result = screeningService.listByMovie(1L, 1L);
        assertEquals(1, result.size());
        assertEquals("1号厅", result.get(0).getHallName());
    }

    @Test
    void listByMovieWithoutCinemaId() {
        Screening s1 = new Screening();
        s1.setId(1L);
        s1.setMovieId(1L);
        s1.setCinemaId(1L);
        Screening s2 = new Screening();
        s2.setId(2L);
        s2.setMovieId(1L);
        s2.setCinemaId(2L);

        when(screeningMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(s1, s2));

        List<Screening> result = screeningService.listByMovie(1L, null);
        assertEquals(2, result.size());
    }

    @Test
    void listByMovieReturnsEmptyList() {
        when(screeningMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        List<Screening> result = screeningService.listByMovie(999L, null);
        assertTrue(result.isEmpty());
    }

    // ==================== getById ====================

    @Test
    void getByIdReturnsScreening() {
        Screening s = new Screening();
        s.setId(1L);
        s.setHallName("IMAX厅");

        when(screeningMapper.selectById(1L)).thenReturn(s);

        Screening result = screeningService.getById(1L);
        assertNotNull(result);
        assertEquals("IMAX厅", result.getHallName());
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        when(screeningMapper.selectById(999L)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> screeningService.getById(999L));
        assertEquals("场次不存在", ex.getMessage());
    }

    // ==================== listAll ====================

    @Test
    void listAllReturnsAllScreenings() {
        Screening s1 = new Screening();
        s1.setId(1L);
        Screening s2 = new Screening();
        s2.setId(2L);

        when(screeningMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(s1, s2));

        List<Screening> result = screeningService.listAll();
        assertEquals(2, result.size());
    }

    // ==================== save (insert with defaults) ====================

    @Test
    void saveInsertsNewScreeningWithDefaults() {
        Screening screening = new Screening();
        screening.setMovieId(1L);
        screening.setCinemaId(1L);
        screening.setHallName("1号厅");

        Screening result = screeningService.save(screening);

        assertEquals(8, result.getSeatRows());
        assertEquals(12, result.getSeatCols());
        assertEquals("OPEN", result.getStatus());
        verify(screeningMapper).insert(screening);
        verify(screeningMapper, never()).updateById(any());
    }

    // ==================== save (update) ====================

    @Test
    void saveUpdatesExistingScreening() {
        Screening screening = new Screening();
        screening.setId(1L);
        screening.setMovieId(1L);
        screening.setHallName("更新厅");
        screening.setSeatRows(10);
        screening.setSeatCols(15);

        screeningService.save(screening);

        verify(screeningMapper).updateById(screening);
        verify(screeningMapper, never()).insert(any());
    }

    // ==================== delete ====================

    @Test
    void deleteCallsMapperDelete() {
        screeningService.delete(1L);
        verify(screeningMapper).deleteById(1L);
    }
}
