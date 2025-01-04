package com.codemouse.salog.ledger.outgo.service;

import com.codemouse.salog.auth.jwt.JwtTokenizer;
import com.codemouse.salog.auth.utils.TokenBlackListService;
import com.codemouse.salog.dto.MultiResponseDto;
import com.codemouse.salog.exception.BusinessLogicException;
import com.codemouse.salog.exception.ExceptionCode;
import com.codemouse.salog.helper.naverOcr.ClovaOcrApiService;
import com.codemouse.salog.helper.naverOcr.ClovaOcrDto;
import com.codemouse.salog.ledger.outgo.dto.OutgoDto;
import com.codemouse.salog.ledger.outgo.entity.Outgo;
import com.codemouse.salog.ledger.outgo.mapper.OutgoMapper;
import com.codemouse.salog.ledger.outgo.repository.OutgoRepository;
import com.codemouse.salog.members.entity.Member;
import com.codemouse.salog.members.service.MemberService;
import com.codemouse.salog.tags.ledgerTags.dto.LedgerTagDto;
import com.codemouse.salog.tags.ledgerTags.entity.LedgerTag;
import com.codemouse.salog.tags.ledgerTags.repository.LedgerTagRepository;
import com.codemouse.salog.tags.ledgerTags.service.LedgerTagService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
@Transactional
public class OutgoService {
    private final OutgoRepository outgoRepository;
    private final OutgoMapper outgoMapper;
    private final LedgerTagRepository ledgerTagRepository;
    private final LedgerTagService ledgerTagService;
    private final JwtTokenizer jwtTokenizer;
    private final TokenBlackListService tokenBlackListService;
    private final MemberService memberService;
    private final ClovaOcrApiService clovaOcrApiService;


    // POST
    @Transactional
    public OutgoDto.Response createOutgo (String token, OutgoDto.Post outgoDTO){
        tokenBlackListService.isBlackListed(token);

        if(!isValidYear(outgoDTO.getDate().getYear())){
            throw new BusinessLogicException(ExceptionCode.INVALID_YEAR);
        }

        Member member = memberService.findVerifiedMember(jwtTokenizer.getMemberId(token));
        Outgo outgo = outgoMapper.outgoPostDtoToOutgo(outgoDTO);
        outgo.setMember(member);

        // 태그 생성 로직
        Outgo savedOutgo = tagHandler(outgoDTO.getOutgoTag(), token, outgo);

        return outgoMapper.outgoToOutgoResponseDto(savedOutgo);
    }

    // PATCH
    @Transactional
    public OutgoDto.Response patchOutgo (String token, long outgoId, OutgoDto.Patch outgoDto){
        tokenBlackListService.isBlackListed(token);

        Outgo outgo = outgoMapper.outgoPatchDtoToOutgo(outgoDto);
        Outgo findOutgo = findVerifiedOutgo(outgoId);
        memberService.verifiedRequest(token, findOutgo.getMember().getMemberId());

        Optional.of(outgo.getDate()).ifPresent(findOutgo::setDate);
        Optional.of(outgo.getOutgoName()).ifPresent(findOutgo::setOutgoName);
        Optional.of(outgo.getMoney()).ifPresent(findOutgo::setMoney);
        Optional.of(outgo.getPayment()).ifPresent(findOutgo::setPayment);
        Optional.ofNullable(outgo.getMemo()).ifPresent(findOutgo::setMemo);
        Optional.of(outgo.getReceiptImg()).ifPresent(findOutgo::setReceiptImg);
        Optional.of(outgo.isWasteList()).ifPresent(findOutgo::setWasteList);

        Outgo savedOutgo = tagHandler(outgoDto.getOutgoTag(), token, findOutgo);

        return outgoMapper.outgoToOutgoResponseDto(savedOutgo);
    }

    // GET All List
    @Transactional
    public MultiResponseDto<OutgoDto.Response> findAllOutgos (String token, int page, int size, String date, String outgoTag){
        tokenBlackListService.isBlackListed(token);

        Page<Outgo> outgoPage = findOutgoPages(token, page, size, date, null, null, outgoTag, null);

        List<OutgoDto.Response> outgoDtoList = outgoPage.getContent().stream()
                .map(outgoMapper::outgoToOutgoResponseDto)
                .collect(Collectors.toList());

        return new MultiResponseDto<>(outgoDtoList, outgoPage);
    }

    // 기간을 사용자가 커스텀하여 지출을 조회하는 메서드
    public MultiResponseDto<OutgoDto.Response> findOutgosByDateRange(String token, int page, int size, String fromDate, String toDate, String outgoTag){
        tokenBlackListService.isBlackListed(token);
        Page<Outgo> outgoPage = findOutgoPages(token, page, size, null, fromDate, toDate, outgoTag, null);

        List<OutgoDto.Response> outgoDtoList = outgoPage.getContent().stream()
                .map(outgoMapper::outgoToOutgoResponseDto)
                .collect(Collectors.toList());

        return new MultiResponseDto<>(outgoDtoList, outgoPage);
    }

    // GET WasteList
    public MultiResponseDto<OutgoDto.Response> findAllWasteLists(String token, int page, int size, String date, String outgoTag){
        tokenBlackListService.isBlackListed(token);
        Page<Outgo> wastePage = findOutgoPages(token, page, size, date, null, null, outgoTag, true);

        List<OutgoDto.Response> wasteDtoList = wastePage.getContent().stream()
                .map(outgoMapper::outgoToOutgoResponseDto)
                .collect(Collectors.toList());

        return new MultiResponseDto<>(wasteDtoList, wastePage);
    }

    // 기간을 사용자가 커스텀하여 낭비리스트를 조회하는 메서드
    public MultiResponseDto<OutgoDto.Response> findWasteListsByDateRange(String token, int page, int size, String fromDate, String toDate, String outgoTag){
        tokenBlackListService.isBlackListed(token);
        Page<Outgo> wastePage = findOutgoPages(token, page, size, null, fromDate, toDate, outgoTag, true);

        List<OutgoDto.Response> wasteDtoList = wastePage.getContent().stream()
                .map(outgoMapper::outgoToOutgoResponseDto)
                .collect(Collectors.toList());

        return new MultiResponseDto<>(wasteDtoList, wastePage);
    }

    // GET Outgo Sum
    public OutgoDto.MonthlyResponse getSumOfOutgoLists(String token, String date){
        tokenBlackListService.isBlackListed(token);
        long memberId = jwtTokenizer.getMemberId(token);

        String[] dateParts = date.split("-");
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]);

        List<Object[]> results = outgoRepository.getSumOfOutgoListsByTag(memberId, year, month);
        List<LedgerTagDto.MonthlyResponse> sumByTags = results.stream()
                .map(result -> {
                    String tagName = (String) result[0];
                    long tagSum = (result[1] != null ? ((Number) result[1]).longValue() : 0L);
                    return new LedgerTagDto.MonthlyResponse(tagName, tagSum);
                })
                .collect(Collectors.toList());

        // 월간 지출 합계를 계산합니다.
        long monthlyOutgo = Optional.ofNullable(
                outgoRepository.findTotalOutgoByMonth(memberId, year, month))
                .orElse(0L);

        return new OutgoDto.MonthlyResponse(monthlyOutgo, sumByTags);
    }

    // GET WasteList Sum
    public OutgoDto.MonthlyResponse getSumOfWasteLists(String token, String date){
        tokenBlackListService.isBlackListed(token);
        long memberId = jwtTokenizer.getMemberId(token);

        String[] dateParts = date.split("-");
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]);

        List<Object[]> results = outgoRepository.getSumOfWasteListsByTag(memberId, year, month);
        List<LedgerTagDto.MonthlyResponse> sumByTags = results.stream()
                .map(result -> {
                    String tagName = (String) result[0];
                    long tagSum = (result[1] != null ? ((Number) result[1]).longValue() : 0L);
                    return new LedgerTagDto.MonthlyResponse(tagName, tagSum);
                })
                .collect(Collectors.toList());

        long monthlyWasteList =
                sumByTags.stream().mapToLong(LedgerTagDto.MonthlyResponse::getTagSum).sum();

        return new OutgoDto.MonthlyResponse(monthlyWasteList, sumByTags);
    }

    // DELETE
    @Transactional
    public void deleteOutgo (String token, long outgoId){
        log.info("Outgo delete requested - token: {}, outgoId: {}", token, outgoId);
        tokenBlackListService.isBlackListed(token);

        Outgo findOutgo = findVerifiedOutgo(outgoId);
        memberService.verifiedRequest(token, findOutgo.getMember().getMemberId());

        LedgerTag ledgerTag = findOutgo.getLedgerTag();

        // 연결을 먼저 끊어줌.
        findOutgo.setLedgerTag(null);

        // 삭제하려는 지출에만 연결된 태그인 경우, 태그 삭제
        if (ledgerTag != null && ledgerTag.getOutgos().size() == 1) {
            ledgerTagService.deleteLedgerTag(token, ledgerTag.getLedgerTagId());
        }

        outgoRepository.delete(findOutgo);
        log.info("Outgo successfully deleted - memberId: {}, outgoId: {}", jwtTokenizer.getMemberId(token), outgoId);
    }

    public Outgo findVerifiedOutgo(long outgoId){
        return outgoRepository.findById(outgoId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.OUTGO_NOT_FOUND));
    }

    public long getDailyTotalOutgo(String token, LocalDate curDate){
        long memberId = jwtTokenizer.getMemberId(token);

        return Optional.ofNullable(
                outgoRepository.findTotalOutgoByDay(memberId, curDate)
        ).orElse(0L); // 쿼리결과 null 값이 반환될 경우 0이 대신 반환
    }

    // 태그 등록, 중복 체크
    private Outgo tagHandler(String outgoPostDto, String token, Outgo outgo) {
        LedgerTag ledgerTag = null;

        if (outgoPostDto != null) {
            String tagName = outgoPostDto;

            LedgerTag existTag = ledgerTagService.findLedgerTagByMemberIdAndTagName(token, tagName, LedgerTag.Group.OUTGO);

            if (existTag != null) {
                if (existTag.getCategory() == LedgerTag.Group.OUTGO) {
                    ledgerTag = existTag;
                }
            } else {
                LedgerTagDto.Post tagPost = new LedgerTagDto.Post(tagName, LedgerTag.Group.OUTGO);
                ledgerTag = ledgerTagService.postLedgerTag(token, tagPost);
            }
        }

        // 만약 ledgerTag가 null이 아니라면 outgo 객체에 설정
        if (ledgerTag != null) {
            outgo.setLedgerTag(ledgerTag);
        }

        Outgo savedOutgo = outgoRepository.save(outgo);
        ledgerTagService.deleteUnusedOutgoTagsByMemberId(token);

        return savedOutgo;
    }

    // 지출, 낭비리스트 조회시 페이지 생성 중복 코드 통일 (월별, 일별, 기간 지정 조회)
    private Page<Outgo> findOutgoPages(String token, int page, int size, String date, String fromDate, String toDate,String outgoTag, Boolean isWasteList) {
        long memberId = jwtTokenizer.getMemberId(token);
        Page<Outgo> outgoPage = null;

        // 입력 받은 날짜의 유효성 검증
        isValidDate(date, fromDate, toDate);

        // 1. 조회할 날짜 지정
        // 시작 날짜와 종료날짜 null로 초기화
        LocalDate startDate = null;
        LocalDate endDate = null;

        // 월별 조회
        if (date != null && date.endsWith("00") && fromDate == null && toDate == null) { //2025-01-00
            LocalDate parsedDate = LocalDate.parse(date.substring(0, 7) + "-01");
            startDate = parsedDate.withDayOfMonth(1);
            endDate = parsedDate.withDayOfMonth(parsedDate.lengthOfMonth());
        } // 그외 경우 일별 조회
        else if (date != null && !date.endsWith("00") && fromDate == null && toDate == null) {
            startDate = LocalDate.parse(date);
            endDate = startDate;
        }
        // 기간 지정 조회시
        else if (date == null && fromDate != null && toDate != null) {
            startDate = LocalDate.parse(fromDate.substring(0, 10));
            endDate = LocalDate.parse(toDate.substring(0, 10));

            // 시작 날짜가 종료 날짜 보다 작은 경우 검사
            if (startDate.isAfter(endDate)) {
                throw new BusinessLogicException(ExceptionCode.INVALID_DATE_RANGE);
            }
        }
        else {
            log.info("date, fromDate, toDate 모두 null 이거나, YYYY-MM-DD 타입이 아닙니다.");
            throw new BusinessLogicException(ExceptionCode.INVALID_DATE_FORMAT);
        }

        // 2. 지정한 날짜에 대한 쿼리들(태그O, 태그X)
        if (outgoTag != null) { // 태그별 조회
            String decodedTag = URLDecoder.decode(outgoTag, StandardCharsets.UTF_8);
            log.info("DecodedTag To UTF-8 : {}", decodedTag);

            // 태그이름에 대한 검색 쿼리 outgo 쿼리
            List<LedgerTag> tags = ledgerTagRepository.findAllByMemberMemberIdAndTagName(memberId, decodedTag);
            List<Long> outgoIds = tags.stream()
                    .flatMap(tag -> tag.getOutgos().stream()) // 각 LedgerTag의 Outgo 리스트를 스트림으로 평탄화
                    .map(Outgo::getOutgoId)
                    .collect(Collectors.toList());

            if(isWasteList == null){
                outgoPage = outgoRepository.findAllByOutgoIdInAndDateBetween(
                        outgoIds, startDate, endDate, PageRequest.of(page - 1, size, Sort.by("date").descending()));

            }
            else if(isWasteList){
                outgoPage = outgoRepository.findAllByOutgoIdInAndWasteListAndDateBetween(
                        outgoIds, true, startDate, endDate, PageRequest.of(page - 1, size, Sort.by("date").descending()));
            }
        }
        else { // none tag
            if (isWasteList == null){
                outgoPage = outgoRepository.findAllByMemberMemberIdAndDateBetween(
                        memberId, startDate, endDate, PageRequest.of(page - 1, size, Sort.by("date").descending()));
            } else if (isWasteList) {
                outgoPage = outgoRepository.findAllByMemberMemberIdAndWasteListAndDateBetween(
                        memberId, true, startDate, endDate, PageRequest.of(page - 1, size, Sort.by("date").descending()));
            }
        }
        return outgoPage;
    }
    
    // 상단의 페이지생성 메서드오버로딩, 캘린더 대시보드에 출력될 지출의 리스트 생성
    public List<OutgoDto.Response> findOutgoPagesAsList(String token, int page, int size, String date, String outgoTag, Boolean isWasteList){
        Page<Outgo> outgoPage = findOutgoPages(token, page, size, date, null, null, outgoTag, isWasteList);
        return outgoPage.getContent().stream()
                .map(outgoMapper::outgoToOutgoResponseDto)
                .collect(Collectors.toList());
    }

    // 영수증 인식 후 자동작성
    public OutgoDto.ImageOcrResponse convertImageToOutgo(String token, OutgoDto.PostImage outgoDto) throws IOException {
        tokenBlackListService.isBlackListed(token);

        String receiptImageUrl = outgoDto.getReceiptImageUrl();
        String base64Image = clovaOcrApiService.convertImageToBase64(receiptImageUrl);
        ClovaOcrDto callOcrDto = clovaOcrApiService.callOcrApi(base64Image);

        // ocrDto와 outgoDto를 매칭해줌
        log.info(callOcrDto.getDate(),
                callOcrDto.getTotalPrice(),
                callOcrDto.getStoreInfo());

        return new OutgoDto.ImageOcrResponse(
                callOcrDto.getDate(), // date
                callOcrDto.getTotalPrice(), // money
                callOcrDto.getStoreInfo(), // outgoName
                receiptImageUrl
        );
    }

    // 입력 들어온 날짜의 유효성 검증 메서드
    // date만 들어온 경우/ start, end가 들어온 경우 모두 사용 nullable 하게
    private void isValidDate(String date, String startDate, String endDate){
        // 1. 가계부 조회 시 (월간 조회/ 일일 조회)
        if(date != null && startDate == null && endDate == null){
            // 날짜 포맷이 맞는지 재검증(yyyy-mm-dd)
            isValidDateFormat(date);

            int year = Integer.parseInt(date.substring(0, 4));
            int month = Integer.parseInt(date.substring(5, 7));
            int day = Integer.parseInt(date.substring(8, 10));

            // 400 error, 유효하지 않은 월자
            if(month > 12 || month < 1) {
                throw new BusinessLogicException(ExceptionCode.INVALID_MONTH);
            }
            // 400 error, 유효하지 않은 일자
            if(day < 0 || day > getMaxDaysInMonth(month, year)) {
                throw new BusinessLogicException(ExceptionCode.INVALID_DAY);
            }
        }

        // 2. 기간 지정 조회 시, 보다 상세한 에러를 위해 에러 코드 추가
        if(date == null && startDate != null && endDate != null) {
            // 날짜 포맷이 맞는지 재검증 (yyyy-mm-dd)
            isValidDateFormat(startDate);
            isValidDateFormat(endDate);

            int startYear = Integer.parseInt(startDate.substring(0, 4));
            int startMonth = Integer.parseInt(startDate.substring(5, 7));
            int startDay = Integer.parseInt(startDate.substring(8, 10));
            int endYear = Integer.parseInt(endDate.substring(0, 4));
            int endMonth = Integer.parseInt(endDate.substring(5, 7));
            int endDay = Integer.parseInt(endDate.substring(8, 10));

            // 400 error, 유효하지 않은 시작 월자
            if(startMonth > 12 || startMonth < 1) {
                throw new BusinessLogicException(ExceptionCode.INVALID_START_MONTH);
            }

            // 400 error, 유효하지 않은 종료 월자
            if(endMonth > 12 || endMonth < 1) {
                throw new BusinessLogicException(ExceptionCode.INVALID_END_MONTH);
            }

            // 400 error, 유효하지 않은 시작 일자
            if(startDay <= 0 || startDay > getMaxDaysInMonth(startMonth, startYear)) {
                throw new BusinessLogicException(ExceptionCode.INVALID_START_DAY);
            }

            // 400 error, 유효하지 않은 종료 일자
            if(endDay <= 0 || endDay > getMaxDaysInMonth(endMonth, endYear)) {
                throw new BusinessLogicException(ExceptionCode.INVALID_END_DAY);
            }
        }
    }

    // 연도 유효성 검사 메서드 -> 앞뒤로 100년 제한, post 시에만 사용
    // 범위 안에 있다면 true 반환, 범위 밖이라면 false
    private boolean isValidYear(int year) {
        int currentYear = LocalDate.now().getYear(); // 현재 연도
        int minYear = currentYear - 100; // 최소 연도
        int maxYear = currentYear + 100; // 최대 연도

        // 연도 범위 검사
        return year >= minYear && year <= maxYear;
    }

    // 월에 따른 최대 일 수 체크 메서드
    private int getMaxDaysInMonth(int month, int year) {
        return switch (month) {
            case 1, 3, 5, 7, 8, 10, 12 -> 31;
            case 4, 6, 9, 11 -> 30;
            case 2 -> ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) ? 29 : 28; // 윤년일 경우 파악
            default -> 0; // 잘못된 월
        };
    }

    // 날짜를 String으로 받아오기 때문에 형식이 올바른 지 검증하는 메서드
    private static void isValidDateFormat(String date) {
        // 날짜 형식 검증을 위한 정규 표현식
        String datePattern = "^\\d{4}-\\d{2}-\\d{2}$";
        Pattern pattern = Pattern.compile(datePattern);

        // 검증
        if (!pattern.matcher(date).matches()) {
            throw new BusinessLogicException(ExceptionCode.INVALID_DATE_FORMAT);
        }
    }
}
