

CREATE TABLE app.project_details (
	project_id serial4 NOT NULL,
	project_name varchar(255) NOT NULL,
	project_objective text NULL,
	end_date date NULL,
	no_of_weeks varchar(100) NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	created_by varchar(100) NULL,
	is_active bool DEFAULT true NULL,
	modified_at timestamp NULL,
	modified_by varchar(100) NULL,
	status varchar(50) NULL,
	stage varchar(50) NULL,
	message text NULL,
	client_id int4 NULL,
	customer_id int4 NULL,
	modeling_measure varchar(255) NULL,
	is_trade_calculated bool DEFAULT false NULL,
	created_user_id varchar(100) NULL,
	modified_user_id varchar(100) NULL,
	"uuid" uuid DEFAULT gen_random_uuid() NULL,
	display_name varchar(254) DEFAULT ''::character varying NULL,
	version_id int4 DEFAULT 1 NULL,
	is_active_version bool DEFAULT true NULL,
	project_period varchar(255) NULL,
	is_from_base_version bool DEFAULT false NULL,
	start_date date NULL,
	CONSTRAINT project_details_new_pkey PRIMARY KEY (project_id)
);
-- =====================
-- Table: Upload Session
-- =====================

-- =====================
-- Drop Table
-- =====================

--DROP TABLE app.upload_session CASCADE;

-- =====================
-- Create Table
-- =====================
CREATE TABLE IF NOT EXISTS app.upload_session (
    id               serial4 PRIMARY KEY,

    project_id       int8        NOT NULL,
    status           VARCHAR(16)   NOT NULL
        CHECK (status IN ('ACTIVE','COMPLETED','FAILED','CANCELLED')),

    created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
    created_user_id  VARCHAR(100)  NULL,
    created_by       VARCHAR(100)  NOT NULL,

    modified_at      TIMESTAMP     NULL,
    modified_user_id VARCHAR(100)  NULL,
    modified_by      VARCHAR(100)  null,

    CONSTRAINT fk_upload_session_project
      FOREIGN KEY (project_id)
      REFERENCES app.project_details(project_id)
      ON DELETE RESTRICT
);


CREATE INDEX IF NOT EXISTS idx_upload_session_project
    ON app.upload_session (project_id);

CREATE INDEX IF NOT EXISTS idx_upload_session_status
    ON app.upload_session (status);

CREATE UNIQUE INDEX IF NOT EXISTS uq_upload_session_id_project
  ON app.upload_session (id, project_id);


-- =====================
-- Table: File Upload
-- =====================

-- =====================
-- Drop Table
-- =====================

--DROP TABLE app.file_upload CASCADE;

-- =====================
-- Create Table
-- =====================

CREATE TABLE IF NOT EXISTS app.file_upload (
  id serial4 PRIMARY KEY,
  upload_session_id int8 NOT NULL REFERENCES app.upload_session(id) ON DELETE CASCADE,
  project_id int8 NOT NULL,

  original_file_name VARCHAR(255) NOT NULL,
  file_size_bytes BIGINT NOT NULL,
  total_chunks INT NULL,
  uploaded_chunks INT DEFAULT 0 NULL,

  upload_status VARCHAR(16) NOT NULL CHECK (
    upload_status IN ('UPLOADING', 'INGESTED', 'FAILED', 'CANCELLED')
  ),

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL,
  created_user_id VARCHAR(100) NULL,
  created_by VARCHAR(100) NOT NULL,

  modified_at TIMESTAMP NULL,
  modified_user_id VARCHAR(100) NULL,
  modified_by VARCHAR(100) NULL,


  CONSTRAINT fk_file_upload_session
    FOREIGN KEY (upload_session_id)
    REFERENCES app.upload_session(id)
    ON DELETE CASCADE,

  CONSTRAINT fk_file_upload_project
    FOREIGN KEY (project_id)
    REFERENCES app.project_details(project_id)
    ON DELETE RESTRICT,

  CONSTRAINT fk_file_upload_session_project
    FOREIGN KEY (upload_session_id, project_id)
    REFERENCES app.upload_session (id, project_id)
    ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_file_upload_session
  ON app.file_upload (upload_session_id);

CREATE INDEX IF NOT EXISTS idx_file_upload_project
  ON app.file_upload (project_id);

CREATE INDEX IF NOT EXISTS idx_file_upload_project_status
  ON app.file_upload (project_id, upload_status);

CREATE INDEX IF NOT EXISTS idx_file_upload_status
  ON app.file_upload (upload_status);


ALTER TABLE app.file_upload
  ADD CONSTRAINT chk_uploaded_chunks_nonnegative
    CHECK (uploaded_chunks IS NULL OR uploaded_chunks >= 0);

ALTER TABLE app.file_upload
  ADD CONSTRAINT chk_uploaded_chunks_within_total
    CHECK (total_chunks IS NULL OR uploaded_chunks IS NULL OR uploaded_chunks <= total_chunks);

-- =====================
-- Table: Source Column
-- =====================

-- =====================
-- Drop Table
-- =====================

--DROP TABLE app.source_column;

-- =====================
-- Create Table
-- =====================

CREATE TABLE IF NOT EXISTS app.source_column (
  id serial4 PRIMARY KEY,
  file_upload_id int8 NOT NULL REFERENCES app.file_upload(id) ON DELETE CASCADE,

  name_original VARCHAR(255) NOT NULL,
  name_sanitized VARCHAR(255) NOT NULL,
  data_type VARCHAR(50) NULL,

  mapping_status VARCHAR(16) NOT NULL CHECK (
    mapping_status IN ('UNMAPPED', 'MAPPED')
  ),

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL,
  created_user_id VARCHAR(100) NULL,
  created_by VARCHAR(100) NOT NULL,

  modified_at TIMESTAMP NULL,
  modified_user_id VARCHAR(100) NULL,
  modified_by VARCHAR(100) NULL
);

ALTER TABLE app.source_column
  ADD CONSTRAINT uq_source_column_name_sanitized_per_file
  UNIQUE (file_upload_id, name_sanitized);

CREATE INDEX IF NOT EXISTS idx_source_column_file
  ON app.source_column (file_upload_id);

CREATE INDEX IF NOT EXISTS idx_source_column_status
  ON app.source_column (mapping_status);
