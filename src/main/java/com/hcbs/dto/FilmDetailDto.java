package com.hcbs.dto;

import java.util.List;

public record FilmDetailDto(
        //详情页：在卡片基础上加简介、时长、演员、场次列表
        Long filmId,
        String title,
        String posterUrl,
        String description,
        String genre,
        String ageRating,
        double rating,
        int durationMinutes,
        String actors,
        List<ShowingRow> upcomingShowings
) {
}
