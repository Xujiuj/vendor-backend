package org.dromara.carbon.vendor.dimension.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.dimension.domain.bo.CvEmissionSourcePublicationBo;
import org.dromara.carbon.vendor.dimension.domain.vo.CvEmissionSourcePublicationVo;
import org.dromara.carbon.vendor.dimension.domain.vo.CvEmissionSourceVersionVo;
import org.dromara.carbon.vendor.dimension.service.ICvEmissionSourcePublicationService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Stores the vendor-controlled publication boundary used by enterprise sync.
 */
@RequiredArgsConstructor
@Service
public class CvEmissionSourcePublicationServiceImpl implements ICvEmissionSourcePublicationService {

    public static final String MODE_SINGLE = "SINGLE";
    public static final String MODE_ALL = "ALL";
    private static final String DIMENSION_CODE = "emission-source-category";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public CvEmissionSourcePublicationVo queryPolicy() {
        CvEmissionSourcePublicationVo policy = jdbcTemplate.query(
            "SELECT TOP 1 publication_id, publish_mode, version_no, record_count, published_by, published_time "
                + "FROM dbo.cv_dimension_publish_policy WHERE dimension_code = ?",
            (rs, rowNum) -> mapPolicy(rs),
            DIMENSION_CODE
        ).stream().findFirst().orElseGet(this::defaultAllPolicy);
        policy.setPublishedVersions(resolvePublishedVersions(policy));
        return policy;
    }

    @Override
    public List<CvEmissionSourceVersionVo> queryVersions() {
        return jdbcTemplate.query("""
            SELECT COALESCE(NULLIF(LTRIM(RTRIM(version_no)), ''), '1') AS version_no,
                   MAX(effective_date) AS effective_date,
                   COUNT(*) AS record_count
            FROM dbo.cv_emission_source_category
            WHERE status = '0'
            GROUP BY COALESCE(NULLIF(LTRIM(RTRIM(version_no)), ''), '1')
            ORDER BY TRY_CONVERT(DECIMAL(30, 10), COALESCE(NULLIF(LTRIM(RTRIM(version_no)), ''), '1')) DESC,
                     COALESCE(NULLIF(LTRIM(RTRIM(version_no)), ''), '1') DESC
            """, (rs, rowNum) -> {
            CvEmissionSourceVersionVo version = new CvEmissionSourceVersionVo();
            version.setVersionNo(rs.getString("version_no"));
            var effectiveDate = rs.getDate("effective_date");
            version.setEffectiveDate(effectiveDate == null ? null : effectiveDate.toLocalDate());
            version.setRecordCount(rs.getInt("record_count"));
            return version;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CvEmissionSourcePublicationVo publish(CvEmissionSourcePublicationBo bo, String operatedBy) {
        String mode = normalizeMode(bo == null ? null : bo.getPublishMode());
        String versionNo = MODE_SINGLE.equals(mode) ? normalizeVersion(bo.getVersionNo()) : null;
        if (MODE_SINGLE.equals(mode) && !versionExists(versionNo)) {
            throw new ServiceException("The selected 103 version does not contain enabled data");
        }
        int recordCount = countPublishedRows(mode, versionNo);
        Date now = new Date();
        String publicationId = UUID.randomUUID().toString();
        jdbcTemplate.update("""
            MERGE dbo.cv_dimension_publish_policy AS target
            USING (SELECT ? AS dimension_code) AS source
            ON target.dimension_code = source.dimension_code
            WHEN MATCHED THEN UPDATE SET publication_id = ?, publish_mode = ?, version_no = ?, record_count = ?,
                published_by = ?, published_time = ?, update_time = ?
            WHEN NOT MATCHED THEN INSERT (dimension_code, publication_id, publish_mode, version_no, record_count,
                published_by, published_time, create_time, update_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
            """,
            DIMENSION_CODE, publicationId, mode, versionNo, recordCount, operatedBy, now, now,
            DIMENSION_CODE, publicationId, mode, versionNo, recordCount, operatedBy, now, now, now
        );
        jdbcTemplate.update("""
            INSERT INTO dbo.cv_dimension_publish_log (dimension_code, publication_id, publish_mode, version_no,
                record_count, published_by, published_time)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """, DIMENSION_CODE, publicationId, mode, versionNo, recordCount, operatedBy, now);
        return queryPolicy();
    }

    @Override
    public void touchPublication() {
        jdbcTemplate.update("""
            UPDATE p
            SET publication_id = CONVERT(NVARCHAR(36), NEWID()),
                record_count = CASE WHEN publish_mode = 'SINGLE' THEN
                    (SELECT COUNT(*) FROM dbo.cv_emission_source_category
                     WHERE status = '0' AND COALESCE(NULLIF(LTRIM(RTRIM(version_no)), ''), '1') = p.version_no)
                    ELSE (SELECT COUNT(*) FROM dbo.cv_emission_source_category WHERE status = '0') END,
                update_time = SYSDATETIME()
            FROM dbo.cv_dimension_publish_policy p
            WHERE p.dimension_code = ?
            """, DIMENSION_CODE);
    }

    private CvEmissionSourcePublicationVo defaultAllPolicy() {
        CvEmissionSourcePublicationVo policy = new CvEmissionSourcePublicationVo();
        policy.setPublicationId("legacy-all");
        policy.setPublishMode(MODE_ALL);
        policy.setRecordCount(countPublishedRows(MODE_ALL, null));
        return policy;
    }

    private CvEmissionSourcePublicationVo mapPolicy(ResultSet rs) throws SQLException {
        CvEmissionSourcePublicationVo policy = new CvEmissionSourcePublicationVo();
        policy.setPublicationId(rs.getString("publication_id"));
        policy.setPublishMode(rs.getString("publish_mode"));
        policy.setVersionNo(rs.getString("version_no"));
        policy.setRecordCount(rs.getInt("record_count"));
        policy.setPublishedBy(rs.getString("published_by"));
        policy.setPublishedTime(rs.getTimestamp("published_time"));
        return policy;
    }

    private List<String> resolvePublishedVersions(CvEmissionSourcePublicationVo policy) {
        if (MODE_SINGLE.equals(policy.getPublishMode())) {
            return List.of(normalizeVersion(policy.getVersionNo()));
        }
        return queryVersions().stream().map(CvEmissionSourceVersionVo::getVersionNo).toList();
    }

    private boolean versionExists(String versionNo) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM dbo.cv_emission_source_category
            WHERE status = '0' AND COALESCE(NULLIF(LTRIM(RTRIM(version_no)), ''), '1') = ?
            """, Integer.class, versionNo);
        return count != null && count > 0;
    }

    private int countPublishedRows(String mode, String versionNo) {
        Integer count = MODE_SINGLE.equals(mode)
            ? jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM dbo.cv_emission_source_category
                WHERE status = '0' AND COALESCE(NULLIF(LTRIM(RTRIM(version_no)), ''), '1') = ?
                """, Integer.class, versionNo)
            : jdbcTemplate.queryForObject("SELECT COUNT(*) FROM dbo.cv_emission_source_category WHERE status = '0'", Integer.class);
        return count == null ? 0 : count;
    }

    private String normalizeMode(String value) {
        String mode = StringUtils.trimToEmpty(value).toUpperCase();
        if (!MODE_SINGLE.equals(mode) && !MODE_ALL.equals(mode)) {
            throw new ServiceException("publishMode must be SINGLE or ALL");
        }
        return mode;
    }

    private String normalizeVersion(String value) {
        if (StringUtils.isBlank(value)) {
            throw new ServiceException("versionNo is required when publishing one version");
        }
        return value.trim();
    }
}
