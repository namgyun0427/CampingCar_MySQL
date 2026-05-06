-- 이름: camping_db.sql
-- 설명: 캠핑카 예약 시스템 데이터베이스 초기화

/* root 계정으로 접속, camping_db 데이터베이스 생성, user1 계정 생성 */
/* MySQL Workbench에서 초기화면에서 +를 눌러 root Connection을 만들어 접속한다. */
/* user : user1, database : camping_db */
/* root 계정: root/1234, user1 계정: user1/user1 */

-- root 계정이 없으면 생성하고, 비밀번호를 1234로 설정
CREATE USER IF NOT EXISTS 'root'@'localhost' IDENTIFIED BY '1234';

-- root 계정에 모든 권한 부여
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;

DROP DATABASE IF EXISTS camping_db;
CREATE DATABASE IF NOT EXISTS camping_db;
USE camping_db;


-- user1 계정을 암호 user1으로 만들고 캠핑카 대여 관련 테이블에만 권한 부여
CREATE USER IF NOT EXISTS 'user1'@'localhost' IDENTIFIED BY 'user1';

-- 처음 실행시는 아래 테이블 삭제 오류는 무시한다.
DROP TABLE IF EXISTS ExternalRepair;
DROP TABLE IF EXISTS InternalRepair;
DROP TABLE IF EXISTS Rental;
DROP TABLE IF EXISTS Part;
DROP TABLE IF EXISTS ExternalRepairShop;
DROP TABLE IF EXISTS Camper;
DROP TABLE IF EXISTS Employee;
DROP TABLE IF EXISTS Customer;
DROP TABLE IF EXISTS Company;

-- Company 테이블 (캠핑카 회사)
CREATE TABLE Company (
  company_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  address VARCHAR(200),
  phone VARCHAR(20) NOT NULL,
  manager_name VARCHAR(50) NOT NULL,
  manager_email VARCHAR(100) NOT NULL UNIQUE
);

-- Customer 테이블 (고객)
CREATE TABLE Customer (
  customer_id VARCHAR(30) PRIMARY KEY,
  username VARCHAR(30) UNIQUE NOT NULL,
  password VARCHAR(60) NOT NULL,
  license_number VARCHAR(30) UNIQUE NOT NULL,
  name VARCHAR(50) NOT NULL,
  address VARCHAR(200),
  phone VARCHAR(20) NOT NULL,
  email VARCHAR(100),
  recent_rental DATE,
  preferred_type VARCHAR(50)
);

-- Employee 테이블 (직원)
CREATE TABLE Employee (
  employee_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  address VARCHAR(200),
  salary DECIMAL(10,2),
  num_dependents INT,
  department VARCHAR(50) NOT NULL,
  role ENUM('관리', '사무', '정비') NOT NULL,
  company_id INT NOT NULL,
  FOREIGN KEY (company_id) REFERENCES Company(company_id)
);

-- Camper 테이블 (캠핑카)
CREATE TABLE Camper (
  camper_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  plate_number VARCHAR(20) UNIQUE NOT NULL,
  capacity INT,
  image_url TEXT,
  description TEXT,
  rental_price DECIMAL(10,2),
  company_id INT NOT NULL,
  registration_date DATE NOT NULL,
  status ENUM('Available', 'Rented', 'Maintenance') DEFAULT 'Available',
  FOREIGN KEY (company_id) REFERENCES Company(company_id)
);

-- Part 테이블 (부품)
CREATE TABLE Part (
  part_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  unit_price DECIMAL(10,2),
  quantity_in_stock INT,
  received_date DATE NOT NULL,
  supplier_name VARCHAR(100) NOT NULL
);

-- ExternalRepairShop 테이블 (외부 정비소)
CREATE TABLE ExternalRepairShop (
  repair_shop_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  address VARCHAR(200) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  contact_name VARCHAR(50) NOT NULL,
  contact_email VARCHAR(100) NOT NULL UNIQUE
);

-- Rental 테이블 (대여)
CREATE TABLE Rental (
  rental_id INT AUTO_INCREMENT PRIMARY KEY,
  camper_id INT NOT NULL,
  license_number VARCHAR(30) NOT NULL,
  company_id INT NOT NULL,
  start_date DATE NOT NULL,
  duration_days INT,
  base_charge DECIMAL(10,2),
  payment_due_date DATE NOT NULL,
  extra_details TEXT,
  extra_charge DECIMAL(10,2) DEFAULT 0,
  status ENUM('Reserved', 'Active', 'Completed', 'Cancelled') DEFAULT 'Reserved',
  FOREIGN KEY (camper_id) REFERENCES Camper(camper_id),
  FOREIGN KEY (license_number) REFERENCES Customer(license_number),
  FOREIGN KEY (company_id) REFERENCES Company(company_id)
);

-- InternalRepair 테이블 (내부 정비)
CREATE TABLE InternalRepair (
  repair_id INT AUTO_INCREMENT PRIMARY KEY,
  camper_id INT NOT NULL,
  part_id INT NOT NULL,
  repair_date DATE NOT NULL,
  repair_duration_minutes INT,
  employee_id INT NOT NULL,
  FOREIGN KEY (camper_id) REFERENCES Camper(camper_id),
  FOREIGN KEY (part_id) REFERENCES Part(part_id),
  FOREIGN KEY (employee_id) REFERENCES Employee(employee_id)
);

-- ExternalRepair 테이블 (외부 정비)
CREATE TABLE ExternalRepair (
  external_repair_id INT AUTO_INCREMENT PRIMARY KEY,
  camper_id INT NOT NULL,
  repair_shop_id INT NOT NULL,
  company_id INT NOT NULL,
  license_number VARCHAR(30) NOT NULL,
  repair_details TEXT NOT NULL,
  repair_date DATE NOT NULL,
  cost DECIMAL(10,2),
  payment_due_date DATE NOT NULL,
  extra_details TEXT,
  status ENUM('Requested', 'In Progress', 'Completed', 'Cancelled') DEFAULT 'Requested',
  FOREIGN KEY (camper_id) REFERENCES Camper(camper_id),
  FOREIGN KEY (repair_shop_id) REFERENCES ExternalRepairShop(repair_shop_id),
  FOREIGN KEY (company_id) REFERENCES Company(company_id),
  FOREIGN KEY (license_number) REFERENCES Customer(license_number)
);

-- 권한 부여
GRANT SELECT, INSERT, UPDATE, DELETE ON camping_db.Customer TO 'user1'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON camping_db.Camper TO 'user1'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON camping_db.Rental TO 'user1'@'localhost';
GRANT SELECT ON camping_db.ExternalRepairShop TO 'user1'@'localhost';
GRANT SELECT, INSERT ON camping_db.ExternalRepair TO 'user1'@'localhost';
FLUSH PRIVILEGES;

-- Company
INSERT INTO Company VALUES (1, '캠핑카월드', '서울 강남구', '02-1234-5678', '김관리', 'campworld@naver.com');
INSERT INTO Company VALUES (2, '자연과함께', '부산 해운대구', '051-987-6543', '이자연', 'nature@daum.net');
INSERT INTO Company VALUES (3, '로드트립캠핑', '제주 노형동', '064-332-1100', '박로드', 'road@google.com');
INSERT INTO Company VALUES (4, '어드벤처캠핑', '강원 춘천시', '033-456-7890', '최어드', 'adventure@naver.com');
INSERT INTO Company VALUES (5, '가족캠핑카', '경기 가평군', '031-123-4567', '정가족', 'family@daum.net');
INSERT INTO Company VALUES (6, '산악캠핑', '강원 평창군', '033-789-1234', '임산악', 'mountain@google.com');
INSERT INTO Company VALUES (7, '바다로캠핑', '경남 통영시', '055-456-7890', '김바다', 'sea@naver.com');
INSERT INTO Company VALUES (8, '시티캠핑카', '대전 유성구', '042-123-4567', '한시티', 'city@daum.net');
INSERT INTO Company VALUES (9, '럭셔리캠핑', '서울 송파구', '02-987-6543', '오럭셔리', 'luxury@google.com');
INSERT INTO Company VALUES (10, '에코캠핑카', '전북 전주시', '063-332-1100', '조에코', 'eco@naver.com');
INSERT INTO Company VALUES (11, '캠핑카렌탈', '인천 연수구', '032-456-7890', '나렌탈', 'rental@daum.net');
INSERT INTO Company VALUES (12, '휴가캠핑카', '경북 경주시', '054-123-4567', '문휴가', 'holiday@google.com');

-- Customer
INSERT INTO Customer VALUES ('CUST0001', 'user1', 'pass1', '12-34-567890-01', '홍길동', '강남구', '010-1234-5678', 'hong@naver.com', '2023-01-15', '럭셔리');
INSERT INTO Customer VALUES ('CUST0002', 'user2', 'pass2', '12-45-678901-12', '김철수', '해운대구', '010-2345-6789', 'kim@daum.net', '2023-02-20', '가족형');
INSERT INTO Customer VALUES ('CUST0003', 'user3', 'pass3', '12-56-789012-23', '박영희', '수성구', '010-3456-7890', 'park@google.com', '2023-03-10', '커플');
INSERT INTO Customer VALUES ('CUST0004', 'user4', 'pass4', '12-67-890123-34', '이영수', '남동구', '010-4567-8901', 'lee@naver.com', '2023-04-05', '소형');
INSERT INTO Customer VALUES ('CUST0005', 'user5', 'pass5', '12-78-901234-45', '최민지', '서구', '010-5678-9012', 'choi@daum.net', '2023-05-12', '대형');
INSERT INTO Customer VALUES ('CUST0006', 'user6', 'pass6', '12-89-012345-56', '정수민', '유성구', '010-6789-0123', 'jung@google.com', '2023-06-25', '럭셔리');
INSERT INTO Customer VALUES ('CUST0007', 'user7', 'pass7', '12-90-123456-67', '강동원', '남구', '010-7890-1234', 'kang@naver.com', '2023-07-08', '가족형');
INSERT INTO Customer VALUES ('CUST0008', 'user8', 'pass8', '13-01-234567-78', '윤서연', '가람동', '010-8901-2345', 'yoon@daum.net', '2023-08-17', '커플');
INSERT INTO Customer VALUES ('CUST0009', 'user9', 'pass9', '13-12-345678-89', '신지민', '팔달구', '010-9012-3456', 'shin@google.com', '2023-09-22', '소형');
INSERT INTO Customer VALUES ('CUST0010', 'user10', 'pass10', '13-23-456789-90', '장현우', '효자동', '010-0123-4567', 'jang@naver.com', '2023-10-30', '대형');
INSERT INTO Customer VALUES ('CUST0011', 'user11', 'pass11', '13-34-567890-01', '한소희', '흥덕구', '010-1234-5678', 'han@daum.net', '2023-11-15', '럭셔리');
INSERT INTO Customer VALUES ('CUST0012', 'user12', 'pass12', '13-45-678901-12', '송태민', '서북구', '010-2345-6789', 'song@google.com', '2023-12-05', '가족형');

-- Employee
INSERT INTO Employee VALUES (1, '김정비', '010-1111-2222', '강남구', 3500000, 2, '정비부', '정비', 1);
INSERT INTO Employee VALUES (2, '이관리', '010-2222-3333', '서초구', 4500000, 1, '관리부', '관리', 1);
INSERT INTO Employee VALUES (3, '박사무', '010-3333-4444', '삼성동', 3000000, 0, '사무부', '사무', 1);
INSERT INTO Employee VALUES (4, '최정비', '010-4444-5555', '우동', 3300000, 1, '정비부', '정비', 2);
INSERT INTO Employee VALUES (5, '정관리', '010-5555-6666', '온천동', 4200000, 3, '관리부', '관리', 2);
INSERT INTO Employee VALUES (6, '강사무', '010-6666-7777', '중동', 2900000, 0, '사무부', '사무', 2);
INSERT INTO Employee VALUES (7, '윤정비', '010-7777-8888', '노형동', 3400000, 1, '정비부', '정비', 3);
INSERT INTO Employee VALUES (8, '임관리', '010-8888-9999', '이도동', 4300000, 2, '관리부', '관리', 3);
INSERT INTO Employee VALUES (9, '한사무', '010-9999-0000', '연동', 3100000, 0, '사무부', '사무', 3);
INSERT INTO Employee VALUES (10, '신정비', '010-0000-1111', '효자동', 3200000, 1, '정비부', '정비', 4);
INSERT INTO Employee VALUES (11, '조관리', '010-1234-5678', '단구동', 4000000, 2, '관리부', '관리', 4);
INSERT INTO Employee VALUES (12, '황사무', '010-2345-6789', '포남동', 2800000, 0, '사무부', '사무', 4);

-- Camper
INSERT INTO Camper VALUES (1, '럭셔리 캠핑카 A', '서울123가4567', 4, 'luxury_a.jpg', '고급 인테리어', 150000, 1, '2023-01-15', 'Available');
INSERT INTO Camper VALUES (2, '가족형 캠핑카', '경기456나7890', 6, 'family.jpg', '6인 가족용', 180000, 2, '2023-02-20', 'Available');
INSERT INTO Camper VALUES (3, '커플 캠핑카', '인천789다1234', 2, 'couple.jpg', '아늑한 공간', 100000, 3, '2023-03-10', 'Available');
INSERT INTO Camper VALUES (4, '소형 캠핑카', '부산321라9876', 2, 'small.jpg', '주차 편리', 90000, 4, '2023-04-05', 'Available');
INSERT INTO Camper VALUES (5, '대형 캠핑카', '대전654마3210', 8, 'large.jpg', '대가족용', 220000, 5, '2023-05-12', 'Available');
INSERT INTO Camper VALUES (6, '오프로드 캠핑카', '강원987바6543', 4, 'offroad.jpg', '산악용', 170000, 6, '2023-06-25', 'Available');
INSERT INTO Camper VALUES (7, '프리미엄 캠핑카', '제주210사8765', 4, 'premium.jpg', '최고급 사양', 200000, 7, '2023-07-08', 'Available');
INSERT INTO Camper VALUES (8, '도심형 캠핑카', '경남543아2109', 3, 'urban.jpg', '도심 주행', 120000, 8, '2023-08-17', 'Available');
INSERT INTO Camper VALUES (9, '럭셔리 캠핑카 B', '서울876자5432', 4, 'luxury_b.jpg', '신형 럭셔리', 160000, 9, '2023-09-22', 'Available');
INSERT INTO Camper VALUES (10, '친환경 캠핑카', '전북109차8765', 4, 'eco.jpg', '친환경 재료', 140000, 10, '2023-10-30', 'Available');
INSERT INTO Camper VALUES (11, '경제적 캠핑카', '충남432카1098', 4, 'economy.jpg', '연비 효율', 110000, 11, '2023-11-15', 'Available');
INSERT INTO Camper VALUES (12, '올인원 캠핑카', '경북765타4321', 5, 'allinone.jpg', '모든 편의시설', 190000, 12, '2023-12-05', 'Available');

-- Part
INSERT INTO Part VALUES (1, '엔진 오일', 50000, 100, '2023-01-10', '자동차부품상사');
INSERT INTO Part VALUES (2, '오일 필터', 15000, 80, '2023-01-10', '자동차부품상사');
INSERT INTO Part VALUES (3, '에어 필터', 20000, 70, '2023-01-15', '자동차부품상사');
INSERT INTO Part VALUES (4, '브레이크 패드', 80000, 40, '2023-02-05', '브레이크상사');
INSERT INTO Part VALUES (5, '타이어', 150000, 30, '2023-02-20', '타이어유통');
INSERT INTO Part VALUES (6, '배터리', 120000, 25, '2023-03-10', '전기부품');
INSERT INTO Part VALUES (7, '와이퍼', 30000, 60, '2023-03-25', '자동차부품상사');
INSERT INTO Part VALUES (8, '전조등', 25000, 45, '2023-04-05', '전기부품');
INSERT INTO Part VALUES (9, '냉각수', 35000, 55, '2023-04-20', '자동차부품상사');
INSERT INTO Part VALUES (10, '변속기 오일', 60000, 35, '2023-05-10', '자동차부품상사');
INSERT INTO Part VALUES (11, '스파크 플러그', 40000, 50, '2023-05-25', '전기부품');
INSERT INTO Part VALUES (12, '타이밍 벨트', 70000, 20, '2023-06-10', '자동차부품상사');

-- ExternalRepairShop
INSERT INTO ExternalRepairShop VALUES (1, '전문캠핑카정비소', '강남구', '02-123-4567', '박정비', 'repair1@naver.com');
INSERT INTO ExternalRepairShop VALUES (2, '빠른정비센터', '해운대구', '051-234-5678', '김빠른', 'quick2@daum.net');
INSERT INTO ExternalRepairShop VALUES (3, '종합자동차정비', '남동구', '032-345-6789', '이종합', 'general3@google.com');
INSERT INTO ExternalRepairShop VALUES (4, '캠핑카케어', '둔산동', '042-456-7890', '최케어', 'care4@naver.com');
INSERT INTO ExternalRepairShop VALUES (5, '모바일정비', '용봉동', '062-567-8901', '정모바일', 'mobile5@daum.net');
INSERT INTO ExternalRepairShop VALUES (6, '프리미엄정비샵', '범어동', '053-678-9012', '강프리미엄', 'premium6@google.com');
INSERT INTO ExternalRepairShop VALUES (7, '긴급정비', '삼산동', '052-789-0123', '윤긴급', 'emergency7@naver.com');
INSERT INTO ExternalRepairShop VALUES (8, '전문수리점', '팔달구', '031-890-1234', '장전문', 'specialist8@daum.net');
INSERT INTO ExternalRepairShop VALUES (9, '친환경정비', '효자동', '033-901-2345', '한친환경', 'eco9@google.com');
INSERT INTO ExternalRepairShop VALUES (10, '첨단정비소', '노형동', '064-012-3456', '신첨단', 'hightech10@naver.com');
INSERT INTO ExternalRepairShop VALUES (11, '원스톱정비', '남구', '054-123-4567', '조원스톱', 'onestop11@daum.net');
INSERT INTO ExternalRepairShop VALUES (12, '신뢰정비', '산정동', '061-234-5678', '황신뢰', 'trust12@google.com');

-- Rental
INSERT INTO Rental VALUES (1, 1, '12-34-567890-01', 1, '2023-07-01', 3, 450000, '2023-06-25', '추가 운전자 1명', 30000, 'Completed');
INSERT INTO Rental VALUES (2, 2, '12-45-678901-12', 2, '2023-07-05', 5, 900000, '2023-06-30', NULL, 0, 'Completed');
INSERT INTO Rental VALUES (3, 3, '12-56-789012-23', 3, '2023-07-10', 2, 200000, '2023-07-05', '아이스박스 대여', 10000, 'Completed');
INSERT INTO Rental VALUES (4, 4, '12-67-890123-34', 4, '2023-07-15', 4, 360000, '2023-07-10', NULL, 0, 'Completed');
INSERT INTO Rental VALUES (5, 5, '12-78-901234-45', 5, '2023-08-01', 7, 1540000, '2023-07-25', '캠핑 장비 세트', 50000, 'Completed');
INSERT INTO Rental VALUES (6, 6, '12-89-012345-56', 6, '2023-08-10', 3, 510000, '2023-08-05', NULL, 0, 'Completed');
INSERT INTO Rental VALUES (7, 7, '12-90-123456-67', 7, '2023-08-15', 5, 1000000, '2023-08-10', '전용 침구 세트', 20000, 'Completed');
INSERT INTO Rental VALUES (8, 8, '13-01-234567-78', 8, '2023-08-20', 2, 240000, '2023-08-15', NULL, 0, 'Completed');
INSERT INTO Rental VALUES (9, 9, '13-12-345678-89', 9, '2023-09-01', 4, 640000, '2023-08-25', '추가 주행거리', 40000, 'Completed');
INSERT INTO Rental VALUES (10, 10, '13-23-456789-90', 10, '2023-09-05', 6, 840000, '2023-08-30', NULL, 0, 'Completed');
INSERT INTO Rental VALUES (11, 11, '13-34-567890-01', 11, '2023-09-10', 3, 330000, '2023-09-05', '세차 서비스', 15000, 'Completed');
INSERT INTO Rental VALUES (12, 12, '13-45-678901-12', 12, '2023-09-15', 5, 950000, '2023-09-10', NULL, 0, 'Completed');

-- InternalRepair
INSERT INTO InternalRepair VALUES (1, 1, 1, '2023-07-15', 60, 1);
INSERT INTO InternalRepair VALUES (2, 2, 2, '2023-07-20', 45, 1);
INSERT INTO InternalRepair VALUES (3, 3, 3, '2023-07-25', 30, 1);
INSERT INTO InternalRepair VALUES (4, 4, 4, '2023-08-05', 120, 4);
INSERT INTO InternalRepair VALUES (5, 5, 5, '2023-08-10', 90, 4);
INSERT INTO InternalRepair VALUES (6, 6, 6, '2023-08-15', 60, 4);
INSERT INTO InternalRepair VALUES (7, 7, 7, '2023-09-01', 30, 7);
INSERT INTO InternalRepair VALUES (8, 8, 8, '2023-09-05', 45, 7);
INSERT INTO InternalRepair VALUES (9, 9, 9, '2023-09-10', 60, 7);
INSERT INTO InternalRepair VALUES (10, 10, 10, '2023-10-01', 90, 10);
INSERT INTO InternalRepair VALUES (11, 11, 11, '2023-10-05', 60, 10);
INSERT INTO InternalRepair VALUES (12, 12, 12, '2023-10-10', 45, 10);

-- ExternalRepair
INSERT INTO ExternalRepair VALUES (1, 1, 1, 1, '12-34-567890-01', '엔진 오일 교체', '2023-07-20', 120000, '2023-08-20', NULL, 'Completed');
INSERT INTO ExternalRepair VALUES (2, 2, 2, 2, '12-45-678901-12', '브레이크 패드 교체', '2023-07-25', 150000, '2023-08-25', '디스크 점검', 'Completed');
INSERT INTO ExternalRepair VALUES (3, 3, 3, 3, '12-56-789012-23', '배터리 교체', '2023-08-01', 180000, '2023-09-01', NULL, 'Completed');
INSERT INTO ExternalRepair VALUES (4, 4, 4, 4, '12-67-890123-34', '타이어 교체', '2023-08-05', 600000, '2023-09-05', '휠 조정', 'Completed');
INSERT INTO ExternalRepair VALUES (5, 5, 5, 5, '12-78-901234-45', '에어컨 충전', '2023-08-10', 80000, '2023-09-10', NULL, 'Completed');
INSERT INTO ExternalRepair VALUES (6, 6, 6, 6, '12-89-012345-56', '변속기 오일 교체', '2023-08-15', 100000, '2023-09-15', '점검', 'Completed');
INSERT INTO ExternalRepair VALUES (7, 7, 7, 7, '12-90-123456-67', '냉각수 교체', '2023-09-01', 70000, '2023-10-01', NULL, 'Completed');
INSERT INTO ExternalRepair VALUES (8, 8, 8, 8, '13-01-234567-78', '전기 시스템 수리', '2023-09-05', 200000, '2023-10-05', '터미널 교체', 'Completed');
INSERT INTO ExternalRepair VALUES (9, 9, 9, 9, '13-12-345678-89', '와이퍼 교체', '2023-09-10', 60000, '2023-10-10', NULL, 'Completed');
INSERT INTO ExternalRepair VALUES (10, 10, 10, 10, '13-23-456789-90', '헤드라이트 교체', '2023-09-15', 120000, '2023-10-15', '테일라이트 점검', 'Completed');
INSERT INTO ExternalRepair VALUES (11, 11, 11, 11, '13-34-567890-01', '휠 얼라인먼트', '2023-09-20', 80000, '2023-10-20', NULL, 'Completed');
INSERT INTO ExternalRepair VALUES (12, 12, 12, 12, '13-45-678901-12', '서스펜션 수리', '2023-09-25', 250000, '2023-10-25', '쇼크업소버 교체', 'Completed');
