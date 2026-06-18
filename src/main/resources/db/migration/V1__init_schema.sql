-- ============================================================
-- HubFlow — начальная схема БД
-- V1__init_schema.sql
-- ============================================================

-- Расширение для UUID
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ─────────────────────────────────────────────────────────────
-- ПОЛЬЗОВАТЕЛИ
-- ─────────────────────────────────────────────────────────────
CREATE TABLE users (
                       id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                       email         VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       first_name    VARCHAR(100) NOT NULL,
                       last_name     VARCHAR(100) NOT NULL,
                       phone         VARCHAR(20),
                       avatar_url    VARCHAR(500),
                       is_active     BOOLEAN     NOT NULL DEFAULT TRUE,
                       created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─────────────────────────────────────────────────────────────
-- КОГОРТЫ
-- ─────────────────────────────────────────────────────────────
CREATE TABLE cohorts (
                         id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                         title             VARCHAR(255) NOT NULL,
                         description       TEXT,
                         start_date        DATE         NOT NULL,
                         end_date          DATE         NOT NULL,
                         total_weeks       INT          NOT NULL,
                         format            VARCHAR(20)  NOT NULL DEFAULT 'ONLINE'
                             CHECK (format IN ('ONLINE', 'OFFLINE', 'HYBRID')),
                         status            VARCHAR(20)  NOT NULL DEFAULT 'DRAFT'
                             CHECK (status IN ('DRAFT', 'ACTIVE', 'DEMO_DAY', 'COMPLETED', 'ARCHIVED')),
                         registration_open BOOLEAN      NOT NULL DEFAULT FALSE,
                         created_by        UUID         REFERENCES users(id) ON DELETE SET NULL,
                         created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                         updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE cohort_memberships (
                                    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                    user_id    UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                    cohort_id  UUID        NOT NULL REFERENCES cohorts(id) ON DELETE CASCADE,
                                    role       VARCHAR(20) NOT NULL
                                        CHECK (role IN ('ADMIN', 'MENTOR', 'PARTICIPANT')),
                                    joined_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                    UNIQUE (user_id, cohort_id)
);

-- ─────────────────────────────────────────────────────────────
-- ПРОГРАММА: НЕДЕЛИ И ЧЕКПОИНТЫ
-- ─────────────────────────────────────────────────────────────
CREATE TABLE weeks (
                       id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                       cohort_id   UUID         NOT NULL REFERENCES cohorts(id) ON DELETE CASCADE,
                       week_number INT          NOT NULL,
                       title       VARCHAR(255) NOT NULL,
                       goal        TEXT,
                       start_date  DATE,
                       end_date    DATE,
                       UNIQUE (cohort_id, week_number)
);

CREATE TABLE checkpoints (
                             id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                             week_id     UUID         NOT NULL REFERENCES weeks(id) ON DELETE CASCADE,
                             title       VARCHAR(255) NOT NULL,
                             description TEXT,
                             is_required BOOLEAN      NOT NULL DEFAULT TRUE,
                             order_index INT          NOT NULL
);

-- ─────────────────────────────────────────────────────────────
-- КОМАНДЫ
-- ─────────────────────────────────────────────────────────────
CREATE TABLE teams (
                       id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                       cohort_id          UUID         NOT NULL REFERENCES cohorts(id) ON DELETE CASCADE,
                       name               VARCHAR(255) NOT NULL,
                       idea_description   TEXT,
                       problem            TEXT,
                       target_segment     VARCHAR(255),
                       solution           TEXT,
                       stage              VARCHAR(50)  CHECK (stage IN ('IDEA', 'MVP', 'REVENUE')),
                       repo_url           VARCHAR(500),
                       landing_url        VARCHAR(500),
                       pitch_url          VARCHAR(500),
                       application_status VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                           CHECK (application_status IN ('PENDING', 'APPROVED', 'REJECTED', 'REVISION')),
                       applied_at         TIMESTAMPTZ,
                       approved_at        TIMESTAMPTZ,
                       reviewed_by        UUID         REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE team_members (
                              id        UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                              team_id   UUID        NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
                              user_id   UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                              role      VARCHAR(20) NOT NULL CHECK (role IN ('LEAD', 'MEMBER')),
                              joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                              UNIQUE (team_id, user_id)
);

-- ─────────────────────────────────────────────────────────────
-- ПРОГРЕСС
-- ─────────────────────────────────────────────────────────────
CREATE TABLE checkpoint_progress (
                                     id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                     checkpoint_id   UUID         NOT NULL REFERENCES checkpoints(id) ON DELETE CASCADE,
                                     team_id         UUID         NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
                                     status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                                         CHECK (status IN ('PENDING', 'DONE', 'PARTIAL')),
                                     proof_url       VARCHAR(500),
                                     proof_file_path VARCHAR(500),
                                     comment         TEXT,
                                     completed_by    UUID         REFERENCES users(id) ON DELETE SET NULL,
                                     completed_at    TIMESTAMPTZ,
                                     UNIQUE (checkpoint_id, team_id)
);

-- ─────────────────────────────────────────────────────────────
-- PEER-ACCOUNTABILITY
-- ─────────────────────────────────────────────────────────────
CREATE TABLE accountability_pairs (
                                      id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                      cohort_id  UUID        NOT NULL REFERENCES cohorts(id) ON DELETE CASCADE,
                                      team_a_id  UUID        NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
                                      team_b_id  UUID        NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
                                      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                      UNIQUE (cohort_id, team_a_id, team_b_id)
);

CREATE TABLE peer_reviews (
                              id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                              accountability_pair_id  UUID        NOT NULL REFERENCES accountability_pairs(id) ON DELETE CASCADE,
                              reviewer_team_id        UUID        NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
                              reviewed_team_id        UUID        NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
                              checkpoint_id           UUID        NOT NULL REFERENCES checkpoints(id) ON DELETE CASCADE,
                              status                  VARCHAR(20) CHECK (status IN ('DONE', 'PARTIAL', 'NOT_DONE')),
                              comment                 TEXT,
                              reviewed_at             TIMESTAMPTZ
);

-- ─────────────────────────────────────────────────────────────
-- МЕНТОРСТВО
-- ─────────────────────────────────────────────────────────────
CREATE TABLE mentor_slots (
                              id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                              mentor_id        UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                              cohort_id        UUID        NOT NULL REFERENCES cohorts(id) ON DELETE CASCADE,
                              slot_start       TIMESTAMPTZ NOT NULL,
                              duration_minutes INT         NOT NULL CHECK (duration_minutes > 0),
                              status           VARCHAR(20) NOT NULL DEFAULT 'FREE'
                                  CHECK (status IN ('FREE', 'BOOKED', 'CANCELLED'))
);

CREATE TABLE mentor_bookings (
                                 id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                 mentor_slot_id UUID        NOT NULL REFERENCES mentor_slots(id) ON DELETE CASCADE,
                                 team_id        UUID        NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
                                 notes          TEXT,
                                 booked_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                 status         VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                                     CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED'))
);

CREATE TABLE mentor_feedback (
                                 id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                 booking_id      UUID        NOT NULL UNIQUE REFERENCES mentor_bookings(id) ON DELETE CASCADE,
                                 mentor_id       UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                 team_id         UUID        NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
                                 content         TEXT        NOT NULL,
                                 readiness_score INT         CHECK (readiness_score BETWEEN 1 AND 5),
                                 created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─────────────────────────────────────────────────────────────
-- DEMO DAY
-- ─────────────────────────────────────────────────────────────
CREATE TABLE demo_days (
                           id                     UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                           cohort_id              UUID        NOT NULL UNIQUE REFERENCES cohorts(id) ON DELETE CASCADE,
                           event_date             TIMESTAMPTZ NOT NULL,
                           description            TEXT,
                           voting_status          VARCHAR(20) NOT NULL DEFAULT 'CLOSED'
                               CHECK (voting_status IN ('CLOSED', 'OPEN', 'FINISHED')),
                           show_results_publicly  BOOLEAN     NOT NULL DEFAULT FALSE,
                           voting_opens_at        TIMESTAMPTZ,
                           voting_closes_at       TIMESTAMPTZ
);

CREATE TABLE demo_criteria (
                               id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                               demo_day_id UUID         NOT NULL REFERENCES demo_days(id) ON DELETE CASCADE,
                               title       VARCHAR(255) NOT NULL,
                               description TEXT,
                               max_score   INT          NOT NULL DEFAULT 10 CHECK (max_score > 0),
                               order_index INT          NOT NULL
);

CREATE TABLE demo_day_participants (
                                       id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                       demo_day_id        UUID         NOT NULL REFERENCES demo_days(id) ON DELETE CASCADE,
                                       team_id            UUID         NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
                                       presentation_order INT,
                                       pitch_deck_url     VARCHAR(500),
                                       video_url          VARCHAR(500),
                                       UNIQUE (demo_day_id, team_id)
);

CREATE TABLE votes (
                       id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                       demo_day_id  UUID        NOT NULL REFERENCES demo_days(id) ON DELETE CASCADE,
                       voter_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                       team_id      UUID        NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
                       criterion_id UUID        NOT NULL REFERENCES demo_criteria(id) ON DELETE CASCADE,
                       score        INT         NOT NULL CHECK (score > 0),
                       voted_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       UNIQUE (demo_day_id, voter_id, team_id, criterion_id)
);

CREATE TABLE guest_tokens (
                              id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                              demo_day_id  UUID         NOT NULL REFERENCES demo_days(id) ON DELETE CASCADE,
                              token        VARCHAR(128) NOT NULL UNIQUE,
                              guest_name   VARCHAR(255),
                              guest_email  VARCHAR(255),
                              is_used      BOOLEAN      NOT NULL DEFAULT FALSE,
                              expires_at   TIMESTAMPTZ,
                              created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ─────────────────────────────────────────────────────────────
-- УВЕДОМЛЕНИЯ
-- ─────────────────────────────────────────────────────────────
CREATE TABLE notifications (
                               id        UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                               user_id   UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               type      VARCHAR(50)  NOT NULL,
                               title     VARCHAR(255) NOT NULL,
                               body      TEXT,
                               metadata  JSONB,
                               is_read   BOOLEAN      NOT NULL DEFAULT FALSE,
                               sent_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                               read_at   TIMESTAMPTZ
);

-- ─────────────────────────────────────────────────────────────
-- ИНДЕКСЫ
-- ─────────────────────────────────────────────────────────────

-- cohort_memberships
CREATE INDEX idx_memberships_user     ON cohort_memberships(user_id);
CREATE INDEX idx_memberships_cohort   ON cohort_memberships(cohort_id);

-- weeks & checkpoints
CREATE INDEX idx_weeks_cohort         ON weeks(cohort_id);
CREATE INDEX idx_checkpoints_week     ON checkpoints(week_id);

-- teams
CREATE INDEX idx_teams_cohort         ON teams(cohort_id);
CREATE INDEX idx_teams_status         ON teams(application_status);

-- team_members
CREATE INDEX idx_team_members_team    ON team_members(team_id);
CREATE INDEX idx_team_members_user    ON team_members(user_id);

-- checkpoint_progress
CREATE INDEX idx_progress_team        ON checkpoint_progress(team_id);
CREATE INDEX idx_progress_checkpoint  ON checkpoint_progress(checkpoint_id);

-- accountability
CREATE INDEX idx_pairs_cohort         ON accountability_pairs(cohort_id);

-- mentor
CREATE INDEX idx_slots_mentor         ON mentor_slots(mentor_id);
CREATE INDEX idx_slots_cohort         ON mentor_slots(cohort_id);
CREATE INDEX idx_slots_status         ON mentor_slots(status);
CREATE INDEX idx_bookings_team        ON mentor_bookings(team_id);

-- demo day
CREATE INDEX idx_votes_demo_day       ON votes(demo_day_id);
CREATE INDEX idx_votes_team           ON votes(team_id);
CREATE INDEX idx_guest_tokens_token   ON guest_tokens(token);

-- notifications
CREATE INDEX idx_notif_user           ON notifications(user_id);
CREATE INDEX idx_notif_is_read        ON notifications(user_id, is_read);
