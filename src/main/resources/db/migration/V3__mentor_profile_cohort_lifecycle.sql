-- ============================================================
-- HubFlow — Профиль ментора + lifecycle когорты
-- V3__mentor_profile_cohort_lifecycle.sql
-- ============================================================

-- ─────────────────────────────────────────────────────────────
-- 1. ПРОФИЛЬ МЕНТОРА
-- Отдельная таблица, потому что не каждый user является ментором.
-- Один user — один профиль (независимо от числа когорт).
-- ─────────────────────────────────────────────────────────────
CREATE TABLE mentor_profiles (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    bio         TEXT,
    expertise   TEXT,                        -- через запятую: "Product, Growth, Fundraising"
    linkedin_url VARCHAR(500),
    avatar_url  VARCHAR(500),
    is_visible  BOOLEAN      NOT NULL DEFAULT TRUE,   -- показывать ли в каталоге менторов
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_mentor_profiles_user ON mentor_profiles(user_id);
CREATE INDEX idx_mentor_profiles_visible ON mentor_profiles(is_visible);

-- ─────────────────────────────────────────────────────────────
-- 2. ИСТОРИЯ СМЕНЫ РОЛЕЙ
-- Аудит — кто, когда, какую роль дал/забрал
-- ─────────────────────────────────────────────────────────────
CREATE TABLE membership_role_history (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    cohort_id   UUID        NOT NULL REFERENCES cohorts(id) ON DELETE CASCADE,
    old_role    VARCHAR(20),
    new_role    VARCHAR(20) NOT NULL,
    changed_by  UUID        NOT NULL REFERENCES users(id),
    changed_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_role_history_user_cohort ON membership_role_history(user_id, cohort_id);

-- ─────────────────────────────────────────────────────────────
-- 3. LIFECYCLE КОГОРТЫ
-- При смене статуса на COMPLETED/ARCHIVED:
--   - FREE слоты автоматически CANCELLED (триггер)
--   - История и memberships сохраняются
-- ─────────────────────────────────────────────────────────────

-- Функция-триггер: при завершении когорты отменяем все FREE слоты
CREATE OR REPLACE FUNCTION cancel_free_slots_on_cohort_end()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status IN ('COMPLETED', 'ARCHIVED')
       AND OLD.status NOT IN ('COMPLETED', 'ARCHIVED') THEN

        UPDATE mentor_slots
        SET    status = 'CANCELLED'
        WHERE  cohort_id = NEW.id
          AND  status = 'FREE';

    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_cancel_slots_on_cohort_end
AFTER UPDATE OF status ON cohorts
FOR EACH ROW
EXECUTE FUNCTION cancel_free_slots_on_cohort_end();
