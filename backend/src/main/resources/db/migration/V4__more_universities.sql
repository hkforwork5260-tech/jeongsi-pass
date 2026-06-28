-- 더미 대학 확장: 17지역 필터가 의미 있도록 수도권+지방 대학 18곳·모집단위 36개 추가.
-- ⚠️ 배치컷·경쟁률 현실 근사 placeholder. cutoffs.source='dummy'.

-- 대학 11~28 (서울 외 지역 다수)
INSERT INTO universities (id, name, region, est_type, homepage_url) VALUES
    (11, '서울시립대학교', '서울', 'national', 'https://www.uos.ac.kr'),
    (12, '이화여자대학교', '서울', 'private',  'https://www.ewha.ac.kr'),
    (13, '한국외국어대학교','서울', 'private',  'https://www.hufs.ac.kr'),
    (14, '동국대학교',     '서울', 'private',  'https://www.dongguk.edu'),
    (15, '세종대학교',     '서울', 'private',  'https://www.sejong.ac.kr'),
    (16, '인하대학교',     '인천', 'private',  'https://www.inha.ac.kr'),
    (17, '아주대학교',     '경기', 'private',  'https://www.ajou.ac.kr'),
    (18, '경기대학교',     '경기', 'private',  'https://www.kyonggi.ac.kr'),
    (19, '충남대학교',     '대전', 'national', 'https://www.cnu.ac.kr'),
    (20, '한밭대학교',     '대전', 'national', 'https://www.hanbat.ac.kr'),
    (21, '경북대학교',     '대구', 'national', 'https://www.knu.ac.kr'),
    (22, '전남대학교',     '광주', 'national', 'https://www.jnu.ac.kr'),
    (23, '강원대학교',     '강원', 'national', 'https://www.kangwon.ac.kr'),
    (24, '충북대학교',     '충북', 'national', 'https://www.chungbuk.ac.kr'),
    (25, '전북대학교',     '전북', 'national', 'https://www.jbnu.ac.kr'),
    (26, '경상국립대학교', '경남', 'national', 'https://www.gnu.ac.kr'),
    (27, '울산대학교',     '울산', 'private',  'https://www.ulsan.ac.kr'),
    (28, '제주대학교',     '제주', 'national', 'https://www.jejunu.ac.kr');

-- 모집단위 41~76 (대학별 2개: 자연 1 + 인문/사회 1)
INSERT INTO recruitment_units (id, university_id, department_name, admission_type, recruit_group, track, field, quota, admission_year) VALUES
    (41, 11, '도시공학과',     '일반전형', 'NA', 'natural',    '공학', 30, 2027),
    (42, 11, '행정학과',       '일반전형', 'NA', 'humanities', '사회', 25, 2027),
    (43, 12, '컴퓨터공학과',   '일반전형', 'NA', 'natural',    '공학', 25, 2027),
    (44, 12, '영어영문학과',   '일반전형', 'NA', 'humanities', '인문', 20, 2027),
    (45, 13, 'AI융합학부',     '일반전형', 'GA', 'natural',    '공학', 40, 2027),
    (46, 13, '경영학부',       '일반전형', 'NA', 'humanities', '경영', 50, 2027),
    (47, 14, '컴퓨터공학과',   '일반전형', 'GA', 'natural',    '공학', 30, 2027),
    (48, 14, '경영학과',       '일반전형', 'NA', 'humanities', '경영', 40, 2027),
    (49, 15, '소프트웨어학과', '일반전형', 'GA', 'natural',    '공학', 35, 2027),
    (50, 15, '경영학부',       '일반전형', 'NA', 'humanities', '경영', 45, 2027),
    (51, 16, '기계공학과',     '일반전형', 'GA', 'natural',    '공학', 50, 2027),
    (52, 16, '경영학과',       '일반전형', 'NA', 'humanities', '경영', 40, 2027),
    (53, 17, '소프트웨어학과', '일반전형', 'DA', 'natural',    '공학', 40, 2027),
    (54, 17, '경영학과',       '일반전형', 'NA', 'humanities', '경영', 35, 2027),
    (55, 18, '컴퓨터공학부',   '일반전형', 'NA', 'natural',    '공학', 30, 2027),
    (56, 18, '경영학과',       '일반전형', 'GA', 'humanities', '경영', 40, 2027),
    (57, 19, '의예과',         '일반전형', 'NA', 'natural',    '의학', 30, 2027),
    (58, 19, '전자공학과',     '일반전형', 'GA', 'natural',    '공학', 45, 2027),
    (59, 20, '컴퓨터공학과',   '일반전형', 'NA', 'natural',    '공학', 25, 2027),
    (60, 20, '건축공학과',     '일반전형', 'GA', 'natural',    '공학', 20, 2027),
    (61, 21, '의예과',         '일반전형', 'GA', 'natural',    '의학', 30, 2027),
    (62, 21, '경영학부',       '일반전형', 'NA', 'humanities', '경영', 50, 2027),
    (63, 22, '의예과',         '일반전형', 'GA', 'natural',    '의학', 30, 2027),
    (64, 22, '국어국문학과',   '일반전형', 'NA', 'humanities', '인문', 20, 2027),
    (65, 23, '간호학과',       '일반전형', 'NA', 'natural',    '의학', 30, 2027),
    (66, 23, '경영학과',       '일반전형', 'GA', 'humanities', '경영', 35, 2027),
    (67, 24, '수의예과',       '일반전형', 'NA', 'natural',    '의학', 25, 2027),
    (68, 24, '행정학과',       '일반전형', 'GA', 'humanities', '사회', 25, 2027),
    (69, 25, '전자공학부',     '일반전형', 'NA', 'natural',    '공학', 40, 2027),
    (70, 25, '경영학과',       '일반전형', 'GA', 'humanities', '경영', 30, 2027),
    (71, 26, '의예과',         '일반전형', 'GA', 'natural',    '의학', 35, 2027),
    (72, 26, '기계공학부',     '일반전형', 'NA', 'natural',    '공학', 45, 2027),
    (73, 27, '의예과',         '일반전형', 'NA', 'natural',    '의학', 30, 2027),
    (74, 27, '경영학부',       '일반전형', 'GA', 'humanities', '경영', 40, 2027),
    (75, 28, '해양생명과학과', '일반전형', 'NA', 'natural',    '공학', 25, 2027),
    (76, 28, '행정학과',       '일반전형', 'GA', 'humanities', '사회', 20, 2027);

-- 환산 규칙 (신규 유닛만). V2와 동일 CASE 로직. index_type 등 신규 컬럼은 기본값.
INSERT INTO score_rules (recruitment_unit_id, weight_korean, weight_math, weight_english, weight_inquiry, math_required, inquiry_required, english_grade_score, base_score)
SELECT
    u.id,
    CASE WHEN u.track = 'humanities' THEN 30 ELSE 20 END,
    CASE WHEN u.track = 'humanities' THEN 25 ELSE 35 END,
    20, 25,
    CASE WHEN u.track = 'natural' AND u.field IN ('의학','공학') THEN 'calculus_geometry' ELSE 'any' END,
    CASE WHEN u.track = 'natural' AND u.field = '의학' THEN 'science' ELSE 'any' END,
    '{"1":100,"2":98,"3":94,"4":88,"5":80,"6":70,"7":58,"8":44,"9":28}'::jsonb,
    100
FROM recruitment_units u WHERE u.id > 40;

-- 의학계열은 표준점수 활용지표로
UPDATE score_rules sr SET index_type = 'std'
  FROM recruitment_units ru
 WHERE sr.recruitment_unit_id = ru.id AND ru.id > 40 AND ru.field = '의학';

-- 배치컷 (신규 유닛). 수도권>지방, 의예 매우 높음. target_avg=applicant_avg+1.2.
INSERT INTO cutoffs (recruitment_unit_id, year, cut_percentile, competition_rate, applicant_avg, target_avg, source) VALUES
    (41, 2027, 87.0, 6.10, 88.0, 89.2, 'dummy'),
    (42, 2027, 86.0, 7.30, 87.1, 88.3, 'dummy'),
    (43, 2027, 90.5, 8.40, 91.3, 92.5, 'dummy'),
    (44, 2027, 88.5, 6.70, 89.4, 90.6, 'dummy'),
    (45, 2027, 88.0, 7.10, 88.9, 90.1, 'dummy'),
    (46, 2027, 88.7, 6.20, 89.5, 90.7, 'dummy'),
    (47, 2027, 87.3, 7.80, 88.2, 89.4, 'dummy'),
    (48, 2027, 86.8, 6.50, 87.7, 88.9, 'dummy'),
    (49, 2027, 86.0, 7.40, 87.0, 88.2, 'dummy'),
    (50, 2027, 85.5, 6.10, 86.5, 87.7, 'dummy'),
    (51, 2027, 84.0, 6.90, 85.1, 86.3, 'dummy'),
    (52, 2027, 83.0, 5.80, 84.2, 85.4, 'dummy'),
    (53, 2027, 86.5, 9.10, 87.3, 88.5, 'dummy'),
    (54, 2027, 85.0, 6.30, 86.0, 87.2, 'dummy'),
    (55, 2027, 80.0, 5.40, 81.2, 82.4, 'dummy'),
    (56, 2027, 79.0, 5.10, 80.3, 81.5, 'dummy'),
    (57, 2027, 96.3, 9.80, 96.9, 98.1, 'dummy'),
    (58, 2027, 82.0, 5.60, 83.1, 84.3, 'dummy'),
    (59, 2027, 78.5, 4.90, 79.8, 81.0, 'dummy'),
    (60, 2027, 76.0, 4.50, 77.4, 78.6, 'dummy'),
    (61, 2027, 96.7, 11.20, 97.2, 98.4, 'dummy'),
    (62, 2027, 83.5, 5.70, 84.5, 85.7, 'dummy'),
    (63, 2027, 96.5, 10.40, 97.0, 98.2, 'dummy'),
    (64, 2027, 77.0, 4.60, 78.3, 79.5, 'dummy'),
    (65, 2027, 85.0, 7.20, 85.9, 87.1, 'dummy'),
    (66, 2027, 79.5, 5.00, 80.7, 81.9, 'dummy'),
    (67, 2027, 95.0, 12.30, 95.6, 96.8, 'dummy'),
    (68, 2027, 76.5, 4.70, 77.8, 79.0, 'dummy'),
    (69, 2027, 79.0, 4.80, 80.2, 81.4, 'dummy'),
    (70, 2027, 77.5, 4.40, 78.8, 80.0, 'dummy'),
    (71, 2027, 96.0, 9.60, 96.6, 97.8, 'dummy'),
    (72, 2027, 80.5, 5.20, 81.6, 82.8, 'dummy'),
    (73, 2027, 96.8, 13.10, 97.3, 98.5, 'dummy'),
    (74, 2027, 80.0, 5.30, 81.1, 82.3, 'dummy'),
    (75, 2027, 75.0, 4.20, 76.4, 77.6, 'dummy'),
    (76, 2027, 74.0, 4.10, 75.5, 76.7, 'dummy');

SELECT setval('universities_id_seq',     (SELECT MAX(id) FROM universities));
SELECT setval('recruitment_units_id_seq', (SELECT MAX(id) FROM recruitment_units));
