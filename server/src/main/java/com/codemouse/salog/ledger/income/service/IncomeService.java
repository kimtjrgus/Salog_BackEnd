package com.codemouse.salog.ledger.income.service;

import com.codemouse.salog.auth.jwt.JwtTokenizer;
import com.codemouse.salog.diary.service.DiaryService;
import com.codemouse.salog.dto.MultiResponseDto;
import com.codemouse.salog.exception.BusinessLogicException;
import com.codemouse.salog.exception.ExceptionCode;
import com.codemouse.salog.ledger.income.dto.IncomeDto;
import com.codemouse.salog.ledger.income.entity.Income;
import com.codemouse.salog.ledger.income.mapper.IncomeMapper;
import com.codemouse.salog.ledger.income.repository.IncomeRepository;
import com.codemouse.salog.members.entity.Member;
import com.codemouse.salog.members.service.MemberService;
import com.codemouse.salog.tags.ledgerTags.dto.LedgerTagDto;
import com.codemouse.salog.tags.ledgerTags.entity.LedgerTag;
import com.codemouse.salog.tags.ledgerTags.service.LedgerTagService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@Transactional
@Slf4j
//@EnableCaching
public class IncomeService {
    private final IncomeRepository incomeRepository;
    private final IncomeMapper incomeMapper;
    private final MemberService memberService;
    private final JwtTokenizer jwtTokenizer;
    private final LedgerTagService tagService;
    private final Validator validator;

//    @CacheEvict(value = "getIncomes", allEntries = true)
    public IncomeDto.Response createIncome(String token, IncomeDto.Post incomePostDto) {
        Income income = incomeMapper.incomePostDtoToIncome(incomePostDto);

        Member member = memberService.findVerifiedMember(jwtTokenizer.getMemberId(token));
        income.setMember(member);

        // 태그
        Income savedIncome = tagHandler(incomePostDto.getIncomeTag(), token, income);

        return incomeMapper.incomeToIncomeResponseDto(savedIncome);
    }

//    @CacheEvict(value = "getIncomes", allEntries = true)
    public IncomeDto.Response updateIncome(String token, long incomeId, IncomeDto.Patch incomePatchDto) {

        Income income = incomeMapper.incomePatchDtoToIncome(incomePatchDto);
        Income findIncome = findVerifiedIncome(incomeId);
        memberService.verifiedRequest(token, findIncome.getMember().getMemberId());


        Optional.of(income.getMoney())
                .ifPresent(findIncome::setMoney);
        Optional.of(income.getIncomeName())
                .ifPresent(findIncome::setIncomeName);
        Optional.ofNullable(income.getMemo())
                .ifPresent(findIncome::setMemo);

        // 태그
        Income savedIncome = tagHandler(incomePatchDto.getIncomeTag(), token, findIncome);

        return incomeMapper.incomeToIncomeResponseDto(savedIncome);
    }

//    @Cacheable(value = "getIncomes", key = "#memberId + '_' + #page + '_' + #size + '_' + #incomeTag + '_' + #date")
    /*
    @Cacheable 이 어노테이션의 사용법을 잘 못 알아서 주석 캐싱 기능을 주석 처리함
    위 어노테이션은 각 캐시 키를 적용된 메서드의 매개변수로 부터 받아오게 됨
    즉, 현재 getIncomes 메서드의 매개변수를 token이 아니라 memberId로 수정하면 회원의 식별자로 캐시 키가 정상적으로 만들어짐

    현재 방식대로라면 A 회원이 수입을 조회한 경우 해당 정보가 캐시 메모리에 저장되는데 문제는 memeberId가 null 인 상태이기 때문에
    나머지 page, size, tag, date로 캐시 키가 생성되고, 이로 인해 가장 처음 수입을 조회한 회원의 정보가 캐시 메모리에 저장됨
    그래서 다른 모든 회원이 동일한 page, size, tag, date를 입력해서 수입 조회를 요청하면 해당 캐시 메모리에 있는 데이터를 요청하게 되는 것

    이를 해결하기 위해 컨트롤러에서 부터 token을 memberId로 변환하고 서비스의 조회 메서드에서 memberId를 메개변수로 반아오게 하고,
    이외에 getIncomes 메서드를 사용하는 Calendar 클래스에서도 JwtTokenizer 를 주입하고 구조를 변경해야함
    하지만 getIncomes 메서드만 그렇게 수정하기에는 전체 구조가 일관성이 없기 때문에 현재 캐싱 기능을 비활성화 함
    */
    public MultiResponseDto<IncomeDto.Response> getIncomes(String token, int page, int size, String incomeTag, String date) {
        long memberId = jwtTokenizer.getMemberId(token);

        Page<Income> incomes;

        int[] arr = Arrays.stream(date.split("-")).mapToInt(Integer::parseInt).toArray();
        int year = arr[0];
        int month = arr[1];
        int day = arr[2];

        // 연 유효성 검사
        if (!isValidYear(year)) {
            throw new BusinessLogicException(ExceptionCode.UNVALIDATED_YEAR);
        }

        // 월 유효성 검사
        if (month < 1 || month > 12) {
            throw new BusinessLogicException(ExceptionCode.UNVALIDATED_MONTH);
        }

        // 일 유효성 검사
        if (day < 0 || day > getMaxDaysInMonth(month, year)) {
            throw new BusinessLogicException(ExceptionCode.UNVALIDATED_DAY);
        }

        if (incomeTag != null) {
            String decodedTag = URLDecoder.decode(incomeTag, StandardCharsets.UTF_8);
            log.info("DecodedTag To UTF-8 : {}", decodedTag);

            if (day == 0) {
                incomes = incomeRepository.findByMonthAndTag(memberId, year, month, decodedTag,
                            PageRequest.of(page - 1, size, Sort.by("date").descending()));
            } else {
                incomes = incomeRepository.findByDateAndTag(memberId, year, month, day, decodedTag,
                            PageRequest.of(page - 1, size, Sort.by("date").descending()));
            }
        } else {
            if (day == 0) {
                incomes = incomeRepository.findByMonth(memberId, year, month,
                        PageRequest.of(page - 1, size, Sort.by("date").descending()));
            } else {
                incomes = incomeRepository.findByDate(memberId, year, month, day,
                        PageRequest.of(page - 1, size, Sort.by("date").descending()));
            }
        }


        List<IncomeDto.Response> incomeList = incomes.getContent().stream()
                .map(incomeMapper::incomeToIncomeResponseDto)
                .collect(Collectors.toList());

        return new MultiResponseDto<>(incomeList, incomes);
    }

    public MultiResponseDto<IncomeDto.Response> getIncomesByDateRange(String token, int page, int size, String startDate, String endDate) {
        long memberId = jwtTokenizer.getMemberId(token);

        // start dates
        int[] startDateArr = Arrays.stream(startDate.split("-")).mapToInt(Integer::parseInt).toArray();
        int startYear = startDateArr[0];
        int startMonth = startDateArr[1];
        int startDay = startDateArr[2];

        // end dates
        int[] endDateArr = Arrays.stream(endDate.split("-")).mapToInt(Integer::parseInt).toArray();
        int endYear = endDateArr[0];
        int endMonth = endDateArr[1];
        int endDay = endDateArr[2];

        /*
            시작, 종료, 연, 월, 일을 보다 명확하게 구분해서 에러 난 부분을 캐치하기 위해
            전부 분리해서 로직 작성 했습니다.
            + 에러 코드도 이에 맞춰 추가 했습니다.
        */
        // 연 유효성 검사
        if (!isValidYear(startYear)) {
            throw new BusinessLogicException(ExceptionCode.UNVALIDATED_START_YEAR);
        }

        if (!isValidYear(endYear)) {
            throw new BusinessLogicException(ExceptionCode.UNVALIDATED_END_YEAR);
        }

        // 시작 월 유효성 검사
        if (startMonth < 1 || startMonth > 12) {
            throw new BusinessLogicException(ExceptionCode.UNVALIDATED_START_MONTH);
        }

        // 종료 월 유효성 검사
        if (endMonth < 1 || endMonth > 12) {
            throw new BusinessLogicException(ExceptionCode.UNVALIDATED_END_MONTH);
        }

        // 시작 일 유효성 검사
        if (startDay < 1 || startDay > getMaxDaysInMonth(startMonth, startYear)) {
            throw new BusinessLogicException(ExceptionCode.UNVALIDATED_START_DAY);
        }

        // 종료 일 유효성 검사
        if (endDay < 1 || endDay > getMaxDaysInMonth(endMonth, endYear)) {
            throw new BusinessLogicException(ExceptionCode.UNVALIDATED_END_DAY);
        }

        LocalDate start = LocalDate.of(startYear, startMonth, startDay);
        LocalDate end = LocalDate.of(endYear, endMonth, endDay);

        // 시작 날짜가 종료 날짜 보다 작은 경우 검사
        if (start.isAfter(end)) {
            throw new BusinessLogicException(ExceptionCode.INVALID_DATE_RANGE);
        }

        Page<Income> incomes = incomeRepository.findByDateRange(memberId, start, end,
                PageRequest.of(page - 1, size, Sort.by("date")));

        List<IncomeDto.Response> incomeList = incomes.getContent().stream()
                .map(incomeMapper::incomeToIncomeResponseDto)
                .collect(Collectors.toList());

        return new MultiResponseDto<>(incomeList, incomes);
    }

    public IncomeDto.MonthlyResponse getMonthlyIncome(String token, String date) {
        long memberId = jwtTokenizer.getMemberId(token);

        int[] arr = Arrays.stream(date.split("-")).mapToInt(Integer::parseInt).toArray();
        int year = arr[0];
        int month = arr[1];

        // 연 유효성 검사
        if (!isValidYear(year)) {
            throw new BusinessLogicException(ExceptionCode.UNVALIDATED_YEAR);
        }

        if (month > 12 || month < 1) {
            throw new BusinessLogicException(ExceptionCode.UNVALIDATED_MONTH);
        }

        // 월별 태그 합계 계산
        List<Object[]> totalIncomeByTag = incomeRepository.findTotalIncomeByMonthGroupByTag(memberId, year, month);

        // 계산된 태그 합계를 삽입
        List<LedgerTagDto.MonthlyResponse> tagIncomes = totalIncomeByTag.stream()
                .map(obj -> new LedgerTagDto.MonthlyResponse((String) obj[0], obj[1] != null ? (Long) obj[1] : 0))
                .collect(Collectors.toList());

        // 월별 수입 합계 계산
        Long monthlyIncome = incomeRepository.findTotalIncomeByMonth(memberId, year, month);

        return new IncomeDto.MonthlyResponse(
                monthlyIncome != null ? monthlyIncome.intValue() : 0,
                tagIncomes
        );
    }

//    @CacheEvict(value = "getIncomes", allEntries = true)
    public void deleteIncome(String token, long incomeId) {

        Income income = findVerifiedIncome(incomeId);

        memberService.verifiedRequest(token, income.getMember().getMemberId());

        LedgerTag ledgerTag = income.getLedgerTag();

        // 삭제하려는 수입에만 연결된 태그인 경우, 태그 삭제
        if (ledgerTag != null && ledgerTag.getIncomes().size() == 1) {
            tagService.deleteLedgerTag(token, ledgerTag.getLedgerTagId());
        }

        // 수입과 태그의 연결을 끊음
        income.setLedgerTag(null);

        incomeRepository.delete(income);
    }

    public Income findVerifiedIncome(long incomeId) {
        return incomeRepository.findById(incomeId)
                .orElseThrow(
                        () -> new BusinessLogicException(ExceptionCode.INCOME_NOT_FOUND)
                );
    }

    public long getDailyTotalIncome(String token, LocalDate curDate){
        long memberId = jwtTokenizer.getMemberId(token);

        return Optional.ofNullable(
                incomeRepository.findTotalIncomeByDay(memberId, curDate)
                ).orElse(0L); // 쿼리결과 null 값이 반환될 경우 0이 대신 반환
    }

    // 태그 등록, 중복 체크
    private Income tagHandler(String incomePostDto, String token, Income income) {
        LedgerTag ledgerTag = null;

        if (incomePostDto != null) {
            String tagName = incomePostDto;

            LedgerTag existTag = tagService.findLedgerTagByMemberIdAndTagName(token, tagName, LedgerTag.Group.INCOME);

            if (existTag != null) {
                if (existTag.getCategory() == LedgerTag.Group.INCOME) {
                    ledgerTag = existTag;
                }
            } else {
                LedgerTagDto.Post tagPost = new LedgerTagDto.Post(tagName, LedgerTag.Group.INCOME);

                Set<ConstraintViolation<LedgerTagDto.Post>> violations = validator.validate(tagPost);
                if (!violations.isEmpty()) {
                    throw new BusinessLogicException(ExceptionCode.TAG_UNVALIDATED);
                }

                ledgerTag = tagService.postLedgerTag(token, tagPost);
            }
        }

        // 만약 ledgerTag가 null이 아니라면 Income 객체에 설정
        if (ledgerTag != null) {
            income.setLedgerTag(ledgerTag);
        }

        Income savedIncome = incomeRepository.save(income);
        tagService.deleteUnusedIncomeTagsByMemberId(token);

        return savedIncome;
    }

    // 월에 따른 최대 일 수 체크 메서드
    private int getMaxDaysInMonth(int month, int year) {
        return switch (month) {
            case 1, 3, 5, 7, 8, 10, 12 -> 31;
            case 4, 6, 9, 11 -> 30;
            case 2 -> (isLeapYear(year)) ? 29 : 28;
            default -> 0; // 잘못된 월
        };
    }

    // 연도 유효성 검사 메서드
    private boolean isValidYear(int year) {
        int currentYear = LocalDate.now().getYear(); // 현재 연도
        int minYear = currentYear - 100; // 최소 연도
        int maxYear = currentYear + 100; // 최대 연도

        // 음수 연도 제한
        if (year < 0) {
            return false;
        }

        // 연도 범위 검사
        return year >= minYear && year <= maxYear;
    }

    // 윤년 여부 판단 메서드
    private boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }
}
