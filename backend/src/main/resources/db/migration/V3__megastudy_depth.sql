-- 메가 합격예측 충실도 보강: 반영비율 환산·상위누적%·5단계·경쟁자평균·목표대학·원점수.
-- ⚠️ 분포 파라미터(score_dist_params)·target_avg는 현실 근사 placeholder. 실데이터 시 교체.

-- ============================================================
-- 1. score_rules 확장 — 활용지표 / 탐구반영개수 / 반영비율(수능·내신·기타)
-- ============================================================
ALTER TABLE score_rules ADD COLUMN index_type    VARCHAR(10)  NOT NULL DEFAULT 'both';  -- std(표준) / pct(백분위) / both(표+백)
ALTER TABLE score_rules ADD COLUMN inquiry_count INT          NOT NULL DEFAULT 2;        -- 반영 탐구 과목 수 (1 또는 2)
ALTER TABLE score_rules ADD COLUMN suneung_ratio NUMERIC(5,2) NOT NULL DEFAULT 100;      -- 수능 반영비율 %
ALTER TABLE score_rules ADD COLUMN naesin_ratio  NUMERIC(5,2) NOT NULL DEFAULT 0;        -- 내신 %
ALTER TABLE score_rules ADD COLUMN etc_ratio     NUMERIC(5,2) NOT NULL DEFAULT 0;        -- 기타(실기·면접) %
COMMENT ON COLUMN score_rules.index_type IS '활용지표: std | pct | both. 반영영역은 weight>0으로 파생.';

-- 더미 변주: 상위권 자연(의/약/반도체)은 표준점수, 인문 상위는 표+백, 나머지 기본 both. 탐구는 대부분 2개.
UPDATE score_rules sr SET index_type = 'std'
  FROM recruitment_units ru
 WHERE sr.recruitment_unit_id = ru.id AND ru.field IN ('의학','약학') ;
UPDATE score_rules sr SET inquiry_count = 1
  FROM recruitment_units ru
 WHERE sr.recruitment_unit_id = ru.id AND ru.track = 'humanities' AND ru.field = '경영';

-- ============================================================
-- 2. cutoffs 확장 — 목표등록자 평균(목표대학으로 등록한 사람들의 평균; 보통 배치컷보다 약간 높음)
-- ============================================================
ALTER TABLE cutoffs ADD COLUMN target_avg NUMERIC(5,2);
COMMENT ON COLUMN cutoffs.target_avg IS '목표등록자 평균(백분위). 모의지원자 평균=applicant_avg.';
UPDATE cutoffs SET target_avg = LEAST(applicant_avg + 1.2, 100.0) WHERE applicant_avg IS NOT NULL;

-- ============================================================
-- 3. user_scores 확장 — 필수정보 + 원점수(공통/선택 분리)
-- ============================================================
ALTER TABLE user_scores ADD COLUMN exam_track    VARCHAR(16);   -- humanities / natural (응시계열)
ALTER TABLE user_scores ADD COLUMN gender        VARCHAR(8);    -- male / female
ALTER TABLE user_scores ADD COLUMN grad_year     VARCHAR(20);   -- 2027 / 2026 / before_2025 ...
ALTER TABLE user_scores ADD COLUMN korean_common INT;           -- 국어 공통 원점수
ALTER TABLE user_scores ADD COLUMN korean_select INT;           -- 국어 선택 원점수
ALTER TABLE user_scores ADD COLUMN korean_raw    INT;           -- 국어 원점수 합
ALTER TABLE user_scores ADD COLUMN math_common   INT;
ALTER TABLE user_scores ADD COLUMN math_select   INT;
ALTER TABLE user_scores ADD COLUMN math_raw      INT;
ALTER TABLE user_scores ADD COLUMN english_raw   INT;
ALTER TABLE user_scores ADD COLUMN history_raw   INT;
ALTER TABLE user_scores ADD COLUMN inquiry1_raw  INT;
ALTER TABLE user_scores ADD COLUMN inquiry2_raw  INT;

-- ============================================================
-- 4. 목표 대학 (target_units) — 군별 1지망/2지망
-- ============================================================
CREATE TABLE target_units (
    id                  BIGSERIAL   PRIMARY KEY,
    device_id           BIGINT      NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    recruitment_unit_id BIGINT      NOT NULL REFERENCES recruitment_units(id) ON DELETE CASCADE,
    recruit_group       VARCHAR(4)  NOT NULL,
    priority            INT         NOT NULL DEFAULT 1,   -- 1지망 / 2지망
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (device_id, recruit_group, priority)
);
CREATE INDEX idx_target_device ON target_units(device_id);

-- ============================================================
-- 5. 반영유형별 전국 점수 분포 (score_dist_params)
--    상위누적% = (1 - 정규CDF((내총점 - mean)/stddev)) × 100  → ScoringService에서 계산.
--    ⚠️ placeholder 정규분포 가정. 실데이터 시 교체.
-- ============================================================
CREATE TABLE score_dist_params (
    id           BIGSERIAL    PRIMARY KEY,
    reflect_type VARCHAR(16)  NOT NULL,   -- 국수영탐 / 국수영과 / 국수영사 / 국영탐
    index_type   VARCHAR(10)  NOT NULL,   -- std / pct
    mean         NUMERIC(7,2) NOT NULL,
    stddev       NUMERIC(6,2) NOT NULL,
    max_score    NUMERIC(7,2) NOT NULL,
    UNIQUE (reflect_type, index_type)
);
COMMENT ON TABLE score_dist_params IS '반영유형별 전국 분포 파라미터(평균·표준편차). 상위누적% 산출용. placeholder.';

INSERT INTO score_dist_params (reflect_type, index_type, mean, stddev, max_score) VALUES
    ('국수영탐', 'std', 320, 45, 420),   -- 표준점수 국+수+탐(합)
    ('국수영탐', 'pct', 211, 34, 300),   -- 백분위 국+수+탐(평균)
    ('국수영과', 'std', 340, 40, 420),   -- 과탐 응시집단이 상위권 → 같은 점수도 누적% 높음
    ('국수영과', 'pct', 224, 32, 300),
    ('국수영사', 'std', 305, 42, 420),
    ('국수영사', 'pct', 205, 33, 300),
    ('국영탐',   'std', 235, 35, 300),   -- 수학 미반영(인문 일부)
    ('국영탐',   'pct', 160, 28, 200);
