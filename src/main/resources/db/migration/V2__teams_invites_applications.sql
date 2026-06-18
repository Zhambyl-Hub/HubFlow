-- ============================================================
-- HubFlow — Команды независимы от когорт + приглашения + заявки
-- V2__teams_invites_applications.sql
-- ============================================================

-- ─────────────────────────────────────────────────────────────
-- 1. ОТДЕЛЯЕМ TEAMS ОТ COHORTS
-- ─────────────────────────────────────────────────────────────

-- Убираем FK на cohort и поля заявки — теперь команда существует независимо
ALTER TABLE teams DROP CONSTRAINT IF EXISTS teams_cohort_id_fkey;
ALTER TABLE teams DROP COLUMN IF EXISTS cohort_id;
ALTER TABLE teams DROP COLUMN IF EXISTS application_status;
ALTER TABLE teams DROP COLUMN IF EXISTS applied_at;
ALTER TABLE teams DROP COLUMN IF EXISTS approved_at;
ALTER TABLE teams DROP COLUMN IF EXISTS reviewed_by;

-- Добавляем created_by и created_at — кто создал команду
ALTER TABLE teams ADD COLUMN created_by UUID REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE teams ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

-- ─────────────────────────────────────────────────────────────
-- 2. ПРИГЛАШЕНИЯ В КОМАНДУ
-- ─────────────────────────────────────────────────────────────

CREATE TABLE team_invites (
                              id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                              team_id         UUID        NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
                              invited_user_id UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                              invited_by      UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                              status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                                  CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED')),
                              created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                              responded_at    TIMESTAMPTZ,
                              UNIQUE (team_id, invited_user_id)
);

CREATE INDEX idx_team_invites_user   ON team_invites(invited_user_id);
CREATE INDEX idx_team_invites_team   ON team_invites(team_id);
CREATE INDEX idx_team_invites_status ON team_invites(status);

-- ─────────────────────────────────────────────────────────────
-- 3. ЗАЯВКИ КОМАНД В КОГОРТЫ
-- ─────────────────────────────────────────────────────────────

CREATE TABLE cohort_applications (
                                     id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                     cohort_id   UUID        NOT NULL REFERENCES cohorts(id) ON DELETE CASCADE,
                                     team_id     UUID        NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
                                     status      VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                                         CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
                                     applied_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                     reviewed_at TIMESTAMPTZ,
                                     reviewed_by UUID        REFERENCES users(id) ON DELETE SET NULL,
                                     UNIQUE (cohort_id, team_id)
);

CREATE INDEX idx_applications_cohort ON cohort_applications(cohort_id);
CREATE INDEX idx_applications_team   ON cohort_applications(team_id);
CREATE INDEX idx_applications_status ON cohort_applications(status);

-- ─────────────────────────────────────────────────────────────
-- 4. ОБНОВЛЯЕМ ЗАВИСИМЫЕ ТАБЛИЦЫ
-- ─────────────────────────────────────────────────────────────
-- accountability_pairs, demo_day_participants, mentor_bookings,
-- checkpoint_progress, peer_reviews, votes — теперь привязаны
-- к команде через cohort_applications (team может быть в когорте
-- только если есть APPROVED заявка). FK на teams сохраняются как есть,
-- логика проверки "команда в когорте" переносится в сервисный слой:
-- EXISTS (SELECT 1 FROM cohort_applications
--         WHERE team_id = ? AND cohort_id = ? AND status = 'APPROVED')

-- Индекс ускоряет эту проверку
CREATE INDEX idx_applications_team_cohort_approved
    ON cohort_applications(team_id, cohort_id)
    WHERE status = 'APPROVED';