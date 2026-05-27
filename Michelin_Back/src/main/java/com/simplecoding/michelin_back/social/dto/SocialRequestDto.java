package com.simplecoding.michelin_back.social.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialRequestDto {
    private Long memberId;      // [수정] userId -> memberId
    private Long restaurantId;
}