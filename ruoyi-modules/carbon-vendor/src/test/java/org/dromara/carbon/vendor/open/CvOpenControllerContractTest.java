package org.dromara.carbon.vendor.open;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.dromara.carbon.vendor.openapi.controller.CvOpenAnnouncementController;
import org.dromara.carbon.vendor.openapi.controller.CvOpenDimensionController;
import org.dromara.carbon.vendor.openapi.controller.CvOpenFactorController;
import org.dromara.carbon.vendor.openapi.controller.CvOpenLicenseController;
import org.dromara.carbon.vendor.openapi.controller.CvOpenReportTemplateController;
import org.dromara.carbon.vendor.openapi.domain.CvOpenAnnouncementListResponse;
import org.dromara.carbon.vendor.openapi.domain.CvOpenAnnouncementRequest;
import org.dromara.carbon.vendor.openapi.domain.CvOpenDimensionListResponse;
import org.dromara.carbon.vendor.openapi.domain.CvOpenDimensionRequest;
import org.dromara.carbon.vendor.openapi.domain.CvOpenFactorSyncRequest;
import org.dromara.carbon.vendor.openapi.domain.CvOpenFactorSyncResponse;
import org.dromara.carbon.vendor.openapi.domain.CvOpenLicenseCurrentRequest;
import org.dromara.carbon.vendor.openapi.domain.CvOpenLicenseCurrentResponse;
import org.dromara.carbon.vendor.openapi.domain.CvOpenRenewalOrderRequest;
import org.dromara.carbon.vendor.openapi.domain.CvOpenRenewalOrderResponse;
import org.dromara.carbon.vendor.openapi.domain.CvOpenReportTemplateDownloadResponse;
import org.dromara.carbon.vendor.openapi.domain.CvOpenReportTemplateListResponse;
import org.dromara.carbon.vendor.openapi.domain.CvOpenReportTemplateRequest;
import org.dromara.carbon.vendor.announcement.service.ICvOpenAnnouncementService;
import org.dromara.carbon.vendor.openapi.service.ICvOpenDimensionService;
import org.dromara.carbon.vendor.openapi.service.ICvOpenFactorService;
import org.dromara.carbon.vendor.openapi.service.ICvOpenLicenseService;
import org.dromara.carbon.vendor.openapi.service.ICvOpenReportTemplateService;
import org.dromara.common.core.domain.R;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class CvOpenControllerContractTest {

    @Test
    void openControllersExposeStableRoutes() throws NoSuchMethodException {
        assertControllerRoute(CvOpenFactorController.class, "/open/factors");
        assertGetRoute(CvOpenFactorController.class.getMethod("sync", CvOpenFactorSyncRequest.class), "");

        assertControllerRoute(CvOpenLicenseController.class, "/open");
        assertPostRoute(CvOpenLicenseController.class.getMethod("current", CvOpenLicenseCurrentRequest.class),
            "/licenses/current");
        assertPostRoute(CvOpenLicenseController.class.getMethod("renewalOrder", CvOpenRenewalOrderRequest.class),
            "/renewal-orders");

        assertControllerRoute(CvOpenReportTemplateController.class, "/open/report-templates");
        assertGetRoute(CvOpenReportTemplateController.class.getMethod("list", CvOpenReportTemplateRequest.class), "");
        assertGetRoute(CvOpenReportTemplateController.class.getMethod(
            "download", Long.class, CvOpenReportTemplateRequest.class), "/{id}/download");
        assertGetRoute(CvOpenReportTemplateController.class.getMethod(
            "consumeDownloadToken", String.class, HttpServletResponse.class), "/download-tokens/{token}");

        assertControllerRoute(CvOpenAnnouncementController.class, "/open/announcements");
        assertGetRoute(CvOpenAnnouncementController.class.getMethod("list", CvOpenAnnouncementRequest.class), "");

        assertControllerRoute(CvOpenDimensionController.class, "/open/dimensions");
        assertGetRoute(CvOpenDimensionController.class.getMethod("list", CvOpenDimensionRequest.class), "");
    }

    @Test
    void openControllerMethodsKeepValidationAndBindingAnnotations() throws NoSuchMethodException {
        assertParameterAnnotation(
            CvOpenFactorController.class.getMethod("sync", CvOpenFactorSyncRequest.class), 0, Valid.class);
        assertParameterAnnotation(
            CvOpenAnnouncementController.class.getMethod("list", CvOpenAnnouncementRequest.class), 0, Valid.class);
        assertParameterAnnotation(
            CvOpenDimensionController.class.getMethod("list", CvOpenDimensionRequest.class), 0, Valid.class);

        Method current = CvOpenLicenseController.class.getMethod("current", CvOpenLicenseCurrentRequest.class);
        assertParameterAnnotation(current, 0, Valid.class);
        assertParameterAnnotation(current, 0, RequestBody.class);

        Method renewalOrder = CvOpenLicenseController.class.getMethod("renewalOrder", CvOpenRenewalOrderRequest.class);
        assertParameterAnnotation(renewalOrder, 0, Valid.class);
        assertParameterAnnotation(renewalOrder, 0, RequestBody.class);

        Method download = CvOpenReportTemplateController.class.getMethod(
            "download", Long.class, CvOpenReportTemplateRequest.class);
        assertParameterAnnotation(download, 0, PathVariable.class);
        assertParameterAnnotation(download, 1, Valid.class);

        Method consumeDownloadToken = CvOpenReportTemplateController.class.getMethod(
            "consumeDownloadToken", String.class, HttpServletResponse.class);
        assertParameterAnnotation(consumeDownloadToken, 0, PathVariable.class);
    }

    @Test
    void openRequestDtosKeepRequiredLicenseScopeFields() throws NoSuchFieldException {
        assertNotBlankFields(CvOpenFactorSyncRequest.class, "installId");
        assertNotBlankFields(CvOpenLicenseCurrentRequest.class, "installId");
        assertNotBlankFields(CvOpenRenewalOrderRequest.class, "installId");
        assertNotBlankFields(CvOpenReportTemplateRequest.class, "installId");
        assertNotBlankFields(CvOpenAnnouncementRequest.class, "installId");
        assertNotBlankFields(CvOpenDimensionRequest.class, "installId", "dimensionCode");
    }

    @Test
    void factorControllerDelegatesAndWrapsResponse() {
        ICvOpenFactorService service = mock(ICvOpenFactorService.class);
        CvOpenFactorController controller = new CvOpenFactorController(service);
        CvOpenFactorSyncRequest request = new CvOpenFactorSyncRequest();
        CvOpenFactorSyncResponse serviceResponse = new CvOpenFactorSyncResponse();
        when(service.syncFactors(request)).thenReturn(serviceResponse);

        R<CvOpenFactorSyncResponse> response = controller.sync(request);

        assertOk(response, serviceResponse);
        verify(service).syncFactors(request);
    }

    @Test
    void licenseControllerDelegatesAndWrapsResponses() {
        ICvOpenLicenseService service = mock(ICvOpenLicenseService.class);
        CvOpenLicenseController controller = new CvOpenLicenseController(service);
        CvOpenLicenseCurrentRequest currentRequest = new CvOpenLicenseCurrentRequest();
        CvOpenLicenseCurrentResponse currentResponse = new CvOpenLicenseCurrentResponse();
        CvOpenRenewalOrderRequest renewalRequest = new CvOpenRenewalOrderRequest();
        CvOpenRenewalOrderResponse renewalResponse = new CvOpenRenewalOrderResponse();
        when(service.currentLicense(currentRequest)).thenReturn(currentResponse);
        when(service.createRenewalOrder(renewalRequest)).thenReturn(renewalResponse);

        assertOk(controller.current(currentRequest), currentResponse);
        assertOk(controller.renewalOrder(renewalRequest), renewalResponse);
        verify(service).currentLicense(currentRequest);
        verify(service).createRenewalOrder(renewalRequest);
    }

    @Test
    void reportTemplateControllerDelegatesAndWrapsResponses() throws Exception {
        ICvOpenReportTemplateService service = mock(ICvOpenReportTemplateService.class);
        CvOpenReportTemplateController controller = new CvOpenReportTemplateController(service);
        CvOpenReportTemplateRequest request = new CvOpenReportTemplateRequest();
        CvOpenReportTemplateListResponse listResponse = new CvOpenReportTemplateListResponse();
        CvOpenReportTemplateDownloadResponse downloadResponse = new CvOpenReportTemplateDownloadResponse();
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        when(service.listTemplates(request)).thenReturn(listResponse);
        when(service.downloadTemplate(7L, request)).thenReturn(downloadResponse);

        assertOk(controller.list(request), listResponse);
        assertOk(controller.download(7L, request), downloadResponse);
        controller.consumeDownloadToken("token-1", servletResponse);

        verify(service).listTemplates(request);
        verify(service).downloadTemplate(7L, request);
        verify(service).consumeDownloadToken("token-1", servletResponse);
    }

    @Test
    void announcementAndDimensionControllersDelegateAndWrapResponses() {
        ICvOpenAnnouncementService announcementService = mock(ICvOpenAnnouncementService.class);
        CvOpenAnnouncementController announcementController = new CvOpenAnnouncementController(announcementService);
        CvOpenAnnouncementRequest announcementRequest = new CvOpenAnnouncementRequest();
        CvOpenAnnouncementListResponse announcementResponse = new CvOpenAnnouncementListResponse();
        when(announcementService.listAnnouncements(announcementRequest)).thenReturn(announcementResponse);

        ICvOpenDimensionService dimensionService = mock(ICvOpenDimensionService.class);
        CvOpenDimensionController dimensionController = new CvOpenDimensionController(dimensionService);
        CvOpenDimensionRequest dimensionRequest = new CvOpenDimensionRequest();
        CvOpenDimensionListResponse dimensionResponse = new CvOpenDimensionListResponse();
        when(dimensionService.listDimensions(dimensionRequest)).thenReturn(dimensionResponse);

        assertOk(announcementController.list(announcementRequest), announcementResponse);
        assertOk(dimensionController.list(dimensionRequest), dimensionResponse);
        verify(announcementService).listAnnouncements(announcementRequest);
        verify(dimensionService).listDimensions(dimensionRequest);
    }

    private static void assertControllerRoute(Class<?> controllerClass, String route) {
        assertNotNull(controllerClass.getAnnotation(RestController.class));
        RequestMapping requestMapping = controllerClass.getAnnotation(RequestMapping.class);
        assertNotNull(requestMapping);
        assertArrayEquals(new String[]{route}, requestMapping.value());
    }

    private static void assertGetRoute(Method method, String route) {
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        assertNotNull(getMapping);
        assertMappingValue(route, getMapping.value());
    }

    private static void assertPostRoute(Method method, String route) {
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        assertNotNull(postMapping);
        assertMappingValue(route, postMapping.value());
    }

    private static void assertMappingValue(String expectedRoute, String[] actualRoutes) {
        if (expectedRoute.isEmpty() && actualRoutes.length == 0) {
            return;
        }
        assertArrayEquals(new String[]{expectedRoute}, actualRoutes);
    }

    private static void assertParameterAnnotation(
        Method method, int parameterIndex, Class<? extends Annotation> annotationClass) {
        assertNotNull(method.getParameters()[parameterIndex].getAnnotation(annotationClass));
    }

    private static void assertNotBlankFields(Class<?> requestClass, String... fieldNames) throws NoSuchFieldException {
        for (String fieldName : fieldNames) {
            Field field = requestClass.getDeclaredField(fieldName);
            assertTrue(field.isAnnotationPresent(NotBlank.class), fieldName + " must be @NotBlank");
        }
    }

    private static <T> void assertOk(R<T> response, T expectedData) {
        assertEquals(R.SUCCESS, response.getCode());
        assertSame(expectedData, response.getData());
    }
}
