package com.cinema.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cinema.entity.OrderSeat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderSeatMapper extends BaseMapper<OrderSeat> {

    @Select("SELECT CONCAT(os.row_num, '-', os.col_num) AS seatKey " +
            "FROM order_seats os JOIN orders o ON os.order_id = o.id " +
            "WHERE o.screening_id = #{screeningId} AND o.status IN ('PAID', 'PENDING')")
    List<String> findSoldSeats(Long screeningId);
}
