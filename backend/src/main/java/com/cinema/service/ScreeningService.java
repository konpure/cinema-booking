package com.cinema.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cinema.entity.Screening;
import com.cinema.mapper.ScreeningMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScreeningService {

    private final ScreeningMapper screeningMapper;

    public List<Screening> listByMovie(Long movieId, Long cinemaId) {
        LambdaQueryWrapper<Screening> wrapper = new LambdaQueryWrapper<Screening>()
                .eq(Screening::getMovieId, movieId)
                .eq(Screening::getStatus, "OPEN")
                .orderByAsc(Screening::getStartTime);
        if (cinemaId != null) {
            wrapper.eq(Screening::getCinemaId, cinemaId);
        }
        return screeningMapper.selectList(wrapper);
    }

    public Screening getById(Long id) {
        Screening s = screeningMapper.selectById(id);
        if (s == null) throw new IllegalArgumentException("场次不存在");
        return s;
    }

    public List<Screening> listAll() {
        return screeningMapper.selectList(new LambdaQueryWrapper<Screening>().orderByAsc(Screening::getStartTime));
    }

    public Screening save(Screening screening) {
        if (screening.getSeatRows() == null) screening.setSeatRows(8);
        if (screening.getSeatCols() == null) screening.setSeatCols(12);
        if (screening.getStatus() == null) screening.setStatus("OPEN");
        if (screening.getId() == null) {
            screeningMapper.insert(screening);
        } else {
            screeningMapper.updateById(screening);
        }
        return screening;
    }

    public void delete(Long id) {
        screeningMapper.deleteById(id);
    }
}
