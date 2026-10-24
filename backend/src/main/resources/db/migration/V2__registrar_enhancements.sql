-- Flyway V2: Review case decision history support
ALTER TABLE review_cases ADD COLUMN decision_history_json TEXT NULL;
