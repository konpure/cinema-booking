package com.cinema.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cinema.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT DATE(created_at) AS date, SUM(total_price) AS revenue, COUNT(*) AS count " +
            "FROM orders WHERE status = 'PAID' GROUP BY DATE(created_at) ORDER BY date DESC LIMIT 7")
    List<Map<String, Object>> revenueByDay();

    @Select("SELECT m.title AS title, COUNT(o.id) AS orderCount, SUM(o.total_price) AS revenue " +
            "FROM orders o JOIN screenings s ON o.screening_id = s.id JOIN movies m ON s.movie_id = m.id " +
            "WHERE o.status = 'PAID' GROUP BY m.id, m.title ORDER BY revenue DESC LIMIT 5")
    List<Map<String, Object>> topMovies();
}
