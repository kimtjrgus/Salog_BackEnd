# 📒 가계부 및 일기 기록 웹 서비스 Salog
  
  ![Component 70](https://github.com/kimtjrgus/salog/assets/120611048/52114883-7a5f-43b0-8a3a-a26072be37a3)

  ### 프로젝트 기간 : 2023.11.17 ~ 2024.02.24

  ### 배포링크 : <a href="http://www.salog.kro.kr" target="_blank">Salog</a>

  <br />
  <br />

## 📠 목차
- [👥 팀원 구성](#-팀원-구성)
- [🔧 기술 스택](#-기술-스택)
- [📊 주요 구현 기능](#-주요-구현-기능)
- [💻 페이지 별 기능](#-페이지-별-기능)
- [📄 기획서](#-기획서)
- [📢 피드백](#-피드백)
- [🎉 프로젝트 후기](#-프로젝트-후기)

<br />
<br />
  
## 👥 팀원 구성
<div align="center">
  
  |<img src="https://github.com/codestates-seb/seb43_main_004/assets/120611048/fd4b071f-c773-4a17-b27f-ec9656290fa5" width="130px" />|<img src="https://github.com/codestates-seb/seb43_main_004/assets/120611048/1c7f47bc-6dba-4d67-b189-5ac3148256fd" width="130px" />|<img src="https://github.com/codestates-seb/seb43_main_004/assets/120611048/c194e140-fb6b-4bec-8b60-5b8398258e86" width="130px" />
|:---:|:---:|:---:|
|[이용석](https://github.com/021Skyfall)|[김석현](https://github.com/kimtjrgus)|[선유준](https://github.com/YujunSun0)
|BE (팀장)|BE|FE|

</div>

<br />
<br />

## 🔧 기술 스택

### Common
![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)
![Discord](https://img.shields.io/badge/Discord-%235865F2.svg?style=for-the-badge&logo=discord&logoColor=white)
![Figma](https://img.shields.io/badge/figma-%23F24E1E.svg?style=for-the-badge&logo=figma&logoColor=white)

### Front-end
 ![HTML5](https://img.shields.io/badge/html5-%23E34F26.svg?style=for-the-badge&logo=html5&logoColor=white)
  ![CSS3](https://img.shields.io/badge/css3-%231572B6.svg?style=for-the-badge&logo=css3&logoColor=white)
  ![TypeScript](https://img.shields.io/badge/typescript-%23007ACC.svg?style=for-the-badge&logo=typescript&logoColor=white)
  ![React](https://img.shields.io/badge/react-%2320232a.svg?style=for-the-badge&logo=react&logoColor=%2361DAFB)
  <img src="https://img.shields.io/badge/Redux Toolkit-8B56E6?style=for-the-badge&logo=redux&logoColor=white">
  ![Styled Components](https://img.shields.io/badge/styled--components-DB7093?style=for-the-badge&logo=styled-components&logoColor=white)
  <img src="https://img.shields.io/badge/AWS s3-569A31?style=for-the-badge&logo=amazon s3&logoColor=white">
  <img src="https://img.shields.io/badge/AWS cloudfront-8B56E6?style=for-the-badge&logo=amazoncloudwatch&logoColor=white">
  <img src="https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=white">


   
   ### Back-end
   <img src="https://img.shields.io/badge/java-1E8CBE?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/intellijidea-000000?style=for-the-badge&logo=intellijidea&logoColor=white">
    <img src="https://img.shields.io/badge/spring boot-6DB33F?style=for-the-badge&logo=spring boot&logoColor=white">
    <img src="https://img.shields.io/badge/spring security-6DB33F?style=for-the-badge&logo=spring security&logoColor=white">
    <img src="https://img.shields.io/badge/mySQL-4479A1?style=for-the-badge&logo=mySQL&logoColor=white">
    <img src="https://img.shields.io/badge/JWT-d63aff?style=for-the-badge&logo=JWT&logoColor=white">
    ![GitHub Actions](https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white)
    ![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
    ![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)

<br />
<br />

## 📊 주요 구현 기능


<br />
<br />

## 💻 페이지 별 기능
### [초기화면]
- 웹서비스 접속 초기화면으로 AOS(Animate on scroll)가 적용 되어있습니다.
    - 로그인이 되어 있지 않은 경우에만 접근 가능합니다.

| 초기화면 |
|----------|
|![2024-03-083 49 55-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/Salog/assets/120611048/9845e1ed-a2ae-4e90-bec9-42eab7c0e0fd)|

<br />
<br />

### [회원가입]
- 이메일 주소와 비밀번호를 입력할 때, 입력창에서 벗어나면 유효성 검사가 진행되고 통과하지 못한 경우 각 경고 문구가 입력창 하단에 표시됩니다.
- 이메일 주소의 형식이 유효하지 않으면 인증 버튼이 활성화되지 않으며, 이미 가입된 이메일일 경우 인증 버튼 클릭 시 입력창 하단에 경구 문구가 나타납니다.
- 작성한 이메일을 통하여 인증번호를 받아 입력하는 검증 과정을 거쳐야합니다.
- 작성이 완료된 후, 유효성 검사가 모두 통과되면 회원가입 버튼이 활성화되며, 버튼을 클릭하면 회원가입이 완료되어 로그인 페이지로 이동됩니다.

| 유효성 검사 | 이메일 인증 |
|:---:|:---:|
|![2024-03-024 04 19-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/salog/assets/120611048/69b4fd70-7ff5-4c92-b7e1-873d57e36f10)|![2024-03-024 12 35-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/salog/assets/120611048/bcca3976-0b0e-4019-9ea7-5ccef39445f9)|

| 회원가입 |
|----------|
|![2024-03-024 17 18-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/salog/assets/120611048/8c23808e-1636-435c-a717-4c2aeab5eb9b)|

<br />
<br />

### [로그인]
- 이메일과 비밀번호를 입력하여 로그인을 진행합니다.
   - 로그인 버튼 클릭 시 입력값이 일치하지 않을 경우에는 경구 문구가 나타나며,
로그인에 성공하면 홈 대시보드 화면으로 이동합니다.
- 소셜 로그인(카카오, 구글, 네이버)을 통한 서비스 이용이 가능합니다. (현재는 구글만 가능)

| 일반 로그인 | 소셜 로그인(구글) |
|:---:|:---:|
|![2024-03-024 22 24-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/salog/assets/120611048/b735f71a-3ed7-41b6-85e6-e500254a6b0a)|![2024-03-024 41 20-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/salog/assets/120611048/a9751450-1431-4c6a-9c2a-42a1188bb80e)|

<br>

### [비밀번호 찾기]
- 비밀번호를 잊어버렸을 때, 로그인 페이지에서 하단에 있는 비밀번호를 찾기를 진행합니다.
- 이메일을 입력하여 인증번호를 받고, 인증이 완료되면 비밀번호를 재설정할 수 있습니다.

| 비밀번호 찾기 |
|----------|
|![2024-03-024 56 25-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/salog/assets/120611048/9e4d40b6-c4cd-4058-a8f4-46b9050bcfbb)|

<br>

### [비밀번호 변경 & 회원 탈퇴]
- 기존 비밀번호, 새로운 비밀번호를 입력하여 비밀번호를 변경할 수 있습니다.
- 회원 탈퇴를 한 뒤, 해당 계정으로 재가입이 가능합니다.
- 설정 페이지에서 해당 기능들을 사용할 수 있습니다.

| 비밀번호 변경 | 회원 탈퇴 |
|:---:|:---:|
|![2024-03-158 59 10-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/Salog/assets/120611048/7ded3643-a0ae-41ad-9e94-075dae104f4b)|![2024-03-159 02 38-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/Salog/assets/120611048/e02657b5-9acc-46ec-9559-cc5c34978ff0)|

<br>

### [로그아웃]
- 좌측의 sidebar에 있는 로그아웃을 클릭 후 나타나는 모달창의 확인 버튼을 클릭하면 로그아웃이 됩니다.
- 로그아웃시 로컬 저장소 및 쿠키의 토큰 값을 삭제하고 로그인 화면으로 이동합니다.

| 로그아웃 |
|----------|
|![2024-03-025 06 34-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/salog/assets/120611048/8942f28e-8bb8-4594-9953-13d88d10a77c)|

<br>

### [대시보드]
- 이번 달 지출 · 수입 · 낭비리스트 · 예산을 그래프와 함께 확인할 수 있습니다.
- 하단의 캘린더는 타일마다 해당 일의 지출과 수입의 합계를 보여주며, 타일을 클릭하면 작성 된 가계부를 조회할 수 있습니다.
- 타일에 마우스를 올리면 작성 아이콘이 보여지며, 클릭 시 가계부를 작성할 수 있습니다.

| 대시보드 |
|:---:|
|![2024-03-026 01 12-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/salog/assets/120611048/38793435-ea5c-4a8c-b66d-3ef08c33d218)|

<br>

### [지출 · 수입]
#### 1. 지출 · 수입 조회
- 상단의 탭을 통하여 지출과 수입을 따로 볼수 있으며, 낭비리스트를 확인할 수 있습니다.
- 낭비리스트는 지출내역 중 추가할 수 있으며, 항목을 체크하면 나오는 탭에서 낭비리스트 버튼으로 추가 가능합니다.
- 날짜 오름차순 / 내림차순 정렬이 가능하고 카테고리 필터링 및 특정 날짜 필터링 조회 기능이 추가 될 예정입니다.

| 지출 · 수입 조회 |
|:---:|
|![2024-03-026 24 43-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/salog/assets/120611048/dcadff14-9e9f-40d4-a4da-b531949eaea2)|

<br>

#### 2. 지출 · 수입 작성
- 가계부 작성하기 버튼을 클릭하면 작성 모달이 나옵니다.
- 주어진 항목들을 입력하면 작성 버튼이 활성화되어 가계부 작성이 가능합니다. (항목 중 메모는 작성하지 않아도 됩니다)
- 하단의 행 추가를 눌러 여러 개의 내역을 작성할 수 있습니다.

| 지출 · 수입 작성 |
|:---:|
|![2024-03-026 28 34-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/salog/assets/120611048/7ff77e6c-a7b5-4f28-8b50-2202c0bff827)|

<br>

#### 3. 지출 · 수입 수정 & 삭제
- 항목들을 체크하여 나오는 탭에서 수정 또는 삭제를 클릭합니다.
- 수정 및 삭제로 인해 내용이 변경되면 페이지에 바로 반영됩니다.

| 지출 · 수입 수정 & 삭제 |
|:---:|
|![2024-03-026 49 33-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/salog/assets/120611048/1248e98c-d420-4aed-84ec-c3771c413197)|

<br>

### [분석]
- 이번 달 지출을 차트 및 그래프를 통해 분석을 보여주는 페이지입니다.
- 최근 3개월 지출 합계, 주간 별 분석, 분류 별 지출, 예산 소진율, 낭비 리스트를 확인할 수 있습니다.

| 분석 |
|:---:|
|<img width="610" height="310" alt="스크린샷 2024-03-02 오후 8 28 25" src="https://github.com/kimtjrgus/salog/assets/120611048/e14c09c8-120f-435c-bf03-3bd931445433">|

<br>

### [예산]
- 이번 달 예산을 설정하고 남은 예산 및 하루 예산을 볼 수 있습니다.

| 예산 |
|:---:|
|![2024-03-027 08 26-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/salog/assets/120611048/fbfce4be-6114-4fdb-974a-129a9ea04b18)|

<br>

### [고정 지출 · 수입]

#### 1. 고정 지출 · 수입 조회
- 달마다 고정으로 발생하는 지출 및 수입을 관리하는 페이지입니다.
- 고정 지출은 지출 일을 기준으로 3일 미만 남았다면 빨간색, 일주일 미만이면 노란색, 일주일 이상은 초록색으로 표시됩니다.

| 고정 지출 · 수입 조회 |
|:---:|
|<img width="610" height="312" alt="스크린샷 2024-03-02 오후 8 36 56" src="https://github.com/kimtjrgus/salog/assets/120611048/7c6fd232-8df7-4d39-ad63-8ccc45d9fc8b">|

<br>

#### 2. 고정 지출 · 수입 작성
- 항목들을 작성하면 일정 추가하기 버튼이 활성화됩니다.

| 고정 지출 · 수입 작성 |
|:---:|
|![2024-03-027 21 07-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/salog/assets/120611048/e77aff03-a61f-4569-acef-a557f7fbdaf0)|

<br>

#### 3. 고정 지출 · 수입 수정 & 삭제
- 상단의 탭을 통하여 일정 수정 탭으로 이동하면 수정 및 삭제가 가능합니다.
- 금융 일정 목록에서 수정하고 싶은 항목을 선택하여 내용 수정을 할 수 있습니다.
- 목록 하단의 삭제하기를 클릭하여 내용 삭제를 할 수 있습니다.

| 고정 지출 · 수입 수정 & 삭제 |
|:---:|
|![2024-03-027 26 13-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/salog/assets/120611048/0b2fb1ed-64b8-4148-95ca-52449ff7b031)|

<br>

### [영수증 인식]
- 영수증의 가계 이름, 지출 일, 총 합계의 항목을 인식하여 가계부를 작성할 수 있습니다.
- 영수증 인식을 통하여 가계부를 작성하면 가계부 조회 시 영수증 사진을 볼 수 있습니다.
- 대시보드 페이지, 지출&수입 페이지에서 가계부를 작성할 때 사용 가능합니다.
- 이미지 업로드 -> 영수증 인식 -> 내용 확인 -> 자동작성의 플로우를 가집니다.

| 영수증 인식(대시보드 페이지) | 영수증 인식(지출&수입 페이지) |
|:---:|:---:|
|![2024-03-159 13 28-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/Salog/assets/120611048/4399bb91-b6a4-47d9-ac69-49ff0df71766)|![2024-03-159 16 49-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/Salog/assets/120611048/813f0f05-cb88-4bc2-9db8-7565155d8b8a)|

<br>

### [알림]
- 웹 알림, 이메일 알림이 있습니다.
  - 웹 알림은 헤더의 알림 아이콘을 클릭하여 확인 가능하며, 알람을 확인 후 삭제하면 로컬 저장소에 저장되어 알림이 다시 발송되지 않습니다. 
- 고정 지출 및 수입에서 일정이 3일 미만인 항목들만 알림을 발송하며, 일정 당일날에는 해당 목록들을 가계부에 추가하는 것을 묻는 모달을 보여줍니다.
- 알림 설정은 회원 가입 시 가능하며, 헤더의 알림 아이콘을 클릭하면 하단의 스위치를 통하여 on/off 가능합니다.

| 알림 |
|:---:|
|![2024-03-027 54 16-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/salog/assets/120611048/70567a95-d9fe-403d-8795-58aa2adbe727)|

<br>

### [일기]
#### 1. 일기 전체 조회
- 작성한 모든 일기를 조회하는 페이지입니다.
- 제목으로 검색, 태그 필더링 및 날짜 필터링 조회가 가능합니다.

| 일기 전체 조회 |
|:---:|
|![2024-03-028 53 52-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/salog/assets/120611048/344b9303-2ab5-44ad-bc57-aa28d9f142bd)|

<br>

#### 2. 일기 상세 조회
- 전체 조회 페이지에서 일기를 선택하여 조회합니다.
- 텍스트 에디터로 작성 된 일기가 그대로 보여지며, 우측에는 일기를 작성한 당일의 가계부를 확인할 수 있습니다.

| 일기 상세 조회 |
|:---:|
|<img width="610" height="312" alt="스크린샷 2024-03-02 오후 9 25 27" src="https://github.com/kimtjrgus/salog/assets/120611048/d0a29a8e-b285-4e7c-98b5-26f0d9ae7b97">|

<br>

#### 3. 일기 작성
- 텍스트 에디터를 통하여 본문에 이미지를 첨부할 수 있습니다.
- 우측에는 작성 당일의 가계부가 표시되어 내역을 볼 수 있습니다.
- 작성 버튼을 누르면 유효성 검사가 진행되고 통과하지 못하면 토스트 창으로 경고 문구가 나타납니다.
- 작성이 완료되면 전체 조회 페이지로 이동합니다. 또한 본문의 이미지를 올렸다면 썸네일로 지정하며,
  이미지가 여러개라면 첫 번째를 썸네일로 지정하여 전체 조회에 보여집니다.

| 일기 작성 |
|:---:|
|![2024-03-029 12 21-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/salog/assets/120611048/6c72940c-248a-4384-b209-cc17fd4b9fd4)|

<br>

#### 4. 일기 수정 & 삭제
- 일기 상세 조회시 상단의 수정 및 삭제 버튼을 클릭합니다.
- 수정 또는 삭제가 완료되면 전체 조회 페이지로 이동합니다.

| 일기 수정 & 삭제 |
|:---:|
|![2024-03-029 21 13-ezgif com-video-to-gif-converter](https://github.com/kimtjrgus/salog/assets/120611048/c3cb819c-f062-4670-9b25-f5c555604c06)|

<br />
<br />

## 📄 기획서

<a href="https://docs.google.com/spreadsheets/d/1_bI9UAymfg1Typ5DVZzbXZe_OQ08q-AZO9Ve1Ph1SL8/edit#gid=0" target="_blank">사용자 요구사항 정의서</a>
<br />
<br />
<a href="https://docs.google.com/spreadsheets/d/1_bI9UAymfg1Typ5DVZzbXZe_OQ08q-AZO9Ve1Ph1SL8/edit#gid=256906179" target="_blank">기능 명세서</a>
<br />
<br />
<a href="https://www.figma.com/file/aKvGgfBlFxHudca45yo8Oe/Fontpair-(Community)?type=design&node-id=298%3A7004&mode=design&t=YwHkQlgXU63AX6HR-1" target="_blank">화면 정의서</a>
<br />
<br />
<a href="https://docs.google.com/spreadsheets/d/1_bI9UAymfg1Typ5DVZzbXZe_OQ08q-AZO9Ve1Ph1SL8/edit#gid=89922257" target="_blank">API 명세서</a>
<br />
<br />
<a href="https://docs.google.com/spreadsheets/d/1_bI9UAymfg1Typ5DVZzbXZe_OQ08q-AZO9Ve1Ph1SL8/edit#gid=1572238774" target="_blank">테이블 명세서</a>
<br />
<br />
<a href="https://docs.google.com/spreadsheets/d/1_bI9UAymfg1Typ5DVZzbXZe_OQ08q-AZO9Ve1Ph1SL8/edit#gid=1829469387" target="_blank">코드 컨벤션</a>

<br>
<br>

## 📢 피드백

<br>
<br>
<br>
 
## 🎉 프로젝트 후기

### 이용석
이번 프로젝트는 이전에 진행했던 경험을 바탕으로 팀을 구성하여 더욱 발전된 결과물을 만들어낼 수 있었습니다. 
개개인의 시간을 활용하여 진행된 프로젝트였기에 일정은 예상보다 조금 늘어났지만, 그만큼 더 깊고 세밀한 부분까지 고려할 수 있었습니다.

특히, 보안과 배포 환경에 대한 깊은 고찰을 할 수 있던 시간은 매우 값진 경험이었습니다. 
이런 과정에서 Oauth와 Docker 같은 새로운 기술을 접하면서 통신 프로토콜과 배포 방법에 대한 이해도 깊어졌습니다. 
이 과정에서는 어려움도 많았지만, 그 어려움을 극복하며 새로운 기술을 습득한 것은 큰 성취감을 주었습니다.

에러 로그 분석 방법 등, 제 자신의 성장을 체감할 수 있는 순간도 있었습니다. 
프로젝트에 참여한 팀원들에게 감사의 마음을 전하며, 앞으로 리펙토링을 통해 이 프로젝트를 완벽에 더 가까이 가져가고자 합니다.

### 김석현
부트캠프 때 배웠던 것을 복습하는 느낌으로 팀원과 다 같이 설계부터 
프로젝트 끝까지 다시 구현해본 뜻 깊은 시간이었습니다.
지난 프로젝트때를 기억하며, 이번 프로젝트를 시작했었고 어떤 부분이 부족했는지,
더 채워나갈 부분은 무엇인지 알고 협업하며 공부하는 것이 재밌었습니다.

저는 백엔드로써 AWS를 다루고 배포하는 것에 대해 부족하다고 느꼈는데,
좀더 익숙해지고 Docker, Clova Api 등을 다루며, 이전에는 못 해보았던 것들을 경험하고 한 층 더 성장한 것 같습니다. 
트러블슈팅의 순간순간을 기록하지 못해 조금 아쉬운 점이 있으나, 일부 기록하고 차후에 블로깅을 하며 되새기는 것도 
하나의 재미일 것 같아 두근거립니다.
그리고 앞으로도 해당 프로젝트에 새로 배운 기술들의 추가나 좀 더 클린한 코드, 클린한 아키텍처로 
리팩토링하며 더 좋은 프로젝트가 되도록 가다듬어 보려합니다.

함께 협업한 코드마우스팀, 용석님 유준님 감사합니다.

### 선유준
많은 것을 배울 수 있었던 프로젝트였습니다. 
프론트엔드를 혼자 담당하게 되어 기획한 모든 기능들을 구현 해내기 위해 노력하였으며, 점차 완성되어 가는 웹을 보며 뿌듯함을 느꼈습니다.

기능적 부분으로는 Redux-toolkit을 사용하면서 리덕스 상태를 지속적으로 저장 및 복원하기 위한 Redux-persist를 사용하면서 전역상태를 사용해보는 경험을 해본 점이 좋았습니다.
추가로, 텍스트 에디터를 사용하여 이미지를 올릴 때 base64 형식으로 인코딩되어 저장되는 것을 firebase storage를 사용하여 url로 변환하여 사용하는 과정으로 최적화도 이뤄냈고, 가독성 문제도
해결한 뿌듯한 성과를 이뤄내며 성장할 수 있었습니다.

아쉬운 점으로는 React의 상태를 관리하면서 서버와 통신 할 때 최신화하는 것을 위해 React-Query를 사용해보고 싶었지만, 기능을 구현하는 것에 초점을 두어 학습하지 못한 점이 아쉬웠습니다.

모두 고생하셨고 리팩토링을 통하여 더 나은 프로젝트 완성까지 화이팅해봅시다!
