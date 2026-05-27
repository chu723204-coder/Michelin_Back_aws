package com.simplecoding.michelin_back.notice.entity;

import com.simplecoding.michelin_back.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NOTICE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notice_seq")
    @SequenceGenerator(name = "notice_seq", sequenceName = "SEQ_NOTICE", allocationSize = 1)
    @Column(name = "NOTICE_ID")
    private Long noticeId;

    @Column(name = "TITLE", length = 500, nullable = false)
    private String title;

    @Lob // CLOB 매핑
    @Column(name = "CONTENT", nullable = false)
    private String content;

    @Column(name = "WRITER_ID", nullable = false)
    private Long writerId; // Long으로 변경하여 DB의 NUMBER 타입과 일치시킴

    @Column(name = "FIX_YN", length = 1, nullable = false)
    @Builder.Default
    private String fixYn = "N";

    @Column(name = "DELETE_YN", length = 1, nullable = false)
    @Builder.Default
    private String deleteYn = "N";

    @Column(name = "DELETE_TIME")
    private LocalDateTime deleteTime;
}