package org.dromara.carbon.vendor.service;

import org.dromara.carbon.vendor.domain.vo.CvReportTemplateUploadVo;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Stores report template files in a vendor-owned local directory.
 */
@Slf4j
@Service
public class CvReportTemplateFileStorageService {

    private static final String DEFAULT_TEMPLATE_ROOT = "vendor/report-templates";
    private static final String VENDOR_URI_PREFIX = "vendor://";
    private static final String UPLOAD_DIR = "uploaded";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pbix", "pbit", "xlsx", "xlsm", "xls", "pdf");
    private static final int MAX_SAFE_NAME_LENGTH = 120;

    @Value("${carbon.vendor.report-template-root:" + DEFAULT_TEMPLATE_ROOT + "}")
    private String reportTemplateRoot = DEFAULT_TEMPLATE_ROOT;

    public CvReportTemplateUploadVo store(MultipartFile file) {
        validateFile(file);
        String safeFileName = safeFileName(file.getOriginalFilename());
        String relativePath = UPLOAD_DIR + "/" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
            + "/" + UUID.randomUUID().toString().replace("-", "") + "-" + safeFileName;

        Path root = resolveTemplateRoot();
        Path target = root.resolve(relativePath).normalize();
        if (!target.startsWith(root)) {
            throw new ServiceException("report template upload path is outside template root");
        }

        try {
            Files.createDirectories(target.getParent());
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            log.warn("Failed to store vendor report template file. root={}, targetParent={}, fileName={}",
                root, target.getParent(), safeFileName, ex);
            throw new ServiceException("failed to store report template file");
        }

        CvReportTemplateUploadVo vo = new CvReportTemplateUploadVo();
        vo.setFileName(safeFileName);
        vo.setFileUri(VENDOR_URI_PREFIX + relativePath);
        vo.setSize(file.getSize());
        return vo;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("report template file cannot be empty");
        }
    }

    private String safeFileName(String originalFilename) {
        if (StringUtils.isBlank(originalFilename)) {
            throw new ServiceException("report template file name cannot be blank");
        }
        String baseName = originalFilename.trim().replace('\\', '/');
        int slashIndex = baseName.lastIndexOf('/');
        if (slashIndex >= 0) {
            baseName = baseName.substring(slashIndex + 1);
        }
        String normalizedName = baseName.replaceAll("[^A-Za-z0-9._ -]", "_").trim();
        if (StringUtils.isBlank(normalizedName)) {
            throw new ServiceException("report template file name cannot be blank");
        }
        int dotIndex = normalizedName.lastIndexOf('.');
        if (dotIndex < 1 || dotIndex == normalizedName.length() - 1) {
            throw new ServiceException("report template file extension is required");
        }
        String extension = normalizedName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ServiceException("report template file extension is not supported");
        }
        if (normalizedName.length() > MAX_SAFE_NAME_LENGTH) {
            String stem = normalizedName.substring(0, dotIndex);
            String suffix = normalizedName.substring(dotIndex);
            int stemLength = Math.max(1, MAX_SAFE_NAME_LENGTH - suffix.length());
            normalizedName = stem.substring(0, Math.min(stem.length(), stemLength)) + suffix;
        }
        return normalizedName;
    }

    private Path resolveTemplateRoot() {
        if (StringUtils.isBlank(reportTemplateRoot)) {
            throw new ServiceException("report template root is invalid");
        }
        try {
            Path configuredRoot = Path.of(reportTemplateRoot.trim());
            Path root = configuredRoot.isAbsolute()
                ? configuredRoot.normalize()
                : Path.of("").toAbsolutePath().normalize().resolve(configuredRoot).normalize();
            Files.createDirectories(root);
            if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) {
                throw new ServiceException("report template root is not a directory");
            }
            return root;
        } catch (InvalidPathException | IOException ex) {
            log.warn("Invalid vendor report template root. configuredRoot={}", reportTemplateRoot, ex);
            throw new ServiceException("report template root is invalid");
        }
    }
}
