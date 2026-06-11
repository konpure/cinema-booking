package com.cinema.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cinema.entity.Snack;
import com.cinema.mapper.SnackMapper;
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
class SnackServiceTest {

    @Mock
    private SnackMapper snackMapper;

    @InjectMocks
    private SnackService snackService;

    // ==================== listOnSale ====================

    @Test
    void listOnSaleReturnsOnSaleSnacks() {
        Snack s1 = new Snack();
        s1.setId(1L);
        s1.setName("爆米花");
        s1.setStatus("ON_SALE");
        s1.setPrice(new BigDecimal("25.00"));

        when(snackMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(s1));

        List<Snack> result = snackService.listOnSale();
        assertEquals(1, result.size());
        assertEquals("爆米花", result.get(0).getName());
    }

    @Test
    void listOnSaleReturnsEmptyList() {
        when(snackMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        List<Snack> result = snackService.listOnSale();
        assertTrue(result.isEmpty());
    }

    // ==================== listAll ====================

    @Test
    void listAllReturnsAllSnacks() {
        Snack s1 = new Snack();
        s1.setId(1L);
        s1.setName("爆米花");
        Snack s2 = new Snack();
        s2.setId(2L);
        s2.setName("可乐");

        when(snackMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(s1, s2));

        List<Snack> result = snackService.listAll();
        assertEquals(2, result.size());
    }

    // ==================== save (insert) ====================

    @Test
    void saveInsertsNewSnackWithDefaultStatus() {
        Snack snack = new Snack();
        snack.setName("薯片");
        snack.setPrice(new BigDecimal("15.00"));

        Snack result = snackService.save(snack);

        assertEquals("ON_SALE", result.getStatus());
        verify(snackMapper).insert(snack);
        verify(snackMapper, never()).updateById(any());
    }

    // ==================== save (update) ====================

    @Test
    void saveUpdatesExistingSnack() {
        Snack snack = new Snack();
        snack.setId(1L);
        snack.setName("大份爆米花");
        snack.setStatus("OFF_SALE");

        snackService.save(snack);

        verify(snackMapper).updateById(snack);
        verify(snackMapper, never()).insert(any());
    }

    // ==================== delete ====================

    @Test
    void deleteCallsMapperDelete() {
        snackService.delete(1L);
        verify(snackMapper).deleteById(1L);
    }

    // ==================== getById ====================

    @Test
    void getByIdReturnsOnSaleSnack() {
        Snack snack = new Snack();
        snack.setId(1L);
        snack.setName("爆米花");
        snack.setStatus("ON_SALE");

        when(snackMapper.selectById(1L)).thenReturn(snack);

        Snack result = snackService.getById(1L);
        assertNotNull(result);
        assertEquals("爆米花", result.getName());
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        when(snackMapper.selectById(999L)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> snackService.getById(999L));
        assertEquals("卖品不存在或已下架", ex.getMessage());
    }

    @Test
    void getByIdThrowsWhenOffSale() {
        Snack snack = new Snack();
        snack.setId(1L);
        snack.setName("下架商品");
        snack.setStatus("OFF_SALE");

        when(snackMapper.selectById(1L)).thenReturn(snack);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> snackService.getById(1L));
        assertEquals("卖品不存在或已下架", ex.getMessage());
    }
}
