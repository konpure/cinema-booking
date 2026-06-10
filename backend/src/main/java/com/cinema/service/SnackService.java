package com.cinema.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cinema.entity.Snack;
import com.cinema.mapper.SnackMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SnackService {

    private final SnackMapper snackMapper;

    public List<Snack> listOnSale() {
        return snackMapper.selectList(new LambdaQueryWrapper<Snack>()
                .eq(Snack::getStatus, "ON_SALE")
                .orderByAsc(Snack::getCategory, Snack::getPrice));
    }

    public List<Snack> listAll() {
        return snackMapper.selectList(new LambdaQueryWrapper<Snack>().orderByAsc(Snack::getId));
    }

    public Snack save(Snack snack) {
        if (snack.getStatus() == null) snack.setStatus("ON_SALE");
        if (snack.getId() == null) {
            snackMapper.insert(snack);
        } else {
            snackMapper.updateById(snack);
        }
        return snack;
    }

    public void delete(Long id) {
        snackMapper.deleteById(id);
    }

    public Snack getById(Long id) {
        Snack snack = snackMapper.selectById(id);
        if (snack == null || !"ON_SALE".equals(snack.getStatus())) {
            throw new IllegalArgumentException("卖品不存在或已下架");
        }
        return snack;
    }
}
