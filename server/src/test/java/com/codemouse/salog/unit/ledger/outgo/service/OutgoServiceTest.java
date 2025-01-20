package com.codemouse.salog.unit.ledger.outgo.service;

import com.codemouse.salog.auth.jwt.JwtTokenizer;
import com.codemouse.salog.auth.utils.TokenBlackListService;
import com.codemouse.salog.dto.MultiResponseDto;
import com.codemouse.salog.exception.BusinessLogicException;
import com.codemouse.salog.exception.ExceptionCode;
import com.codemouse.salog.helper.naverOcr.ClovaOcrApiService;
import com.codemouse.salog.helper.naverOcr.ClovaOcrDto;
import com.codemouse.salog.ledger.income.dto.IncomeDto;
import com.codemouse.salog.ledger.income.service.IncomeService;
import com.codemouse.salog.ledger.outgo.dto.OutgoDto;
import com.codemouse.salog.ledger.outgo.entity.Outgo;
import com.codemouse.salog.ledger.outgo.mapper.OutgoMapper;
import com.codemouse.salog.ledger.outgo.repository.OutgoRepository;
import com.codemouse.salog.ledger.outgo.service.OutgoService;
import com.codemouse.salog.members.entity.Member;
import com.codemouse.salog.members.service.MemberService;
import com.codemouse.salog.tags.ledgerTags.dto.LedgerTagDto;
import com.codemouse.salog.tags.ledgerTags.entity.LedgerTag;
import com.codemouse.salog.tags.ledgerTags.repository.LedgerTagRepository;
import com.codemouse.salog.tags.ledgerTags.service.LedgerTagService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.validation.Validator;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("지출 서비스 유닛 테스트")
public class OutgoServiceTest {
    @InjectMocks
    private OutgoService outgoService;
    @Mock
    private OutgoRepository outgoRepository;
    @Mock
    private OutgoMapper outgoMapper;
    @Mock
    private MemberService memberService;
    @Mock
    private JwtTokenizer jwtTokenizer;
    @Mock
    private IncomeService incomeService; // 연단위 월별 수입, 지출 총합계 조회를 위한 의존성 주입
    @Mock
    private TokenBlackListService tokenBlackListService;
    @Mock
    private LedgerTagService ledgerTagService;
    @Mock
    private LedgerTagRepository ledgerTagRepository;
    @Mock
    private ClovaOcrApiService clovaOcrApiService;

    @Mock
    private Validator validator;

    private Member member;
    private Outgo outgo;
    private LedgerTag ledgerTag;

    @BeforeEach
    void setup(){
        member = new Member();
        outgo = new Outgo();
        outgo.setMember(member);
        outgo.setDate(LocalDate.now());
        ledgerTag = new LedgerTag();
    }

    /* 지출 서비스 유닛 테스트 목차
    *  1. 지출 생성 - 태그조건, 연도 유효성 검사
    *  2. 지출 수정
    *  3. 지출 조회 - 월,일,기간 지정 (낭비리스트 조건, 태그 조건)
    *              - 예외처리
    *  4. 지출 합계 조회 - 월간 지출 합계 조회 (낭비리스트 조건)
    *                  - 연간 총 수입, 지출 통계 조회
    *  5. 지출 삭제
    *
    *  + private메서드는 해당 메서드를 사용 중인  public메서드로 간접테스트 진행
    * */

    /* 지출 생성 목차
    *  1. createOutgo + tagHandler 1 : 태그가 null이 아니고, 태그를 새로 생성하는 경우
    *  2. createOutgo + tagHandler 2 : 태그가 null이 아니고, 태그가 이미 생성되어있는 경우
    *  3. createOutgo + InvalidYear : 연도 유효성 검사 실패
    *
    *  + 태그가 null인 조건은 dto에서 pattern 어노테이션 위반으로 통합테스트에서 테스트 진행
    *
    * */

    @Test
    @DisplayName("createOutgo + tagHandler 1 : 태그가 null이 아니고, 태그를 새로 생성하는 경우")
    @Order(1)
    void createOutgoTest1() {

        // 1. Given - 테스트 수행을 위한 준비 단계, Mock 객체와 입력 데이터를 준비
        String testToken = "testToken";
        String tagName = "validTag";

        // Mock 설정
        OutgoDto.Post postDto = new OutgoDto.Post();
        postDto.setOutgoTag(tagName);
        postDto.setDate(LocalDate.now());

        OutgoDto.Response responseDto = new OutgoDto.Response();
        ledgerTag.setTagName(tagName);

        // 2. When - 동작 수행
        doNothing().when(tokenBlackListService).isBlackListed(testToken); // 블랙리스트 검증 (예외 없음)
        when(jwtTokenizer.getMemberId(testToken)).thenReturn(1L);
        when(memberService.findVerifiedMember(1L)).thenReturn(member);
        when(outgoMapper.outgoPostDtoToOutgo(postDto)).thenReturn(outgo); // DTO -> Outgo 매핑
        when(ledgerTagService.findLedgerTagByMemberIdAndTagName(testToken, tagName, LedgerTag.Group.OUTGO)).thenReturn(null); // 태그가 존재하지 않는 경우
        when(ledgerTagService.postLedgerTag(eq(testToken), any(LedgerTagDto.Post.class))).thenReturn(ledgerTag); // 태그 새로 생성
        when(outgoRepository.save(outgo)).thenReturn(outgo); // Outgo 저장
        when(outgoMapper.outgoToOutgoResponseDto(outgo)).thenReturn(responseDto); // 저장된 Outgo -> Response DTO

        OutgoDto.Response result = outgoService.createOutgo(testToken, postDto);

        // 3. Then - 검증
        assertNotNull(result); // 결과가 null이 아니어야 함
        assertEquals(member, outgo.getMember()); // Outgo에 설정된 Member 검증
        assertEquals(tagName, ledgerTag.getTagName()); // LedgerTag의 태그 이름 검증
        assertEquals(postDto.getDate(), outgo.getDate());
        assertEquals(responseDto, result); // 반환된 Response DTO 검증

        // Mock 호출 검증
        verify(tokenBlackListService, times(1)).isBlackListed(testToken); // 블랙리스트 검증 호출 확인
        verify(jwtTokenizer, times(1)).getMemberId(testToken);
        verify(memberService, times(1)).findVerifiedMember(1L);
        verify(outgoMapper, times(1)).outgoPostDtoToOutgo(postDto);
        verify(ledgerTagService, times(1)).findLedgerTagByMemberIdAndTagName(testToken, tagName, LedgerTag.Group.OUTGO);
        verify(ledgerTagService, times(1)).postLedgerTag(eq(testToken), any(LedgerTagDto.Post.class));
        verify(outgoRepository, times(1)).save(outgo);
        verify(outgoMapper, times(1)).outgoToOutgoResponseDto(outgo);

    }

    @Test
    @DisplayName("createOutgo + tagHandler 2 : 태그가 null이 아니고, 태그가 이미 생성되어있는 경우")
    @Order(2)
    void createOutgoTest2(){

        // 1. Given
        String testToken = "testToken";
        String tagName = "existingTagName";

        OutgoDto.Post postDto = new OutgoDto.Post();
        postDto.setOutgoTag(tagName);
        postDto.setDate(LocalDate.now());

        OutgoDto.Response responseDto = new OutgoDto.Response();
        ledgerTag.setTagName(tagName); // 이미 생성된 태그 설정

        // Mock 설정
        doNothing().when(tokenBlackListService).isBlackListed(testToken); // 블랙리스트 검증 (예외 없음)
        when(jwtTokenizer.getMemberId(testToken)).thenReturn(1L);
        when(memberService.findVerifiedMember(1L)).thenReturn(member);
        when(outgoMapper.outgoPostDtoToOutgo(postDto)).thenReturn(outgo); // DTO -> Outgo 매핑
        when(ledgerTagService.findLedgerTagByMemberIdAndTagName(testToken, tagName, LedgerTag.Group.OUTGO)).thenReturn(ledgerTag); // 태그가 이미 존재
        when(outgoRepository.save(outgo)).thenReturn(outgo); // Outgo 저장
        when(outgoMapper.outgoToOutgoResponseDto(outgo)).thenReturn(responseDto); // 저장된 Outgo -> Response DTO

        // 2. When
        OutgoDto.Response result = outgoService.createOutgo(testToken, postDto);

        // 3. Then
        assertNotNull(result); // 결과가 null이 아니어야 함
        assertEquals(member, outgo.getMember()); // Outgo에 설정된 Member 검증
        assertEquals(tagName, ledgerTag.getTagName()); // LedgerTag의 태그 이름 검증
        assertEquals(responseDto, result); // 반환된 Response DTO 검증

        // Mock 호출 검증
        verify(tokenBlackListService, times(1)).isBlackListed(testToken); // 블랙리스트 검증 호출 확인
        verify(jwtTokenizer, times(1)).getMemberId(testToken);
        verify(memberService, times(1)).findVerifiedMember(1L);
        verify(outgoMapper, times(1)).outgoPostDtoToOutgo(postDto);
        verify(ledgerTagService, times(1)).findLedgerTagByMemberIdAndTagName(testToken, tagName, LedgerTag.Group.OUTGO); // 기존 태그 확인
        verify(ledgerTagService, times(0)).postLedgerTag(anyString(), any(LedgerTagDto.Post.class)); // 태그 생성 메서드는 호출되지 않아야 함
        verify(outgoRepository, times(1)).save(outgo);
        verify(outgoMapper, times(1)).outgoToOutgoResponseDto(outgo);
    }

    @Test
    @DisplayName("createOutgo + InvalidYear : 연도 유효성 검사 실패")
    @Order(3)
    void createOutgoTest_InvalidYear() {
        // 1. Given
        String testToken = "testToken";
        int invalidYear = LocalDate.now().getYear() + 101; // 범위 밖의 연도
        OutgoDto.Post postDto = new OutgoDto.Post();
        postDto.setOutgoTag("validTag");
        postDto.setDate(LocalDate.of(invalidYear, 1, 1)); // 유효하지 않은 연도 설정

        // Mock 설정
        doNothing().when(tokenBlackListService).isBlackListed(testToken); // Token 블랙리스트 검증 Mock

        // 2. When/ Then
        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> outgoService.createOutgo(testToken, postDto)
        );

        // 예외 메시지 검증
        assertEquals(ExceptionCode.INVALID_YEAR.getMessage(), exception.getMessage());

        // Mock 호출 검증
        verify(tokenBlackListService, times(1)).isBlackListed(testToken); // Token 검증은 호출됨
        verify(outgoMapper, never()).outgoPostDtoToOutgo(postDto);        // 이후 로직은 호출되지 않음
        verify(outgoRepository, never()).save(any(Outgo.class));         // 저장 호출되지 않음
    }

    // 지출 수정
    @Test
    @DisplayName("patchOutgo : 지출 수정 성공")
    @Order(4)
    void patchOutgoTest() {
        // 1. Given
        String token = "testToken";
        long outgoId = 1L;

        OutgoDto.Patch patchDto = new OutgoDto.Patch();
        patchDto.setDate(LocalDate.now());
        patchDto.setOutgoName("Updated Outgo Name");
        patchDto.setMoney(2000);
        patchDto.setPayment("카드");
        patchDto.setMemo("Updated Memo");
        patchDto.setReceiptImg("updated-receipt.png");
        patchDto.setWasteList(true);

        Outgo mappedOutgo = new Outgo();
        mappedOutgo.setDate(patchDto.getDate());
        mappedOutgo.setOutgoName(patchDto.getOutgoName());
        mappedOutgo.setMoney(patchDto.getMoney());
        mappedOutgo.setPayment(patchDto.getPayment());
        mappedOutgo.setMemo(patchDto.getMemo());
        mappedOutgo.setReceiptImg(patchDto.getReceiptImg());
        mappedOutgo.setWasteList(patchDto.isWasteList());

        // Mock
        doNothing().when(tokenBlackListService).isBlackListed(token);
        when(outgoMapper.outgoPatchDtoToOutgo(patchDto)).thenReturn(mappedOutgo);
        when(outgoRepository.findById(outgoId)).thenReturn(Optional.of(outgo));
        doNothing().when(memberService).verifiedRequest(token, member.getMemberId());
        when(outgoRepository.save(outgo)).thenReturn(outgo);
        when(outgoMapper.outgoToOutgoResponseDto(outgo))
                .thenReturn(new OutgoDto.Response(
                        outgoId,
                        mappedOutgo.getDate(),
                        mappedOutgo.getMoney(),
                        mappedOutgo.getOutgoName(),
                        mappedOutgo.getPayment(),
                        mappedOutgo.getMemo(),
                        new LedgerTagDto.Response(
                                ledgerTag.getLedgerTagId(), // LedgerTag ID
                                ledgerTag.getTagName() // LedgerTag 이름
                        ),
                        mappedOutgo.isWasteList(),
                        mappedOutgo.getReceiptImg()
                ));
        // 2. When
        OutgoDto.Response response = outgoService.patchOutgo(token, outgoId, patchDto);

        // 3. Then
        assertNotNull(response);
        assertEquals(LocalDate.now(), response.getDate());
        assertEquals("Updated Outgo Name", response.getOutgoName());
        assertEquals(2000, response.getMoney());
        assertEquals("카드", response.getPayment());
        assertEquals("Updated Memo", response.getMemo());
        assertEquals("updated-receipt.png", response.getReceiptImg());
        assertTrue(response.isWasteList());

        verify(tokenBlackListService).isBlackListed(token);
        verify(memberService).verifiedRequest(token, member.getMemberId());
        verify(outgoRepository).save(outgo);
        verify(outgoMapper).outgoToOutgoResponseDto(outgo);
    }

    /* 지출 조회 목차
    *  1. date 로 월별 조회 테스트
    *  2. date 로 일별 조회 테스트
    *  3. fromDate, toDate 로 기간 지정 조회 테스트
    *  4. 태그조건 - 태그 조건이 있을 때
    *     (태그조건 - 태그 조건이 없을 때 > 1,2,3 의 경우와 같아서 생략)
    *  5. 낭비리스트 조건 - isWasteList 가 true 일 경우
    *     (낭비리스트 조건 - isWasteList 가 false 일 경우 >  1,2,3 의 경우와 같아서 생략)
    *
    *  6. 예외처리
    *  6-1. 잘못된 날짜 형식 입력, 조회 실패
    *  6-2. date, fromDate, toDate 모두 null 일 경우
    *  6-3. fromDate > toDate 일 경우
    *
    *  + 월별 조회시 일의 입력값을 00으로 주기로 약속 ex) '2025-01-00'
    * */
    @Test
    @DisplayName("findAllOutgos1_ByMonth : 지출 페이지 생성, 월별 조회")
    @Order(5)
    void findAllOutgosTest1_ByMonth() {
        // 1. Given
        String token = "testToken";
        int page = 1, size = 10;
        String date = "2025-01-00"; // 월별 조회

        LocalDate parsedDate = LocalDate.parse(date.substring(0, 7) + "-01");
        LocalDate startDate = parsedDate.withDayOfMonth(1);
        LocalDate endDate = parsedDate.withDayOfMonth(parsedDate.lengthOfMonth());

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("date").descending());

        when(jwtTokenizer.getMemberId(token)).thenReturn(1L); // memberId를 1L로 설정
        when(outgoRepository.findAllByMemberMemberIdAndDateBetween(1L, startDate, endDate, pageRequest))
                .thenReturn(new PageImpl<>(List.of(outgo)));

        // 2. When
        MultiResponseDto<OutgoDto.Response> result = outgoService.findAllOutgos(token, page, size, date, null);

        // 3. Then
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(outgoRepository, times(1))
                .findAllByMemberMemberIdAndDateBetween(1L, startDate, endDate, PageRequest.of(page - 1, size, Sort.by("date").descending()));
    }

    @Test
    @DisplayName("findAllOutgos2_ByDay : 지출 페이지 생성, 일별 조회")
    @Order(6)
    void findAllOutgosTest2_ByDay(){
        // 1. Given
        String token = "testToken";
        int page = 1, size = 10;
        String date = "2025-01-01"; // 특정 일자 조회

        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = startDate;

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("date").descending());
        when(jwtTokenizer.getMemberId(token)).thenReturn(0L);
        when(outgoRepository.findAllByMemberMemberIdAndDateBetween(0L, startDate, endDate, pageRequest))
                .thenReturn(new PageImpl<>(List.of(outgo)));

        // 2. When
        MultiResponseDto<OutgoDto.Response> result = outgoService.findAllOutgos(token, page, size, date, null);

        // 3. Then
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(outgoRepository, times(1))
                .findAllByMemberMemberIdAndDateBetween(0L, startDate, endDate, pageRequest);
    }

    @Test
    @DisplayName("findAllOutgos3_ByDateRange : 지출 페이지 생성, 기간 지정 조회")
    @Order(7)
    void findAllOutgosTest3_ByDateRange() {
        // 1. Given
        String token = "testToken";
        int page = 1, size = 10;
        String fromDate = "2025-01-01";
        String toDate = "2025-01-31";

        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("date").descending());
        when(jwtTokenizer.getMemberId(token)).thenReturn(0L);
        when(outgoRepository.findAllByMemberMemberIdAndDateBetween(0L, startDate, endDate, pageRequest))
                .thenReturn(new PageImpl<>(List.of(outgo)));

        // 2. When
        MultiResponseDto<OutgoDto.Response> result = outgoService.findOutgosByDateRange(token, page, size, fromDate, toDate, null);

        // 3. Then
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(outgoRepository, times(1))
                .findAllByMemberMemberIdAndDateBetween(0L, startDate, endDate, pageRequest);
    }

    @Test
    @DisplayName("findAllOutgos4_byTag : 지출 페이지 생성, 태그별 조회")
    @Order(8)
    void findAllOutgosTest4_byTag() {
        // 1. Given
        String token = "testToken";
        int page = 1, size = 10;
        String date = "2025-01-00"; // 월별 조회
        String tagName = "tagName";

        LocalDate parsedDate = LocalDate.parse(date.substring(0, 7) + "-01");
        LocalDate startDate = parsedDate.withDayOfMonth(1);
        LocalDate endDate = parsedDate.withDayOfMonth(parsedDate.lengthOfMonth());

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("date").descending());

        List<Long> outgoIds = List.of(1L, 2L, 3L); // Mocked tag ID list
        List<LedgerTag> tags = outgoIds.stream()
                .map(id -> {
                    LedgerTag tag = new LedgerTag();
                    List<Outgo> outgos = List.of(new Outgo()); // Mocked Outgo objects
                    outgos.get(0).setOutgoId(id);
                    tag.setOutgos(outgos);
                    return tag;
                })
                .collect(Collectors.toList());

        when(jwtTokenizer.getMemberId(token)).thenReturn(0L);
        when(ledgerTagRepository.findAllByMemberMemberIdAndTagName(0L, tagName)).thenReturn(tags);
        when(outgoRepository.findAllByOutgoIdInAndDateBetween(outgoIds, startDate, endDate, pageRequest))
                .thenReturn(new PageImpl<>(List.of(new Outgo())));

        // 2. When
        MultiResponseDto<OutgoDto.Response> result = outgoService.findAllOutgos(token, page, size, date, tagName);

        // 3. Then
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(outgoRepository, times(1))
                .findAllByOutgoIdInAndDateBetween(outgoIds, startDate, endDate, pageRequest);
    }

    @Test
    @DisplayName("findAllWasteLists : 지출 페이지 생성, 낭비리스트 조건, 월별 조회")
    @Order(9)
    void findAllOutgosTest5_byWasteList() {
        // 1. Given
        String token = "testToken";
        int page = 1, size = 10;
        String date = "2025-01-00"; // 월별 조회
        boolean isWasteList = true;

        LocalDate parsedDate = LocalDate.parse(date.substring(0, 7) + "-01");
        LocalDate startDate = parsedDate.withDayOfMonth(1);
        LocalDate endDate = parsedDate.withDayOfMonth(parsedDate.lengthOfMonth());

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("date").descending());

        when(jwtTokenizer.getMemberId(token)).thenReturn(0L);
        when(outgoRepository.findAllByMemberMemberIdAndWasteListAndDateBetween(0L, true, startDate, endDate, pageRequest))
                .thenReturn(new PageImpl<>(List.of(outgo)));

        // 2. When
        MultiResponseDto<OutgoDto.Response> result = outgoService.findAllWasteLists(token, page, size, date, null);

        // 3. Then
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(outgoRepository, times(1))
                .findAllByMemberMemberIdAndWasteListAndDateBetween(0L, isWasteList, startDate, endDate, pageRequest);
    }

    // - 지출 조회 : 예외 처리 -
    @Test
    @DisplayName("findAllOutgos1 : 유효하지 않은 월 입력 예외 처리")
    @Order(11)
    void findAllOutgosTest_InvalidMonth() {
        // 1. Given
        String token = "testToken";
        int page = 1, size = 10;
        String date = "2025-13-01"; // 잘못된 월

        when(jwtTokenizer.getMemberId(token)).thenReturn(0L);

        // 2. When/ Then
        assertThrows(BusinessLogicException.class, () -> outgoService.findAllOutgos(token, page, size, date, null));
    }

    @Test
    @DisplayName("findAllOutgos2 : 유효하지 않은 일 입력 예외 처리")
    @Order(12)
    void findAllOutgosTest_InvalidDay() {
        // 1. Given
        String token = "testToken";
        int page = 1, size = 10;
        String date = "2025-01-32"; // 잘못된 일

        when(jwtTokenizer.getMemberId(token)).thenReturn(0L);

        // 2. When/ Then
        assertThrows(BusinessLogicException.class, () -> outgoService.findAllOutgos(token, page, size, date, null));
    }

    @Test
    @DisplayName("findAllOutgos3_withNullDates : date, fromDate, toDate 모두 null 예외 처리")
    @Order(13)
    void findOutgosTest_withNullDates() {
        // 1. Given
        String token = "testToken";
        int page = 1, size = 10;

        // 2. When/ Then
        assertThrows(BusinessLogicException.class, () -> {
            outgoService.findAllOutgos(token, page, size, null, null);
        });
        assertThrows(BusinessLogicException.class, () -> {
            outgoService.findOutgosByDateRange(token, page, size, null, null, null);
        });
    }

    @Test
    @DisplayName("findAllOutgos4_withInvalidDateRange : fromDate > toDate 예외 처리")
    @Order(14)
    void findOutgosTest_withInvalidDateRange() {
        // 1. Given
        String token = "testToken";
        int page = 1, size = 10;
        String fromDate = "2025-01-31";
        String toDate = "2025-01-01";

        // 2. When/ Then
        assertThrows(BusinessLogicException.class, () -> {
            outgoService.findOutgosByDateRange(token, page, size, fromDate, toDate, null);
        });
    }

    @Test
    @DisplayName("getSumOfOutgoLists : 월별 지출 합계 조회, 유효한 날짜 입력")
    @Order(15)
    void getSumOfOutgoListsTest_ValidMonth() {
        // 1. Given
        String token = "testToken";
        String date = "2025-01-00"; // 월별 조회
        long memberId = 1L;

        when(jwtTokenizer.getMemberId(token)).thenReturn(memberId);
        when(outgoRepository.getSumOfOutgoListsByTag(memberId, 2025, 1))
                .thenReturn(List.of(new Object[]{"출금", 1000L}, new Object[]{"교통", 500L}));
        when(outgoRepository.findTotalOutgoByMonth(memberId, 2025, 1)).thenReturn(1500L);

        // 2. When
        OutgoDto.MonthlyResponse result = outgoService.getSumOfOutgoLists(token, date);

        // 3. Then
        assertNotNull(result);
        assertEquals(1500L, result.getMonthlyTotal());
        assertEquals(2, result.getTags().size());
        assertEquals("출금", result.getTags().get(0).getTagName());
        assertEquals(1000L, result.getTags().get(0).getTagSum());
        verify(outgoRepository, times(1)).getSumOfOutgoListsByTag(memberId, 2025, 1);
    }

    @Test
    @DisplayName("getSumOfWasteLists : 월별 낭비리스트 합계 조회, 유효한 날짜 입력")
    @Order(16)
    void getSumOfWasteListsTest_ValidMonth() {
        // 1. Given
        String token = "testToken";
        String date = "2025-01-00"; // 월별 조회
        long memberId = 1L;

        when(jwtTokenizer.getMemberId(token)).thenReturn(memberId);
        when(outgoRepository.getSumOfWasteListsByTag(memberId, 2025, 1))
                .thenReturn(List.of(new Object[]{"쇼핑", 2000L}, new Object[]{"쇼핑", 800L}));

        // 2. When
        OutgoDto.MonthlyResponse result = outgoService.getSumOfWasteLists(token, date);

        // 3. Then
        assertNotNull(result);
        assertEquals(2800L, result.getMonthlyTotal());
        assertEquals(2, result.getTags().size());
        assertEquals("쇼핑", result.getTags().get(0).getTagName());
        assertEquals(2000L, result.getTags().get(0).getTagSum());
        verify(outgoRepository, times(1)).getSumOfWasteListsByTag(memberId, 2025, 1);
    }

    @Test
    @DisplayName("getLedgerSumByMonth : 연단위 월별 수입, 지출 총합계 조회")
    @Order(17)
    void getLedgerSumByMonthTest() {
        // 1. Given
        String token = "testToken";
        String date = "2025-01-00"; // 월별 조회

        long memberId = 1L;

        // Mock 설정
        when(jwtTokenizer.getMemberId(token)).thenReturn(memberId);

        for (int i = 0; i < 12; i++) {
            int currentYear = 2025;
            int currentMonth = 1 - i;

            while (currentMonth <= 0) {
                currentYear -= 1;
                currentMonth += 12;
            }

            String formattedDate = String.format("%d-%02d", currentYear, currentMonth);

            when(incomeService.getMonthlyIncome(token, formattedDate))
                    .thenReturn(new IncomeDto.MonthlyResponse(5000L, new ArrayList<>()));
            when(outgoRepository.findTotalOutgoByMonth(memberId, currentYear, currentMonth))
                    .thenReturn(3000L);
        }

        // 2. When
        List<OutgoDto.YearlyResponse> result = outgoService.getLedgerSumByMonth(token, date);

        // 3. Then
        assertNotNull(result);
        assertEquals(12, result.size()); // 총 12개월 데이터 확인
        assertEquals("2025-01", result.get(0).getDate()); // 첫 번째 결과의 날짜 확인
        assertEquals(5000L, result.get(0).getMonthlyIncome());
        assertEquals(3000L, result.get(0).getMonthlyOutgo());

        verify(jwtTokenizer, times(1)).getMemberId(token);
        verify(incomeService, times(12)).getMonthlyIncome(eq(token), anyString());
        verify(outgoRepository, times(12)).findTotalOutgoByMonth(eq(memberId), anyInt(), anyInt());
    }

    /* - 지출 데이터 삭제 -
    * 1. 같은 이름의 태그를 공유하는 지출이 여러 개 있는 경우
    * 2. 같은 이름의 태그를 공유하지 않고 단 1개의 지출만 있는 경우
    * 3. 예외 - 회원 검증 실패 시 예외 발생
    *
    *  + 태그가 없거나 하나의 지출에 태그가 여러 개인 경우는 존재하지 않음
    * */

    @Test
    @DisplayName("deleteOutgo1 : 여러 지출이 하나의 태그이름을 공유하는 경우")
    @Order(18)
    void deleteOutgoTest1 (){
        // 1. Given
        String token = "testToken";
        long outgoId = 1L; // 삭제 대상 지출 ID

        // 태그와 연결된 여러 지출 데이터 생성
        Outgo outgo1 = new Outgo();
        outgo1.setOutgoId(outgoId);
        outgo1.setMember(member);
        outgo1.setLedgerTag(ledgerTag);

        Outgo outgo2 = new Outgo();
        outgo2.setOutgoId(2L);
        outgo2.setMember(member);
        outgo2.setLedgerTag(ledgerTag);

        Outgo outgo3 = new Outgo();
        outgo3.setOutgoId(3L);
        outgo3.setMember(member);
        outgo3.setLedgerTag(ledgerTag);

        // LedgerTag에 지출 목록 설정
        ledgerTag.setLedgerTagId(1L);
        ledgerTag.setOutgos(Arrays.asList(outgo1, outgo2, outgo3));

        // Mock 설정
        when(outgoRepository.findById(outgoId)).thenReturn(Optional.of(outgo1));

        // 태그 삭제가 호출되지 않아 제외
        // doNothing().when(ledgerTagService).deleteLedgerTag(token, ledgerTag.getLedgerTagId());

        // 2. When
        outgoService.deleteOutgo(token, outgoId);

        // 3. Then
        verify(outgoRepository, times(1)).delete(outgo1);
        verify(ledgerTagService, never()).deleteLedgerTag(anyString(), anyLong());
    }

    @Test
    @DisplayName("deleteOutgo2 : 삭제하려는 지출에만 연결된 태그인 경우")
    @Order(19)
    void deleteOutgoTest2 (){
        // 1. Given
        String token = "testToken";
        long outgoId = 1L; // 삭제 대상 지출 ID

        // 삭제 대상 지출 데이터 생성
        Outgo outgo = new Outgo();
        outgo.setOutgoId(outgoId);
        outgo.setMember(member);
        outgo.setLedgerTag(ledgerTag);

        ledgerTag.setLedgerTagId(1L); // 태그 데이터 생성
        ledgerTag.setOutgos(Collections.singletonList(outgo)); // 태그가 단일 지출에만 연결

        // Mock 설정
        when(outgoRepository.findById(outgoId)).thenReturn(Optional.of(outgo));
        doNothing().when(ledgerTagService).deleteLedgerTag(token, ledgerTag.getLedgerTagId());

        // 2. When
        outgoService.deleteOutgo(token, outgoId);

        // 3. Then
        verify(outgoRepository, times(1)).delete(outgo);
        verify(ledgerTagService, times(1)).deleteLedgerTag(token, ledgerTag.getLedgerTagId());
    }

    @Test
    @DisplayName("deleteOutgo3 : 회원 검증 실패 시 예외 발생")
    @Order(20)
    void deleteOutgoTest3 (){
        // 1. Given
        String token = "invalidToken";
        long outgoId = 1L;

        outgo.setOutgoId(outgoId);
        outgo.setMember(member);

        when(outgoRepository.findById(outgoId)).thenReturn(Optional.of(outgo));
        doThrow(new BusinessLogicException(ExceptionCode.MEMBER_MISMATCHED))
                .when(memberService).verifiedRequest(token, member.getMemberId());

        // 2. When/ Then
        assertThrows(BusinessLogicException.class, () -> outgoService.deleteOutgo(token, outgoId));
    }

    @Test
    @DisplayName("findVerifiedOutgo1 : 지출 데이터 존재 여부, 성공")
    @Order(21)
    void findVerifiedOutgoTest1_success (){
        // 1. Given
        long outgoId = 1L;
        outgo.setOutgoId(outgoId);

        when(outgoRepository.findById(outgo.getOutgoId())).thenReturn(Optional.of(outgo));

        // 2. When
        Outgo result = outgoService.findVerifiedOutgo(outgoId);

        // 3. Then
        assertEquals(outgo, result);
        verify(outgoRepository, times(1)).findById(outgoId);
    }

    @Test
    @DisplayName("findVerifiedOutgo2 : 지출 데이터 존재 여부, 실패")
    @Order(22)
    void findVerifiedOutgoTest2_fail (){
        // 1. Given
        long outgoId = 2L;
        when(outgoRepository.findById(outgoId)).thenReturn(Optional.empty());

        // 2. When/ Then
        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> outgoService.findVerifiedOutgo(outgoId));

        assertEquals(ExceptionCode.OUTGO_NOT_FOUND, exception.getExceptionCode());
        verify(outgoRepository, times(1)).findById(outgoId);
    }

    @Test
    @DisplayName("getDailyTotalOutgo1 : 일일 지출금액 총 합계")
    @Order(23)
    void getDailyTotalOutgoTest (){
        // 1. Given
        String token = "testToken";
        long memberId = 1L;
        LocalDate curDate = LocalDate.now();
        long expectedOutgoTotal = 5000L;

        when(jwtTokenizer.getMemberId(token)).thenReturn(memberId);
        when(outgoRepository.findTotalOutgoByDay(memberId, curDate)).thenReturn(expectedOutgoTotal);

        // 2. When
        long actualOutgoTotal = outgoService.getDailyTotalOutgo(token, curDate);

        // 3. Then
        assertEquals(expectedOutgoTotal, actualOutgoTotal);
        verify(jwtTokenizer, times(1)).getMemberId(token);
        verify(outgoRepository, times(1)).findTotalOutgoByDay(memberId, curDate);
    }


    @Test
    @DisplayName("getDailyTotalOutgo2 : 일일 지출금액 결과가 null인 경우")
    @Order(24)
    void getDailyTotalOutgoTest_nullResult (){
        // 1. Given
        String token = "testToken";
        long memberId = 1L;
        LocalDate curDate = LocalDate.now();
        long expectedOutgoTotal = 0L; // 쿼리 결과가 null인 경우 0 반환 예정

        when(jwtTokenizer.getMemberId(token)).thenReturn(memberId);
        when(outgoRepository.findTotalOutgoByDay(memberId, curDate)).thenReturn(null); // Mock  호출 결과를 null로 설정

        // 2. When
        long actualOutgoTotal = outgoService.getDailyTotalOutgo(token, curDate);

        // 3. Then
        assertEquals(expectedOutgoTotal, actualOutgoTotal);
        verify(jwtTokenizer, times(1)).getMemberId(token);
        verify(outgoRepository, times(1)).findTotalOutgoByDay(memberId, curDate);
    }

    @Test
    @DisplayName("ConvertImageToOutgo : 영수증 인식 후 자동작성")
    @Order(25)
    public void ConvertImageToOutgoTest () throws IOException {
        // Given
        String token = "testToken";
        String imageUrl = "mockImageUrl";
        OutgoDto.PostImage postImage = new OutgoDto.PostImage(imageUrl);
        ClovaOcrDto mockOcrDto = new ClovaOcrDto("2025-01-20", 10000, "Mock Store");

        when(clovaOcrApiService.convertImageToBase64(imageUrl)).thenReturn("mockBase64Image");
        when(clovaOcrApiService.callOcrApi("mockBase64Image")).thenReturn(mockOcrDto);

        // When
        OutgoDto.ImageOcrResponse response = outgoService.convertImageToOutgo(token, postImage);

        // Then
        assertEquals("2025-01-20", response.getDate());
        assertEquals(10000, response.getMoney());
        assertEquals("Mock Store", response.getOutgoName());
        assertEquals(imageUrl, response.getReceiptImg());
    }

    /* 참고 사항 - 2025-01-20 * 김석현 작성 *
    *  1. findOutgoPagesAsList 메서드는 private메서드 findOutgoPages를 오버로딩한 메서드로
    *     findOutgoPages는 findAllOutgos로 우회검증을 많이 하였음으로 제외.
    *
    *  2. 테스트를 진행하며 outgoService 메서드에 개선사항이 보임
    *  -> findOutgoPages 메서드는 페이지 생성 중복 코드를 통일시키려고 통합시켜놓은 메서드인데,
    *     하는 일이 방대하여 가독성을 해치고 단일 책임 원칙을 위반하고 있다고 판단함.
    *     일을 나눠서 메서드를 나눠줄 필요가 있어보임.
    *     그러나 재사용성을 높이기 위한 적절한 중복은 허용
    * */
}