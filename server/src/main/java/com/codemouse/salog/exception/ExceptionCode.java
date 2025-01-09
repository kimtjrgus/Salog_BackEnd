package com.codemouse.salog.exception;

import lombok.Getter;

// 커스텀 에러코드, 예상 되는 에러 코드 작성할 것
@Getter
public enum ExceptionCode {
    // 회원
    MEMBER_NOT_FOUND(404, "MEMBER_NOT_FOUND 존재하지 않는 회원"),
    MEMBER_UNAUTHORIZED(401,"MEMBER_UNAUTHORIZED 인증되지 않음"),
    MEMBER_EXISTS(409, "MEMBER_EXISTS 이미 존재하는 회원"),
    NOT_IMPLEMENTATION(501, "Not Implementation 존재하지 않는 기능"),
    PASSWORD_MISMATCHED(400,"PASSWORD_MISMATCHED 비밀번호가 일치하지 않거나 소셜 가입한 회원"),
    ID_MISMATCHED(400,"ID_MISMATCHED 아이디가 일치하지 않음"),
    MEMBER_ALREADY_DELETED(409, "MEMBER_ALREADY_DELETED 이미 탈퇴한 회원"),
    EMAIL_EXIST(409, "EMAIL_EXIST 이미 존재하는 이메일"),
    PASSWORD_IDENTICAL(409, "PASSWORD_IDENTICAL 이전 비밀번호와 동일함"),
    LOGOUT(400, "LOGOUT 재로그인 필요"),
    TOKEN_EXPIRED(400, "TOKEN_EXPIRED 토큰 만료"),
    TOKEN_INVALID(400, "TOKEN_INVALID 유효하지 않은 토큰"),
    //Oauth
    SOCIAL_MEMBER(400, "SOCIAL_MEMBER 소셜 가입한 회원"),

    // 일기
    MEMBER_MISMATCHED(400, "MEMBER_MISMATCHED MemberId가 일치하지 않음"),
    DIARY_NOT_FOUND(404, "DIARY_NOT_FOUND 존재하지 않는 일기"),
    TAG_NOT_FOUND(404, "TAG_NOT_FOUND 존재하지 않는 태그"),
    TAG_UNVALIDATED(400, "TAG_UNVALIDATED 유효하지않은 태그, 10글자이내로 입력바람"),
    INVALID_SEARCH_TYPE(400, "INVALID_SEARCH_TYPE 유효하지 않은 검색 타입"),

    // 수입
    INCOME_NOT_FOUND(404, "INCOME_NOT_FOUND 존재하지 않는 수입"),

    // 고정 수입
    FIXED_INCOME_NOT_FOUND(404, "FIXED_INCOME_NOT_FOUND 존재하지 않는 고정 수입"),

    // 지출
    OUTGO_NOT_FOUND(404, "OUTGO_NOT_FOUND 존재하지 않는 지출"),

    // 고정 지출
    FIXED_OUTGO_NOT_FOUND(404, "FIXED_OUTGO_NOT_FOUND 존재하지 않는 고정 지출"),

    // 월간 예산
    BUDGET_NOT_FOUND(404, "BUDGET_NOT_FOUND 존재하지 않는 월별 예산"),
    BUDGET_EXIST(400, "BUDGET_EXIST 이미 존재하는 예산"),

    // 가계부 조회 시 유효한 날짜 형식이 아닐 시 (문자열로 입력되기 때문에 직접 제한을 두어야 함 / 음수 포함)
    INVALID_DATE_FORMAT(400, "INVALID_DATE_FORMAT 유효하지 않은 날짜 형식입니다."),
    // 가계부 조회 시 유효한 날짜가 아닐 시
    INVALID_YEAR(400, "INVALIDATED_YEAR 연도는 컴퓨터 시각 기준으로 100년까지로 제한됩니다."),
    INVALID_DAY(400, "INVALIDATED_DAY 유효하지 않은 일자"),
    INVALID_MONTH(400, "INVALIDATED_MONTH 유효하지 않은 월자"),
    // 범위 조회 시, 보다 상세한 에러를 위해 에러 코드 추가
    INVALID_START_DAY(400, "INVALIDATED_START_DAY 유효하지 않은 시작 일자"),
    INVALID_END_DAY(400, "INVALIDATED_END_DAY 유효하지 않은 종료 일자"),
    INVALID_START_MONTH(400, "INVALIDATED_START_MONTH 유효하지 않은 시작 월자"),
    INVALID_END_MONTH(400, "INVALIDATED_END_MONTH 유효하지 않은 종료 월자"),
    INVALID_DATE_RANGE(400, "INVALIDATED_DATE_RANGE 유효하지 않은 날짜 범위");

    private int status;

    private String message;

    ExceptionCode(int code, String message) {
        this.status = code;
        this.message = message;
    }
}
