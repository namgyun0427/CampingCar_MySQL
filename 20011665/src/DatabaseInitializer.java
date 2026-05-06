import java.sql.*;
import java.math.BigDecimal;

public class DatabaseInitializer {
    
    public static void insertAllInitialData(Connection conn) throws SQLException {
        insertCompanyData(conn);
        insertCustomerData(conn);
        insertEmployeeData(conn);
        insertCamperData(conn);
        insertPartData(conn);
        insertRepairShopData(conn);
        insertRentalData(conn);
        insertInternalRepairData(conn);
        insertExternalRepairData(conn);
    }
    
    private static void insertCompanyData(Connection conn) throws SQLException {
        String sql = "INSERT INTO Company VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String[][] data = {
                {"1", "캠핑카월드", "서울 강남구", "02-1234-5678", "김관리", "campworld@naver.com"},
                {"2", "자연과함께", "부산 해운대구", "051-987-6543", "이자연", "nature@daum.net"},
                {"3", "로드트립캠핑", "제주 노형동", "064-332-1100", "박로드", "road@google.com"},
                {"4", "어드벤처캠핑", "강원 춘천시", "033-456-7890", "최어드", "adventure@naver.com"},
                {"5", "가족캠핑카", "경기 가평군", "031-123-4567", "정가족", "family@daum.net"},
                {"6", "산악캠핑", "강원 평창군", "033-789-1234", "임산악", "mountain@google.com"},
                {"7", "바다로캠핑", "경남 통영시", "055-456-7890", "김바다", "sea@naver.com"},
                {"8", "시티캠핑카", "대전 유성구", "042-123-4567", "한시티", "city@daum.net"},
                {"9", "럭셔리캠핑", "서울 송파구", "02-987-6543", "오럭셔리", "luxury@google.com"},
                {"10", "에코캠핑카", "전북 전주시", "063-332-1100", "조에코", "eco@naver.com"},
                {"11", "캠핑카렌탈", "인천 연수구", "032-456-7890", "나렌탈", "rental@daum.net"},
                {"12", "휴가캠핑카", "경북 경주시", "054-123-4567", "문휴가", "holiday@google.com"}
            };
            
            for (String[] row : data) {
                pstmt.setInt(1, Integer.parseInt(row[0]));
                pstmt.setString(2, row[1]);
                pstmt.setString(3, row[2]);
                pstmt.setString(4, row[3]);
                pstmt.setString(5, row[4]);
                pstmt.setString(6, row[5]);
                pstmt.executeUpdate();
            }
        }
    }
    
    private static void insertCustomerData(Connection conn) throws SQLException {
        String sql = "INSERT INTO Customer VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String[][] data = {
                {"CUST0001", "user1", "pass1", "12-34-567890-01", "홍길동", "강남구", "010-1234-5678", "hong@naver.com", "2023-01-15", "럭셔리"},
                {"CUST0002", "user2", "pass2", "12-45-678901-12", "김철수", "해운대구", "010-2345-6789", "kim@daum.net", "2023-02-20", "가족형"},
                {"CUST0003", "user3", "pass3", "12-56-789012-23", "박영희", "수성구", "010-3456-7890", "park@google.com", "2023-03-10", "커플"},
                {"CUST0004", "user4", "pass4", "12-67-890123-34", "이영수", "남동구", "010-4567-8901", "lee@naver.com", "2023-04-05", "소형"},
                {"CUST0005", "user5", "pass5", "12-78-901234-45", "최민지", "서구", "010-5678-9012", "choi@daum.net", "2023-05-12", "대형"},
                {"CUST0006", "user6", "pass6", "12-89-012345-56", "정수민", "유성구", "010-6789-0123", "jung@google.com", "2023-06-25", "럭셔리"},
                {"CUST0007", "user7", "pass7", "12-90-123456-67", "강동원", "남구", "010-7890-1234", "kang@naver.com", "2023-07-08", "가족형"},
                {"CUST0008", "user8", "pass8", "13-01-234567-78", "윤서연", "가람동", "010-8901-2345", "yoon@daum.net", "2023-08-17", "커플"},
                {"CUST0009", "user9", "pass9", "13-12-345678-89", "신지민", "팔달구", "010-9012-3456", "shin@google.com", "2023-09-22", "소형"},
                {"CUST0010", "user10", "pass10", "13-23-456789-90", "장현우", "효자동", "010-0123-4567", "jang@naver.com", "2023-10-30", "대형"},
                {"CUST0011", "user11", "pass11", "13-34-567890-01", "한소희", "흥덕구", "010-1234-5678", "han@daum.net", "2023-11-15", "럭셔리"},
                {"CUST0012", "user12", "pass12", "13-45-678901-12", "송태민", "서북구", "010-2345-6789", "song@google.com", "2023-12-05", "가족형"}
            };
            
            for (String[] row : data) {
                pstmt.setString(1, row[0]);
                pstmt.setString(2, row[1]);
                pstmt.setString(3, row[2]);
                pstmt.setString(4, row[3]);
                pstmt.setString(5, row[4]);
                pstmt.setString(6, row[5]);
                pstmt.setString(7, row[6]);
                pstmt.setString(8, row[7]);
                pstmt.setDate(9, Date.valueOf(row[8]));
                pstmt.setString(10, row[9]);
                pstmt.executeUpdate();
            }
        }
    }
    
    private static void insertEmployeeData(Connection conn) throws SQLException {
        String sql = "INSERT INTO Employee VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Object[][] data = {
                {1, "김정비", "010-1111-2222", "강남구", 3500000, 2, "정비부", "정비", 1},
                {2, "이관리", "010-2222-3333", "서초구", 4500000, 1, "관리부", "관리", 1},
                {3, "박사무", "010-3333-4444", "삼성동", 3000000, 0, "사무부", "사무", 1},
                {4, "최정비", "010-4444-5555", "우동", 3300000, 1, "정비부", "정비", 2},
                {5, "정관리", "010-5555-6666", "온천동", 4200000, 3, "관리부", "관리", 2},
                {6, "강사무", "010-6666-7777", "중동", 2900000, 0, "사무부", "사무", 2},
                {7, "윤정비", "010-7777-8888", "노형동", 3400000, 1, "정비부", "정비", 3},
                {8, "임관리", "010-8888-9999", "이도동", 4300000, 2, "관리부", "관리", 3},
                {9, "한사무", "010-9999-0000", "연동", 3100000, 0, "사무부", "사무", 3},
                {10, "신정비", "010-0000-1111", "효자동", 3200000, 1, "정비부", "정비", 4},
                {11, "조관리", "010-1234-5678", "단구동", 4000000, 2, "관리부", "관리", 4},
                {12, "황사무", "010-2345-6789", "포남동", 2800000, 0, "사무부", "사무", 4}
            };
            
            for (Object[] row : data) {
                pstmt.setInt(1, (Integer) row[0]);
                pstmt.setString(2, (String) row[1]);
                pstmt.setString(3, (String) row[2]);
                pstmt.setString(4, (String) row[3]);
                pstmt.setInt(5, (Integer) row[4]);
                pstmt.setInt(6, (Integer) row[5]);
                pstmt.setString(7, (String) row[6]);
                pstmt.setString(8, (String) row[7]);
                pstmt.setInt(9, (Integer) row[8]);
                pstmt.executeUpdate();
            }
        }
    }
    
    private static void insertCamperData(Connection conn) throws SQLException {
        String sql = "INSERT INTO Camper VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Object[][] data = {
                {1, "럭셔리 캠핑카 A", "서울123가4567", 4, "luxury_a.jpg", "고급 인테리어", 150000, 1, "2023-01-15", "Available"},
                {2, "가족형 캠핑카", "경기456나7890", 6, "family.jpg", "6인 가족용", 180000, 2, "2023-02-20", "Available"},
                {3, "커플 캠핑카", "인천789다1234", 2, "couple.jpg", "아늑한 공간", 100000, 3, "2023-03-10", "Available"},
                {4, "소형 캠핑카", "부산321라9876", 2, "small.jpg", "주차 편리", 90000, 4, "2023-04-05", "Available"},
                {5, "대형 캠핑카", "대전654마3210", 8, "large.jpg", "대가족용", 220000, 5, "2023-05-12", "Available"},
                {6, "오프로드 캠핑카", "강원987바6543", 4, "offroad.jpg", "산악용", 170000, 6, "2023-06-25", "Available"},
                {7, "프리미엄 캠핑카", "제주210사8765", 4, "premium.jpg", "최고급 사양", 200000, 7, "2023-07-08", "Available"},
                {8, "도심형 캠핑카", "경남543아2109", 3, "urban.jpg", "도심 주행", 120000, 8, "2023-08-17", "Available"},
                {9, "럭셔리 캠핑카 B", "서울876자5432", 4, "luxury_b.jpg", "신형 럭셔리", 160000, 9, "2023-09-22", "Available"},
                {10, "친환경 캠핑카", "전북109차8765", 4, "eco.jpg", "친환경 재료", 140000, 10, "2023-10-30", "Available"},
                {11, "경제적 캠핑카", "충남432카1098", 4, "economy.jpg", "연비 효율", 110000, 11, "2023-11-15", "Available"},
                {12, "올인원 캠핑카", "경북765타4321", 5, "allinone.jpg", "모든 편의시설", 190000, 12, "2023-12-05", "Available"}
            };
            
            for (Object[] row : data) {
                pstmt.setInt(1, (Integer) row[0]);
                pstmt.setString(2, (String) row[1]);
                pstmt.setString(3, (String) row[2]);
                pstmt.setInt(4, (Integer) row[3]);
                pstmt.setString(5, (String) row[4]);
                pstmt.setString(6, (String) row[5]);
                pstmt.setInt(7, (Integer) row[6]);
                pstmt.setInt(8, (Integer) row[7]);
                pstmt.setDate(9, Date.valueOf((String) row[8]));
                pstmt.setString(10, (String) row[9]);
                pstmt.executeUpdate();
            }
        }
    }
    
    private static void insertPartData(Connection conn) throws SQLException {
        String sql = "INSERT INTO Part VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Object[][] data = {
                {1, "엔진 오일", 50000, 100, "2023-01-10", "자동차부품상사"},
                {2, "오일 필터", 15000, 80, "2023-01-10", "자동차부품상사"},
                {3, "에어 필터", 20000, 70, "2023-01-15", "자동차부품상사"},
                {4, "브레이크 패드", 80000, 40, "2023-02-05", "브레이크상사"},
                {5, "타이어", 150000, 30, "2023-02-20", "타이어유통"},
                {6, "배터리", 120000, 25, "2023-03-10", "전기부품"},
                {7, "와이퍼", 30000, 60, "2023-03-25", "자동차부품상사"},
                {8, "전조등", 25000, 45, "2023-04-05", "전기부품"},
                {9, "냉각수", 35000, 55, "2023-04-20", "자동차부품상사"},
                {10, "변속기 오일", 60000, 35, "2023-05-10", "자동차부품상사"},
                {11, "스파크 플러그", 40000, 50, "2023-05-25", "전기부품"},
                {12, "타이밍 벨트", 70000, 20, "2023-06-10", "자동차부품상사"}
            };
            
            for (Object[] row : data) {
                pstmt.setInt(1, (Integer) row[0]);
                pstmt.setString(2, (String) row[1]);
                pstmt.setInt(3, (Integer) row[2]);
                pstmt.setInt(4, (Integer) row[3]);
                pstmt.setDate(5, Date.valueOf((String) row[4]));
                pstmt.setString(6, (String) row[5]);
                pstmt.executeUpdate();
            }
        }
    }
    
    private static void insertRepairShopData(Connection conn) throws SQLException {
        String sql = "INSERT INTO ExternalRepairShop VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Object[][] data = {
                {1, "전문캠핑카정비소", "강남구", "02-123-4567", "박정비", "repair1@naver.com"},
                {2, "빠른정비센터", "해운대구", "051-234-5678", "김빠른", "quick2@daum.net"},
                {3, "종합자동차정비", "남동구", "032-345-6789", "이종합", "general3@google.com"},
                {4, "캠핑카케어", "둔산동", "042-456-7890", "최케어", "care4@naver.com"},
                {5, "모바일정비", "용봉동", "062-567-8901", "정모바일", "mobile5@daum.net"},
                {6, "프리미엄정비샵", "범어동", "053-678-9012", "강프리미엄", "premium6@google.com"},
                {7, "긴급정비", "삼산동", "052-789-0123", "윤긴급", "emergency7@naver.com"},
                {8, "전문수리점", "팔달구", "031-890-1234", "장전문", "specialist8@daum.net"},
                {9, "친환경정비", "효자동", "033-901-2345", "한친환경", "eco9@google.com"},
                {10, "첨단정비소", "노형동", "064-012-3456", "신첨단", "hightech10@naver.com"},
                {11, "원스톱정비", "남구", "054-123-4567", "조원스톱", "onestop11@daum.net"},
                {12, "신뢰정비", "산정동", "061-234-5678", "황신뢰", "trust12@google.com"}
            };
            
            for (Object[] row : data) {
                pstmt.setInt(1, (Integer) row[0]);
                pstmt.setString(2, (String) row[1]);
                pstmt.setString(3, (String) row[2]);
                pstmt.setString(4, (String) row[3]);
                pstmt.setString(5, (String) row[4]);
                pstmt.setString(6, (String) row[5]);
                pstmt.executeUpdate();
            }
        }
    }
    
    private static void insertRentalData(Connection conn) throws SQLException {
        String sql = "INSERT INTO Rental VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Object[][] data = {
                {1, 1, "12-34-567890-01", 1, "2023-07-01", 3, 450000, "2023-06-25", "추가 운전자 1명", 30000, "Completed"},
                {2, 2, "12-45-678901-12", 2, "2023-07-05", 5, 900000, "2023-06-30", null, 0, "Completed"},
                {3, 3, "12-56-789012-23", 3, "2023-07-10", 2, 200000, "2023-07-05", "아이스박스 대여", 10000, "Completed"},
                {4, 4, "12-67-890123-34", 4, "2023-07-15", 4, 360000, "2023-07-10", null, 0, "Completed"},
                {5, 5, "12-78-901234-45", 5, "2023-08-01", 7, 1540000, "2023-07-25", "캠핑 장비 세트", 50000, "Completed"},
                {6, 6, "12-89-012345-56", 6, "2023-08-10", 3, 510000, "2023-08-05", null, 0, "Completed"},
                {7, 7, "12-90-123456-67", 7, "2023-08-15", 5, 1000000, "2023-08-10", "전용 침구 세트", 20000, "Completed"},
                {8, 8, "13-01-234567-78", 8, "2023-08-20", 2, 240000, "2023-08-15", null, 0, "Completed"},
                {9, 9, "13-12-345678-89", 9, "2023-09-01", 4, 640000, "2023-08-25", "추가 주행거리", 40000, "Completed"},
                {10, 10, "13-23-456789-90", 10, "2023-09-05", 6, 840000, "2023-08-30", null, 0, "Completed"},
                {11, 11, "13-34-567890-01", 11, "2023-09-10", 3, 330000, "2023-09-05", "세차 서비스", 15000, "Completed"},
                {12, 12, "13-45-678901-12", 12, "2023-09-15", 5, 950000, "2023-09-10", null, 0, "Completed"}
            };
            
            for (Object[] row : data) {
                pstmt.setInt(1, (Integer) row[0]);
                pstmt.setInt(2, (Integer) row[1]);
                pstmt.setString(3, (String) row[2]);
                pstmt.setInt(4, (Integer) row[3]);
                pstmt.setDate(5, Date.valueOf((String) row[4]));
                pstmt.setInt(6, (Integer) row[5]);
                pstmt.setInt(7, (Integer) row[6]);
                pstmt.setDate(8, Date.valueOf((String) row[7]));
                pstmt.setString(9, (String) row[8]);
                pstmt.setInt(10, (Integer) row[9]);
                pstmt.setString(11, (String) row[10]);
                pstmt.executeUpdate();
            }
        }
    }
    
    private static void insertInternalRepairData(Connection conn) throws SQLException {
        String sql = "INSERT INTO InternalRepair VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Object[][] data = {
                {1, 1, 1, "2023-07-15", 60, 1},
                {2, 2, 2, "2023-07-20", 45, 1},
                {3, 3, 3, "2023-07-25", 30, 1},
                {4, 4, 4, "2023-08-05", 120, 4},
                {5, 5, 5, "2023-08-10", 90, 4},
                {6, 6, 6, "2023-08-15", 60, 4},
                {7, 7, 7, "2023-09-01", 30, 7},
                {8, 8, 8, "2023-09-05", 45, 7},
                {9, 9, 9, "2023-09-10", 60, 7},
                {10, 10, 10, "2023-10-01", 90, 10},
                {11, 11, 11, "2023-10-05", 60, 10},
                {12, 12, 12, "2023-10-10", 45, 10}
            };
            
            for (Object[] row : data) {
                pstmt.setInt(1, (Integer) row[0]);
                pstmt.setInt(2, (Integer) row[1]);
                pstmt.setInt(3, (Integer) row[2]);
                pstmt.setDate(4, Date.valueOf((String) row[3]));
                pstmt.setInt(5, (Integer) row[4]);
                pstmt.setInt(6, (Integer) row[5]);
                pstmt.executeUpdate();
            }
        }
    }
    
    private static void insertExternalRepairData(Connection conn) throws SQLException {
        String sql = "INSERT INTO ExternalRepair VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Object[][] data = {
                {1, 1, 1, 1, "12-34-567890-01", "엔진 오일 교체", "2023-07-20", 120000, "2023-08-20", null, "Completed"},
                {2, 2, 2, 2, "12-45-678901-12", "브레이크 패드 교체", "2023-07-25", 150000, "2023-08-25", "디스크 점검", "Completed"},
                {3, 3, 3, 3, "12-56-789012-23", "배터리 교체", "2023-08-01", 180000, "2023-09-01", null, "Completed"},
                {4, 4, 4, 4, "12-67-890123-34", "타이어 교체", "2023-08-05", 600000, "2023-09-05", "휠 조정", "Completed"},
                {5, 5, 5, 5, "12-78-901234-45", "에어컨 충전", "2023-08-10", 80000, "2023-09-10", null, "Completed"},
                {6, 6, 6, 6, "12-89-012345-56", "변속기 오일 교체", "2023-08-15", 100000, "2023-09-15", "점검", "Completed"},
                {7, 7, 7, 7, "12-90-123456-67", "냉각수 교체", "2023-09-01", 70000, "2023-10-01", null, "Completed"},
                {8, 8, 8, 8, "13-01-234567-78", "전기 시스템 수리", "2023-09-05", 200000, "2023-10-05", "터미널 교체", "Completed"},
                {9, 9, 9, 9, "13-12-345678-89", "와이퍼 교체", "2023-09-10", 60000, "2023-10-10", null, "Completed"},
                {10, 10, 10, 10, "13-23-456789-90", "헤드라이트 교체", "2023-09-15", 120000, "2023-10-15", "테일라이트 점검", "Completed"},
                {11, 11, 11, 11, "13-34-567890-01", "휠 얼라인먼트", "2023-09-20", 80000, "2023-10-20", null, "Completed"},
                {12, 12, 12, 12, "13-45-678901-12", "서스펜션 수리", "2023-09-25", 250000, "2023-10-25", "쇼크업소버 교체", "Completed"}
            };
            
            for (Object[] row : data) {
                pstmt.setInt(1, (Integer) row[0]);
                pstmt.setInt(2, (Integer) row[1]);
                pstmt.setInt(3, (Integer) row[2]);
                pstmt.setInt(4, (Integer) row[3]);
                pstmt.setString(5, (String) row[4]);
                pstmt.setString(6, (String) row[5]);
                pstmt.setDate(7, Date.valueOf((String) row[6]));
                pstmt.setInt(8, (Integer) row[7]);
                pstmt.setDate(9, Date.valueOf((String) row[8]));
                pstmt.setString(10, (String) row[9]);
                pstmt.setString(11, (String) row[10]);
                pstmt.executeUpdate();
            }
        }
    }
}
