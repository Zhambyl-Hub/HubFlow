ALTER TABLE mentor_bookings
    DROP CONSTRAINT mentor_bookings_status_check;

ALTER TABLE mentor_bookings
    ADD CONSTRAINT mentor_bookings_status_check
        CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED'));