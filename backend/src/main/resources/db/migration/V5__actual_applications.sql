-- 실제 지원 대학: 모의지원한 학과 중 군별 1개를 "실제 지원"으로 등록. 하루 3번/군 변경 제한.
CREATE TABLE actual_applications (
    id                  BIGSERIAL   PRIMARY KEY,
    device_id           BIGINT      NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    recruit_group       VARCHAR(4)  NOT NULL,                  -- GA/NA/DA
    recruitment_unit_id BIGINT      NOT NULL REFERENCES recruitment_units(id) ON DELETE CASCADE,
    change_count        INT         NOT NULL DEFAULT 0,        -- 오늘(change_date) 변경 횟수
    change_date         DATE,                                  -- 변경 카운트 기준일(KST)
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (device_id, recruit_group)
);
CREATE INDEX idx_actual_device ON actual_applications(device_id);
COMMENT ON TABLE actual_applications IS '군별 실제 지원 대학(1개). 하루 3번까지 변경 가능(change_count/change_date).';
