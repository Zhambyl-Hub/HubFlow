-- ============================================================
-- HubFlow — Глобальные роли системы
-- V4__system_roles.sql
-- ============================================================

-- ─────────────────────────────────────────────────────────────
-- 1. ГЛОБАЛЬНАЯ РОЛЬ ПОЛЬЗОВАТЕЛЯ
-- USER    — обычный участник
-- MENTOR  — ментор системы (может быть привязан к когортам)
-- ADMIN   — суперадмин, видит и управляет всем
-- ─────────────────────────────────────────────────────────────
ALTER TABLE users
    ADD COLUMN system_role VARCHAR(20) NOT NULL DEFAULT 'USER'
        CHECK (system_role IN ('USER', 'MENTOR', 'ADMIN'));

CREATE INDEX idx_users_system_role ON users(system_role);

-- ─────────────────────────────────────────────────────────────
-- 2. COHORT_MEMBERSHIPS — оставляем только PARTICIPANT | MENTOR
--    ADMIN больше не хранится здесь (он глобальный)
-- ─────────────────────────────────────────────────────────────
ALTER TABLE cohort_memberships
    DROP CONSTRAINT IF EXISTS cohort_memberships_role_check;

ALTER TABLE cohort_memberships
    ADD CONSTRAINT cohort_memberships_role_check
        CHECK (role IN ('PARTICIPANT', 'MENTOR'));

-- ─────────────────────────────────────────────────────────────
-- 3. MENTOR_PROFILES — уже создана в V3, ничего не меняем
--    Создаётся автоматически при назначении system_role = MENTOR
-- ─────────────────────────────────────────────────────────────

-- ─────────────────────────────────────────────────────────────
-- 4. ИСТОРИЯ СМЕНЫ СИСТЕМНЫХ РОЛЕЙ
-- ─────────────────────────────────────────────────────────────
CREATE TABLE system_role_history (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    old_role    VARCHAR(20),
    new_role    VARCHAR(20) NOT NULL,
    changed_by  UUID        NOT NULL REFERENCES users(id),
    changed_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_system_role_history_user ON system_role_history(user_id);
