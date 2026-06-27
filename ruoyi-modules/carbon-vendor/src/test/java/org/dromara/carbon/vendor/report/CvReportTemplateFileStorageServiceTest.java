package org.dromara.carbon.vendor.report;

import org.dromara.carbon.vendor.template.domain.vo.CvReportTemplateUploadVo;
import org.dromara.carbon.vendor.template.service.CvReportTemplateFileStorageService;
import org.dromara.common.core.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("dev")
class CvReportTemplateFileStorageServiceTest {

    private CvReportTemplateFileStorageService service;

    @TempDir
    private Path templateRoot;

    @BeforeEach
    void setUp() throws Exception {
        service = new CvReportTemplateFileStorageService();
        Field field = CvReportTemplateFileStorageService.class.getDeclaredField("reportTemplateRoot");
        field.setAccessible(true);
        field.set(service, templateRoot.toString());
    }

    @Test
    void storesSupportedTemplateFileUnderVendorRoot() throws Exception {
        byte[] content = "pbix-content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "../Carbon Report 2026.pbix",
            "application/octet-stream",
            content
        );

        CvReportTemplateUploadVo upload = service.store(file);

        assertEquals("Carbon Report 2026.pbix", upload.getFileName());
        assertEquals(content.length, upload.getSize());
        assertTrue(upload.getFileUri().startsWith("vendor://uploaded/"));
        assertTrue(upload.getFileUri().endsWith("-Carbon Report 2026.pbix"));

        Path storedFile = templateRoot.resolve(upload.getFileUri().substring("vendor://".length())).normalize();
        assertTrue(storedFile.startsWith(templateRoot));
        assertTrue(Files.isRegularFile(storedFile));
        assertArrayEquals(content, Files.readAllBytes(storedFile));
    }

    @Test
    void storesFileWhenRootIsConfiguredAsAbsolutePath() throws Exception {
        Path absoluteRoot = templateRoot.resolve("absolute-root").toAbsolutePath().normalize();
        Field field = CvReportTemplateFileStorageService.class.getDeclaredField("reportTemplateRoot");
        field.setAccessible(true);
        field.set(service, absoluteRoot.toString());
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "acceptance-template.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "xlsx-content".getBytes()
        );

        CvReportTemplateUploadVo upload = service.store(file);

        Path storedFile = absoluteRoot.resolve(upload.getFileUri().substring("vendor://".length())).normalize();
        assertTrue(storedFile.startsWith(absoluteRoot));
        assertTrue(Files.isRegularFile(storedFile));
    }

    @Test
    void rejectsEmptyTemplateFile() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.pbix",
            "application/octet-stream",
            new byte[0]
        );

        ServiceException exception = assertThrows(ServiceException.class, () -> service.store(file));

        assertEquals("report template file cannot be empty", exception.getMessage());
    }

    @Test
    void rejectsUnsupportedFileExtension() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "script.exe",
            "application/octet-stream",
            "bad".getBytes()
        );

        ServiceException exception = assertThrows(ServiceException.class, () -> service.store(file));

        assertEquals("report template file extension is not supported", exception.getMessage());
    }

    @Test
    void normalizesUnsafeCharactersInFileName() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "carbon:report?.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "xlsx".getBytes()
        );

        CvReportTemplateUploadVo upload = service.store(file);

        assertEquals("carbon_report_.xlsx", upload.getFileName());
        assertTrue(upload.getFileUri().endsWith("-carbon_report_.xlsx"));
    }
}
