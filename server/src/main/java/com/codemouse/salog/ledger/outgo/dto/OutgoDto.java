package com.codemouse.salog.ledger.outgo.dto;

import com.codemouse.salog.tags.ledgerTags.dto.LedgerTagDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;


public class OutgoDto {
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Post {
        private LocalDate date;
        private int money;
        @Size(max = 15, message = "outgoName이 15글자이내여야 합니다.")
        private String outgoName;
        private String payment;
        @Size(max = 20, message = "memo는 20자이내여야 합니다.")
        private String memo;
        @Pattern(regexp = "^\\S{1,10}$", message = "outgoTag는 공백 없이 1~10글자 사이여야 합니다.")
        private String outgoTag;
        private String receiptImg;
        @NotNull(message = "wasteList가 비어있습니다.")
        private Boolean wasteList;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class PostImage {
        private String receiptImageUrl;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Patch {
        private LocalDate date;
        private int money;
        @Size(max = 15, message = "outgoName이 15글자이내여야 합니다.")
        private String outgoName;
        private String payment;
        @Size(max = 20, message = "memo는 20자이내여야 합니다.")
        private String memo;
        @Pattern(regexp = "^\\S{1,10}$", message = "outgoTag는 공백 없이 1~10글자 사이여야 합니다.")
        private String outgoTag;
        private String receiptImg;
        private boolean wasteList;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Response {
        private Long outgoId;
        private LocalDate date;
        private int money;
        private String outgoName;
        private String payment;
        private String memo;
        private LedgerTagDto.Response outgoTag;
        private boolean wasteList;
        private String receiptImg;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class ImageOcrResponse {
        private String date;
        private int money;
        private String outgoName;
        private String receiptImg;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class MonthlyResponse {
        private long monthlyTotal;
        private List<LedgerTagDto.MonthlyResponse> tags;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class YearlyResponse {
        // 연간 수입/지출 총합계
        private String date;    // 수입,지출을 보여줄 해당 월
        private long monthlyIncome; // 월간 수입의 총합계
        private long monthlyOutgo;  // 월간 지출의 총합계
    }
}

