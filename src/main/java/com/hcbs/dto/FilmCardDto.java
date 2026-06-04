package com.hcbs.dto;

public record FilmCardDto(
        //首页海报卡片：id、标题、海报、类型、分级、评分
        Long filmId,
        String title,
        String posterUrl,
        String genre,
        String ageRating,
        double rating
) {
}
