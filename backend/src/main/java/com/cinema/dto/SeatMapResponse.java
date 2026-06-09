package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SeatMapResponse {
    private Long screeningId;
    private Integer rows;
    private Integer cols;
    private List<String> sold;
    /** 他人锁定的座位 */
    private List<String> locked;
    /** 当前用户自己锁定的座位（仍可操作） */
    private List<String> myLocked;
}
