-- Non-destructive vendor dimension schema alignment for Source(A).
SET NOCOUNT ON;
GO

IF DB_NAME() <> N'vendor'
    THROW 51000, 'Refusing to migrate a database other than vendor.', 1;

IF OBJECT_ID(N'dbo.cv_emission_source_category', N'U') IS NULL
    THROW 51000, 'Missing dbo.cv_emission_source_category. Run the vendor base schema first.', 1;
GO

IF COL_LENGTH(N'dbo.cv_emission_source_category', N'ghg_scope_category_sort') IS NULL
    ALTER TABLE dbo.cv_emission_source_category ADD ghg_scope_category_sort INT NULL;
IF COL_LENGTH(N'dbo.cv_emission_source_category', N'ghg_scope_en') IS NULL
    ALTER TABLE dbo.cv_emission_source_category ADD ghg_scope_en NVARCHAR(255) NULL;
IF COL_LENGTH(N'dbo.cv_emission_source_category', N'ghg_scope_category_en') IS NULL
    ALTER TABLE dbo.cv_emission_source_category ADD ghg_scope_category_en NVARCHAR(500) NULL;
IF COL_LENGTH(N'dbo.cv_emission_source_category', N'iso_category_description_en') IS NULL
    ALTER TABLE dbo.cv_emission_source_category ADD iso_category_description_en NVARCHAR(1000) NULL;
IF COL_LENGTH(N'dbo.cv_emission_source_category', N'iso_custom_subcategory') IS NULL
    ALTER TABLE dbo.cv_emission_source_category ADD iso_custom_subcategory NVARCHAR(500) NULL;
GO

-- Existing installations used BK_业务键 as category_code. Source(A) defines category_code as SK_排放源分类.
UPDATE dbo.cv_emission_source_category
   SET category_code = CONVERT(NVARCHAR(128), id)
 WHERE remark = N'source(A)'
   AND NULLIF(LTRIM(RTRIM(business_key)), N'') IS NOT NULL;

UPDATE dbo.cv_emission_source_category
   SET ghg_scope_category_sort = COALESCE(ghg_scope_category_sort, TRY_CONVERT(INT, business_key)),
       ghg_scope_en = COALESCE(ghg_scope_en,
           CASE ghg_scope WHEN N'范围1' THEN N'Scope 1' WHEN N'范围2' THEN N'Scope 2' WHEN N'范围3' THEN N'Scope 3' END),
       ghg_scope_category_en = COALESCE(ghg_scope_category_en, category_name_en),
       iso_category_description_en = COALESCE(iso_category_description_en,
           CASE iso_category_description
               WHEN N'直接温室气体排放' THEN N'Direct GHG emissions'
               WHEN N'输入能源的间接温室气体排放' THEN N'Energy indirect GHG emissions'
               WHEN N'运输产生的间接温室气体排放' THEN N'Transportation indirect GHG emissions'
               WHEN N'组织使用的产品或服务产生的间接温室气体排放' THEN N'Indirect GHG emissions from products used by an organization'
               WHEN N'与使用组织产品相关的间接温室气体排放' THEN N'Indirect GHG emissions associated with the use of products from the organization'
               WHEN N'其他间接温室气体排放' THEN N'Other indirect GHG emissions'
           END),
       current_flag = CASE WHEN current_flag IN (N'1', N'Y', N'y', N'是') THEN N'Y' ELSE N'N' END
 WHERE remark = N'source(A)';

UPDATE dbo.cv_emission_source_category
   SET iso_custom_subcategory = CASE business_key
       WHEN N'101' THEN N'1.1 固定燃烧源排放' WHEN N'102' THEN N'1.2 移动燃烧源排放'
       WHEN N'103' THEN N'1.3 工艺过程排放' WHEN N'104' THEN N'1.4 逸散排放'
       WHEN N'105' THEN N'1.5 土地利用与碳清除' WHEN N'201' THEN N'2.1 外购电力排放'
       WHEN N'202' THEN N'2.2 外购蒸汽排放' WHEN N'203' THEN N'2.3 外购热力排放'
       WHEN N'204' THEN N'2.4 外购冷量排放' WHEN N'301' THEN N'4.1 采购的商品和服务'
       WHEN N'302' THEN N'4.2 资本货物' WHEN N'303' THEN N'4.3 燃料和能源相关活动'
       WHEN N'304' THEN N'3.1 上游货物运输和配送' WHEN N'305' THEN N'6.1 废弃物处理处置'
       WHEN N'306' THEN N'3.4 商务旅行' WHEN N'307' THEN N'3.2 员工通勤'
       WHEN N'308' THEN N'6.2 上游租赁资产' WHEN N'309' THEN N'3.3 下游货物运输和配送'
       WHEN N'310' THEN N'5.1 销售产品的加工' WHEN N'311' THEN N'5.2 销售产品的使用'
       WHEN N'312' THEN N'5.3 销售产品的报废处理' WHEN N'313' THEN N'6.3 下游租赁资产'
       WHEN N'314' THEN N'6.4 特许经营' WHEN N'315' THEN N'6.5 投资'
       ELSE iso_custom_subcategory END
 WHERE remark = N'source(A)';
GO

-- Repair only the known legacy 203 collapse (5 Source(A) rows reduced to 2).
IF OBJECT_ID(N'dbo.cv_electricity_factor_version', N'U') IS NOT NULL
   AND (SELECT COUNT_BIG(1) FROM dbo.cv_electricity_factor_version) = 2
   AND EXISTS (SELECT 1 FROM dbo.cv_electricity_factor_version WHERE effective_year = 2023 AND factor_version = N'2022')
   AND EXISTS (SELECT 1 FROM dbo.cv_electricity_factor_version WHERE effective_year = 2025 AND factor_version = N'2023')
BEGIN
    DECLARE @next203Id BIGINT = ISNULL((SELECT MAX(id) FROM dbo.cv_electricity_factor_version), 0);
    INSERT INTO dbo.cv_electricity_factor_version
        (id, factor_version, effective_year, sort_order, status, create_time, update_time, remark)
    SELECT @next203Id + source.row_no, source.factor_version, source.effective_year,
           source.sort_order, N'0', SYSDATETIME(), SYSDATETIME(), N'source(A)'
      FROM (VALUES
          (1, N'2022', 2024, 2),
          (2, N'2023', 2026, 4),
          (3, N'2023', 2027, 5)
      ) source(row_no, factor_version, effective_year, sort_order)
     WHERE NOT EXISTS (
         SELECT 1 FROM dbo.cv_electricity_factor_version target
          WHERE target.factor_version = source.factor_version
            AND target.effective_year = source.effective_year
     );
END;

UPDATE target
   SET sort_order = source.sort_order
  FROM dbo.cv_electricity_factor_version target
  JOIN (VALUES
      (N'2022', 2023, 1), (N'2022', 2024, 2), (N'2023', 2025, 3),
      (N'2023', 2026, 4), (N'2023', 2027, 5)
  ) source(factor_version, effective_year, sort_order)
    ON source.factor_version = target.factor_version
   AND source.effective_year = target.effective_year
 WHERE target.remark = N'source(A)';
GO
