package com.cinema.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cinema.annotation.ReadOnly;
import com.cinema.entity.Cinema;
import com.cinema.mapper.CinemaMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CinemaService {

    private final CinemaMapper cinemaMapper;
    private final MemcachedService memcached;
    private static final ObjectMapper MC_JSON = new ObjectMapper();

    @ReadOnly
    @SneakyThrows
    public List<Cinema> listOpen() {
        String cached = memcached.get("cinema:open");
        if (cached != null) {
            return MC_JSON.readValue(cached,
                MC_JSON.getTypeFactory().constructCollectionType(List.class, Cinema.class));
        }
        List<Cinema> list = cinemaMapper.selectList(new LambdaQueryWrapper<Cinema>()
                .eq(Cinema::getStatus, "OPEN")
                .orderByAsc(Cinema::getId));
        memcached.set("cinema:open", 300, MC_JSON.writeValueAsString(list));
        return list;
    }

    @ReadOnly
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
        memcached.delete("cinema:open");
        return cinema;
    }

    @ReadOnly
    public Cinema getById(Long id) {
        Cinema cinema = cinemaMapper.selectById(id);
        if (cinema == null) throw new IllegalArgumentException("影城不存在");
        return cinema;
    }
}
