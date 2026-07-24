-- Non-destructive vendor 103 publication-policy migration.
-- The policy is authoritative for enterprise synchronization scope.

SET NOCOUNT ON;
GO

IF DB_NAME() <> N'vendor'
    THROW 51000, 'Refusing to apply 103 publication migration outside vendor.', 1;

IF OBJECT_ID(N'dbo.cv_emission_source_category', N'U') IS NULL
    THROW 51000, 'Missing dbo.cv_emission_source_category. Run the vendor base schema first.', 1;

IF OBJECT_ID(N'dbo.cv_dimension_publish_policy', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.cv_dimension_publish_policy (
        dimension_code NVARCHAR(64) NOT NULL PRIMARY KEY,
        publication_id NVARCHAR(36) NOT NULL,
        publish_mode NVARCHAR(16) NOT NULL,
        version_no NVARCHAR(100) NULL,
        record_count INT NOT NULL CONSTRAINT df_cv_dimension_publish_policy_record_count DEFAULT 0,
        published_by NVARCHAR(64) NULL,
        published_time DATETIME2 NULL,
        create_time DATETIME2 NOT NULL CONSTRAINT df_cv_dimension_publish_policy_create_time DEFAULT SYSDATETIME(),
        update_time DATETIME2 NULL,
        CONSTRAINT ck_cv_dimension_publish_policy_mode CHECK (publish_mode IN (N'SINGLE', N'ALL')),
        CONSTRAINT ck_cv_dimension_publish_policy_version CHECK (
            (publish_mode = N'ALL' AND version_no IS NULL) OR (publish_mode = N'SINGLE' AND version_no IS NOT NULL)
        )
    );
END;

IF OBJECT_ID(N'dbo.cv_dimension_publish_log', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.cv_dimension_publish_log (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        dimension_code NVARCHAR(64) NOT NULL,
        publication_id NVARCHAR(36) NOT NULL,
        publish_mode NVARCHAR(16) NOT NULL,
        version_no NVARCHAR(100) NULL,
        record_count INT NOT NULL,
        published_by NVARCHAR(64) NULL,
        published_time DATETIME2 NOT NULL CONSTRAINT df_cv_dimension_publish_log_time DEFAULT SYSDATETIME()
    );
    CREATE INDEX ix_cv_dimension_publish_log_dimension_time
        ON dbo.cv_dimension_publish_log(dimension_code, published_time DESC, id DESC);
END;

IF NOT EXISTS (SELECT 1 FROM dbo.cv_dimension_publish_policy WHERE dimension_code = N'emission-source-category')
BEGIN
    DECLARE @publicationId NVARCHAR(36) = CONVERT(NVARCHAR(36), NEWID());
    DECLARE @publishedTime DATETIME2 = SYSDATETIME();
    DECLARE @recordCount INT = (SELECT COUNT(*) FROM dbo.cv_emission_source_category WHERE status = N'0');

    INSERT INTO dbo.cv_dimension_publish_policy
        (dimension_code, publication_id, publish_mode, version_no, record_count, published_by, published_time, create_time, update_time)
    VALUES
        (N'emission-source-category', @publicationId, N'ALL', NULL, @recordCount, N'vendor-system', @publishedTime, @publishedTime, @publishedTime);

    INSERT INTO dbo.cv_dimension_publish_log
        (dimension_code, publication_id, publish_mode, version_no, record_count, published_by, published_time)
    VALUES
        (N'emission-source-category', @publicationId, N'ALL', NULL, @recordCount, N'vendor-system', @publishedTime);
END;
GO
