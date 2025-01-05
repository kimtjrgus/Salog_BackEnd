package com.codemouse.salog.audit;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class Auditable {
    @CreatedDate
    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP")
    private final LocalDateTime createdAt = LocalDateTime.now();
}

/*
    LocalDateTime은 Java의 날짜 및 시간 API의 일부로, 타입 안전성을 제공함
    그래서 자바 코드에서 조금 더 효율적으로 사용할 수 있도록 LocalDateTime 타입을 사용하는 것을 유지하되
    데이터 베이스 확장 가능성을 염두해 두고 보편적으로 DB에서 사용되는 TimeStamp 타입을 명시만 해줌
*/