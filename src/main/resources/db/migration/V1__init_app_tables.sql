--===============================================
-- app.source_column definition
--===============================================

-- Drop table

-- DROP TABLE app.source_column;

CREATE TABLE app.source_column (
    id serial4 NOT NULL,
    file_upload_id int8 NOT NULL,
    ordinal int4 DEFAULT 0 NOT NULL,
    name_original varchar(255) NOT NULL,
    name_sanitized varchar(255) NOT NULL,
    data_type varchar(50) NULL,
    mapping_status varchar(16) NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
    created_user_id varchar(100) NULL,
    created_by varchar(100) NOT NULL,
    modified_at timestamp NULL,
    modified_user_id varchar(100) NULL,
    modified_by varchar(100) NULL,
    CONSTRAINT source_column_mapping_status_check CHECK (((mapping_status)::text = ANY ((ARRAY['UNMAPPED'::character varying, 'MAPPED'::character varying])::text[]))),
    CONSTRAINT source_column_pkey PRIMARY KEY (id),
    CONSTRAINT uq_source_column_name_sanitized_per_file UNIQUE (file_upload_id, name_sanitized)
);
CREATE INDEX idx_source_column_file ON app.source_column USING btree (file_upload_id);
CREATE INDEX idx_source_column_ordinal ON app.source_column USING btree (file_upload_id, ordinal);
CREATE INDEX idx_source_column_status ON app.source_column USING btree (mapping_status);


-- app.source_column foreign keys

ALTER TABLE app.source_column ADD CONSTRAINT source_column_file_upload_id_fkey FOREIGN KEY (file_upload_id) REFERENCES app.file_upload(id) ON DELETE CASCADE;


--===============================================
-- app.file_upload definition
--===============================================

-- Drop table

-- DROP TABLE app.file_upload;

CREATE TABLE app.file_upload (
    id serial4 NOT NULL,
    project_id int8 NOT NULL,
    original_file_name varchar(255) NOT NULL,
    mime_type varchar(100) NULL,
    file_size_bytes int8 NULL,
    bytes_received int8 DEFAULT 0 NOT NULL,
    upload_etag varchar(64) NULL,
    checksum_sha256 varchar(64) NULL,
    checksum_source_sha256 varchar(64) NULL,
    upload_status varchar(16) NOT NULL,
    csv_path varchar(1024) NULL,
    monet_table_name varchar(128) NULL,
    row_count int8 DEFAULT 0 NOT NULL,
    ingest_status varchar(16) NULL,
    ingested_at timestamp NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
    created_user_id varchar(100) NULL,
    created_by varchar(100) NOT NULL,
    modified_at timestamp NULL,
    modified_user_id varchar(100) NULL,
    modified_by varchar(100) NULL,
    driver_details_id int8 NOT NULL,
    CONSTRAINT chk_bytes_nonnegative CHECK ((bytes_received >= 0)),
    CONSTRAINT chk_bytes_within_total CHECK (((file_size_bytes IS NULL) OR (bytes_received <= file_size_bytes))),
    CONSTRAINT file_upload_ingest_status_check CHECK (((ingest_status)::text = ANY ((ARRAY['PENDING'::character varying, 'INGESTING'::character varying, 'INGESTED'::character varying, 'FAILED'::character varying])::text[]))),
    CONSTRAINT file_upload_pkey PRIMARY KEY (id),
    CONSTRAINT file_upload_upload_status_check CHECK (((upload_status)::text = ANY ((ARRAY['UPLOADING'::character varying, 'FAILED'::character varying, 'CANCELLED'::character varying, 'COMPLETED'::character varying])::text[]))),
    CONSTRAINT fk_file_mapping_status_driver_details FOREIGN KEY (driver_details_id) REFERENCES meta.driver_details(id),
    CONSTRAINT fk_file_upload_project FOREIGN KEY (project_id) REFERENCES app.project_details(project_id) ON DELETE RESTRICT
);
CREATE INDEX idx_file_upload_project ON app.file_upload USING btree (project_id);
CREATE INDEX idx_file_upload_project_status ON app.file_upload USING btree (project_id, upload_status);
CREATE INDEX idx_file_upload_status ON app.file_upload USING btree (upload_status);

--===============================================
-- app.file_mapping_status definition
--===============================================

-- Drop table

-- DROP TABLE app.file_mapping_status;

CREATE TABLE app.file_mapping_status (
    id serial4 NOT NULL,
    file_upload_id int8 NOT NULL,
    total_required_count int4 DEFAULT 0 NOT NULL,
    required_mapped_count int4 DEFAULT 0 NOT NULL,
    total_mapped_count int4 DEFAULT 0 NOT NULL,
    is_marked_as_mapped bool DEFAULT false NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
    created_user_id varchar(100) NULL,
    created_by varchar(100) NOT NULL,
    modified_at timestamp NULL,
    modified_user_id varchar(100) NULL,
    modified_by varchar(100) NULL,
    CONSTRAINT file_mapping_status_pkey PRIMARY KEY (id),
    CONSTRAINT file_mapping_status_file_upload_id_fkey FOREIGN KEY (file_upload_id) REFERENCES app.file_upload(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX uq_file_mapping_status_file ON app.file_mapping_status USING btree (file_upload_id);

--===============================================
-- meta.driver_iterative_column_map definition
--===============================================

CREATE TABLE meta.driver_iterative_column_map (
    id serial4 NOT NULL,
    driver_id int4 NOT NULL,
    column_name varchar(255) NOT NULL,
    column_datatype varchar(255) NOT NULL,
    time_format varchar(255) NULL,
    is_mandatory bool DEFAULT true NOT NULL,
    is_active bool DEFAULT true NOT NULL,
    column_value varchar(255) NULL,
    sort_order int4 NULL,
    tooltip text NULL,
    CONSTRAINT driver_iterative_column_map_pkey PRIMARY KEY (id),
    CONSTRAINT fk_driver FOREIGN KEY (driver_id) REFERENCES meta.driver_details(id)
);

--===============================================
-- meta.driver_details definition
--===============================================

CREATE TABLE meta.driver_details (
    id serial4 NOT NULL,
    driver_type_id int4 NULL,
    driver_name varchar(255) NOT NULL,
    is_active bool NOT NULL,
    status varchar(50) DEFAULT 'Not Started'::character varying NULL,
    custom_name varchar(255) NULL,
    is_hidden bool DEFAULT false NULL,
    relevance_order int4 NULL,
    CONSTRAINT driver_details_pkey PRIMARY KEY (id),
    CONSTRAINT driver_details_driver_type_id_fkey FOREIGN KEY (driver_type_id) REFERENCES meta.driver_type(id) ON DELETE CASCADE
);
