package com.cinema.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cinema.entity.Cinema;
import com.cinema.mapper.CinemaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CinemaService {

    private final CinemaMapper cinemaMapper;

    public List<Cinema> listOpen() {
        return cinemaMapper.selectList(new LambdaQueryWrapper<Cinema>()
                .eq(Cinema::getStatus, "OPEN")
                .orderByAsc(Cinema::getId));
    }

    public List<Cinema> listAll() {
        return cinemaMapper.selectList(new LambdaQueryWrapper<Cinema>().orderByAsc(Cinema::getId));
    }

    public Cinema save(Cinema cinema) {
        if (cinema.getStatus() == null) cinema.setStatus("OPEN");
        if (cinema.getId() == null) {
            cinemaMapper.insert(cinema);
        } else {
            cinemaMapper.updateById(cinema);
        }
        return cinema;
    }

    public Cinema getById(Long id) {
        Cinema cinema = cinemaMapper.selectById(id);
        if (cinema == null) {
            throw new IllegalArgumentException("影城不存在");
        }
        return cinema;
    }
}
