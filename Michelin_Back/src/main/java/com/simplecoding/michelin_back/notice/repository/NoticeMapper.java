package com.simplecoding.michelin_back.notice.repository;

import com.simplecoding.michelin_back.notice.dto.NoticeResponseDto;
import com.simplecoding.michelin_back.notice.entity.Notice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring", imports = {DateTimeFormatter.class, LocalDateTime.class})
public interface NoticeMapper {

    // getInsertTime()을 getCreatedAt()으로 변경했습니다.
    @Mapping(target = "formattedDate", expression = "java(entity.getCreatedAt().format(DateTimeFormatter.ofPattern(\"yyyy-MM-dd\")))")
    @Mapping(target = "isNew", expression = "java(entity.getCreatedAt().isAfter(LocalDateTime.now().minusDays(7)))")
    NoticeResponseDto toResponseDto(Notice entity);
}