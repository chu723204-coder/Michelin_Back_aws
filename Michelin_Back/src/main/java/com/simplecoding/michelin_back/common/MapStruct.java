package com.simplecoding.michelin_back.common;


import com.simplecoding.michelin_back.notice.dto.NoticeRequestDto;
import com.simplecoding.michelin_back.notice.dto.NoticeResponseDto;
import com.simplecoding.michelin_back.notice.entity.Notice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface MapStruct {

    // --- [2] Notice: source를 직접 지정하여 자동 매핑(오염) 방지 ---
    @Mapping(target = "noticeId", ignore = true)
    @Mapping(target = "writerId", ignore = true)
    @Mapping(target = "deleteYn", ignore = true)
    @Mapping(target = "deleteTime", ignore = true)
    @Mapping(target = "title", source = "dto.title")
    @Mapping(target = "content", source = "dto.content")
// fixYn이 Dto에 없다면 기본값 'N'을 보장하도록 설정하는 것이 좋습니다.
    @Mapping(target = "fixYn", source = "dto.fixYn", defaultValue = "N")
    Notice toEntity(NoticeRequestDto dto);

    // 2. Entity -> ResponseDto 변환 (가장 중요한 부분)
    @Mapping(target = "title", source = "entity.title")
    @Mapping(target = "content", source = "entity.content")
// ✅ 수정: insertTime이 null일 경우를 대비해 삼항 연산자(null 체크) 추가
    @Mapping(target = "formattedDate",
            expression = "java(entity.getCreatedAt() != null ? entity.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm\")) : \"\")")
// ✅ 수정: 기준을 1일로 잡으셨는데, 보통 공지는 7일 정도로 넉넉하게 잡거나 null 체크를 추가합니다.
    @Mapping(target = "isNew",
            expression = "java(entity.getCreatedAt() != null ? entity.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusDays(1)) : false)")
    @Mapping(target = "deleteYn", source = "entity.deleteYn")  // ✅ 추가
    NoticeResponseDto toResponseDto(Notice entity);


}
