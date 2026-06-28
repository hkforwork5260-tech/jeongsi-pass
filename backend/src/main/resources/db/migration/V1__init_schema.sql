-- 합격각 v0.1 초기 스키마 (정시 합격 분석)
-- 모든 시간 컬럼은 UTC TIMESTAMPTZ. 사용자 노출 시점에 KST 변환.
-- 핵심 아이템 = recruitment_units(모집단위 = 대학 × 학과 × 전형 × 군).

-- ============================================================
-- 1. 대학 (universities)
-- ============================================================
CREATE TABLE universities (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(120) NOT NULL UNIQUE,
    region        VARCHAR(40)  NOT NULL,                       -- 서울/경기/인천/대전 ...
    est_type      VARCHAR(20)  NOT NULL DEFAULT 'private',     -- national(국립) / private(사립) / special(특수)
    logo_url      TEXT,
    homepage_url  TEXT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_universities_region ON universities(region);
COMMENT ON COLUMN universities.est_type IS 'national | private | special';

-- ============================================================
-- 2. 모집단위 (recruitment_units) = 대학 × 학과 × 전형 × 군
-- ============================================================
CREATE TABLE recruitment_units (
    id              BIGSERIAL PRIMARY KEY,
    university_id   BIGINT       NOT NULL REFERENCES universities(id) ON DELETE CASCADE,
    department_name VARCHAR(120) NOT NULL,                     -- 컴퓨터공학과, 경영학과 ...
    admission_type  VARCHAR(80)  NOT NULL DEFAULT '일반전형',   -- 전형명
    recruit_group   VARCHAR(4)   NOT NULL,                     -- GA(가) / NA(나) / DA(다) / OUT(군외)
    track           VARCHAR(16)  NOT NULL,                     -- humanities(인문) / natural(자연) / arts(예체능)
    field           VARCHAR(40),                               -- 계열 세분: 공학/경영/의학/사범 ... (선택)
    quota           INT          NOT NULL DEFAULT 0,           -- 모집인원
    admission_year  INT          NOT NULL,                     -- 2027
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_units_university ON recruitment_units(university_id);
CREATE INDEX idx_units_group      ON recruitment_units(recruit_group);
CREATE INDEX idx_units_track      ON recruitment_units(track);
COMMENT ON COLUMN recruitment_units.recruit_group IS '정시 군: GA/NA/DA/OUT. 각 군에서 1개씩만 지원 가능.';
COMMENT ON COLUMN recruitment_units.track         IS 'humanities | natural | arts';

-- ============================================================
-- 3. 대학별 환산 규칙 (score_rules) — 반영비율 + 가산/지정
-- ============================================================
CREATE TABLE score_rules (
    id                  BIGSERIAL PRIMARY KEY,
    recruitment_unit_id BIGINT       NOT NULL UNIQUE REFERENCES recruitment_units(id) ON DELETE CASCADE,
    weight_korean       NUMERIC(5,2) NOT NULL,                 -- 국어 반영비율 %
    weight_math         NUMERIC(5,2) NOT NULL,                 -- 수학 %
    weight_english      NUMERIC(5,2) NOT NULL,                 -- 영어 %
    weight_inquiry      NUMERIC(5,2) NOT NULL,                 -- 탐구 %
    math_required       VARCHAR(20)  NOT NULL DEFAULT 'any',   -- any / calculus_geometry(미적·기하 지정)
    inquiry_required    VARCHAR(20)  NOT NULL DEFAULT 'any',   -- any / science(과탐 지정)
    english_grade_score JSONB        NOT NULL,                 -- 영어 등급→점수 환산표 {"1":100,"2":97,...}
    base_score          NUMERIC(7,2) NOT NULL DEFAULT 100,     -- 환산 만점 스케일
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE  score_rules                     IS '대학마다 다른 정시 환산식 파라미터. 합격 판정 시 사용.';
COMMENT ON COLUMN score_rules.weight_korean       IS '국·수·영·탐 반영비율 합 = 100 가정(영어가 가/감점인 대학은 별도 처리 — v0.1은 비율형).';
COMMENT ON COLUMN score_rules.english_grade_score IS 'JSONB: 영어 등급(1~9) → 환산 점수. 대학마다 다름.';

-- ============================================================
-- 4. 배치컷·지표 (cutoffs) — 연도별. 별도 테이블 → JOIN + 이력 가능.
-- ============================================================
CREATE TABLE cutoffs (
    id                  BIGSERIAL PRIMARY KEY,
    recruitment_unit_id BIGINT       NOT NULL REFERENCES recruitment_units(id) ON DELETE CASCADE,
    year                INT          NOT NULL,
    cut_percentile      NUMERIC(5,2) NOT NULL,                 -- 배치컷(백분위, 70%컷 가정)
    competition_rate    NUMERIC(6,2),                          -- 경쟁률 (12.40 = 12.4:1)
    applicant_avg       NUMERIC(5,2),                          -- 지원자 평균(백분위)
    source              VARCHAR(20)  NOT NULL DEFAULT 'dummy',  -- dummy / megastudy / ...
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (recruitment_unit_id, year)
);
CREATE INDEX idx_cutoffs_unit ON cutoffs(recruitment_unit_id);
CREATE INDEX idx_cutoffs_pct  ON cutoffs(cut_percentile);
COMMENT ON COLUMN cutoffs.cut_percentile IS '배치컷 백분위. 내 환산점수와 비교해 합격 라벨 산출.';
COMMENT ON COLUMN cutoffs.source         IS '데이터 출처. dummy=더미. 실데이터 도착 시 megastudy 등으로 교체.';

-- ============================================================
-- 5. 익명 기기 (devices) — 로그인 없이 기기 UUID로 사용자 구분
-- ============================================================
CREATE TABLE devices (
    id          BIGSERIAL   PRIMARY KEY,
    device_id   VARCHAR(64) NOT NULL UNIQUE,                   -- 앱이 만든 UUID (X-Device-Id 헤더)
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 6. 내 수능 성적 (user_scores) — 기기당 1행(최신)
-- ============================================================
CREATE TABLE user_scores (
    id               BIGSERIAL   PRIMARY KEY,
    device_id        BIGINT      NOT NULL UNIQUE REFERENCES devices(id) ON DELETE CASCADE,
    -- 국어
    korean_subject   VARCHAR(20),                              -- speech_writing(화작) / language_media(언매)
    korean_std       INT,                                      -- 표준점수
    korean_pct       INT,                                      -- 백분위 0~100
    korean_grade     INT,                                      -- 등급 1~9
    -- 수학
    math_subject     VARCHAR(20),                              -- probability(확통) / calculus(미적) / geometry(기하)
    math_std         INT,
    math_pct         INT,
    math_grade       INT,
    -- 영어 (절대평가 등급만)
    english_grade    INT,
    -- 한국사 (절대평가 등급만)
    history_grade    INT,
    -- 탐구 1
    inquiry1_subject VARCHAR(40),                              -- 생활과윤리, 물리학Ⅰ ...
    inquiry1_type    VARCHAR(10),                              -- social(사탐) / science(과탐)
    inquiry1_std     INT,
    inquiry1_pct     INT,
    inquiry1_grade   INT,
    -- 탐구 2
    inquiry2_subject VARCHAR(40),
    inquiry2_type    VARCHAR(10),
    inquiry2_std     INT,
    inquiry2_pct     INT,
    inquiry2_grade   INT,
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 7. 모의지원 (mock_applications)
-- ============================================================
CREATE TABLE mock_applications (
    id                  BIGSERIAL  PRIMARY KEY,
    device_id           BIGINT     NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    recruitment_unit_id BIGINT     NOT NULL REFERENCES recruitment_units(id) ON DELETE CASCADE,
    recruit_group       VARCHAR(4) NOT NULL,                   -- 담을 때의 군(편의 중복 저장)
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (device_id, recruitment_unit_id)
);
CREATE INDEX idx_mockapp_device ON mock_applications(device_id);
CREATE INDEX idx_mockapp_unit   ON mock_applications(recruitment_unit_id);

-- ============================================================
-- 8. 관심 학과 (favorites)
-- ============================================================
CREATE TABLE favorites (
    id                  BIGSERIAL   PRIMARY KEY,
    device_id           BIGINT      NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    recruitment_unit_id BIGINT      NOT NULL REFERENCES recruitment_units(id) ON DELETE CASCADE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (device_id, recruitment_unit_id)
);
CREATE INDEX idx_favorites_device ON favorites(device_id);
