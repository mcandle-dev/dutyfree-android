# DutyFree Android App (BLE Advertise)

DutyFree Android App은 사용자 멤버십 정보를 BLE(Bluetooth Low Energy)를 통해 전송하고, 여권 및 출국 정보를 관리하는 안드로이드 애플리케이션입니다. 오프라인 면세점 POS 기기와의 원활한 데이터 연동을 목적으로 개발되었습니다.

## 주요 기능 (Key Features)

### 1. 홈 화면 (Home Screen)
- **그리팅 메시지**: 사용자 이름과 함께 개인화된 환영 메시지 제공.
- **멤버십 카드**: 바코드 형식의 멤버십 번호 표시.
- **여권 정보 카드**: 여권 번호, 영문 이름, 만료일 등 핵심 여권 정보 표시.
- **출국 정보 카드**: 항공사, 편명, 출국 시간 등 출국 관련 실시간 정보 표시.

### 2. MY 설정 (My Tab / Settings)
- **멤버십 정보 설정**: 이름, 멤버십 번호, 등급, 전화번호 설정.
- **여권 정보 입력**: 여권 번호, 영문 성/이름, 만료일 입력.
- **출국 정보 입력**: 항공사 선택, 편명, 출국 시간 입력.
- **데이터 저장**: `SharedPreferences`를 이용한 신속하고 안정적인 데이터 저장 및 동기화.

### 3. BLE 서비스 (BLE Advertising)
- **멤버십 바코드 전송**: BLE Advertise 기술을 사용하여 멤버십 정보를 주변 POS 리더기로 전송.
- **통합 데이터 규격**: 멤버십 정보와 여권/출국 정보를 통합한 전용 패킷 규격 준수.
- **GATT 서버**: POS 기기와의 연결을 위한 전용 GATT 서버 및 특성(Characteristic) 구현.

## 기술 스택 (Tech Stack)
- **Language**: Kotlin
- **UI Architecture**: Fragment-based XML Layout
- **Storage**: SharedPreferences (Synchronous Data Persistence)
- **Connectivity**: Android BLE (Bluetooth Low Energy) API
- **UI Framework**: Google Material Components

## 프로젝트 구조 (Project Structure)
- `com.mcandle.dutyfree.ui.screens`: 각 탭 및 화면별 Fragment 소스 코드.
- `com.mcandle.dutyfree.advertise`: BLE Advertisement 관리 로직.
- `com.mcandle.dutyfree.gatt`: GATT Server 및 전송 데이터 규격 정의.
- `com.mcandle.dutyfree.data`: `PassportStore` (SharedPreferences 기반 데이터 모델).
- `app/src/main/res/layout`: XML 기반 레이아웃 정의 파일.

## 설치 및 실행 방법 (Getting Started)
1. Android Studio를 실행하고 프로젝트를 오픈합니다.
2. `gradle sync`를 완료합니다.
3. 프로젝트를 빌드(`assembleDebug`)하고 실 기기(안드로이드 버전 11 이상 권장)에 설치합니다.
4. **권한**: 앱 실행 시 Bluetooth 및 위치 권한 허용이 필요합니다.

## 개발 정보
- **Source Repository**: [DutyFree Android GitHub](https://github.com/mcandle-dev/dutyfree-android)
- **Design Reference**: Figma Design System
