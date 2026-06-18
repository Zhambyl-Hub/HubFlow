-- ============================================================
-- HubFlow — Demo Day: жюри и лайки аудитории
-- V7__demo_day_jury_and_likes.sql
-- ============================================================

-- ─────────────────────────────────────────────────────────────
-- 1. Добавляем JURY в system_role
--    VARCHAR + CHECK — просто пересоздаём constraint
-- ─────────────────────────────────────────────────────────────
ALTER TABLE users
    DROP CONSTRAINT IF EXISTS users_system_role_check;

ALTER TABLE users
    ADD CONSTRAINT users_system_role_check
        CHECK (system_role IN ('USER', 'MENTOR', 'ADMIN', 'JURY'));

-- ─────────────────────────────────────────────────────────────
-- 2. Таблица приглашений жюри
-- ─────────────────────────────────────────────────────────────
CREATE TABLE jury_invites (
                              id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                              demo_day_id   UUID        NOT NULL REFERENCES demo_days(id) ON DELETE CASCADE,
                              name          VARCHAR(255) NOT NULL,
                              email         VARCHAR(255),
                              invite_token  VARCHAR(128) NOT NULL UNIQUE,
                              status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                                  CHECK (status IN ('PENDING', 'ACTIVATED', 'REVOKED')),
                              jury_user_id  UUID         REFERENCES users(id),
                              expires_at    TIMESTAMPTZ  NOT NULL,
                              activated_at  TIMESTAMPTZ,
                              created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_jury_invites_token    ON jury_invites(invite_token);
CREATE INDEX idx_jury_invites_demo_day ON jury_invites(demo_day_id);

-- ─────────────────────────────────────────────────────────────
-- 3. Голоса жюри (отдельная таблица от votes)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE jury_votes (
                            id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                            demo_day_id  UUID        NOT NULL REFERENCES demo_days(id) ON DELETE CASCADE,
                            jury_id      UUID        NOT NULL REFERENCES users(id),
                            team_id      UUID        NOT NULL REFERENCES teams(id),
                            criterion_id UUID        NOT NULL REFERENCES demo_criteria(id) ON DELETE CASCADE,
                            score        INT         NOT NULL CHECK (score >= 1),
                            voted_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                            UNIQUE (demo_day_id, jury_id, team_id, criterion_id)
);

CREATE INDEX idx_jury_votes_demo_day ON jury_votes(demo_day_id);
CREATE INDEX idx_jury_votes_jury_id  ON jury_votes(jury_id);
CREATE INDEX idx_jury_votes_team_id  ON jury_votes(team_id);

-- ─────────────────────────────────────────────────────────────
-- 4. Лайки аудитории
-- ─────────────────────────────────────────────────────────────
CREATE TABLE audience_likes (
                                id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                demo_day_id UUID        NOT NULL REFERENCES demo_days(id) ON DELETE CASCADE,
                                user_id     UUID        NOT NULL REFERENCES users(id),
                                team_id     UUID        NOT NULL REFERENCES teams(id),
                                liked_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                UNIQUE (demo_day_id, user_id, team_id)
);

CREATE INDEX idx_audience_likes_demo_day ON audience_likes(demo_day_id);
CREATE INDEX idx_audience_likes_team_id  ON audience_likes(team_id);