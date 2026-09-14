ALTER TABLE record ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
ALTER TABLE record ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

CREATE FUNCTION set_record_updated_at() RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = clock_timestamp();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_record_updated_at BEFORE UPDATE ON record
FOR EACH ROW EXECUTE FUNCTION set_record_updated_at();

