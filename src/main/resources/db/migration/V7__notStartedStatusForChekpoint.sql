-- добавление нового статуса по дефолту
ALTER TABLE checkpoint_progress
    DROP CONSTRAINT IF EXISTS checkpoint_progress_status_check;

ALTER TABLE checkpoint_progress
    ADD CONSTRAINT checkpoint_progress_status_check
        CHECK (status IN ('NOT_STARTED', 'PENDING', 'DONE', 'PARTIAL'));