package org.dromara.carbon.vendor.license;

import org.dromara.carbon.vendor.license.controller.CvLicenseIssueController;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssueRequest;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssueResult;
import org.dromara.carbon.vendor.license.domain.CvLicenseReissueRequest;
import org.dromara.carbon.vendor.license.service.ICvLicenseIssueService;
import org.dromara.common.core.domain.R;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class CvLicenseIssueControllerTest {

    @Test
    void issueOverridesRequestOperatorWithServerSideOperatorAndReturnsLicenseResult() {
        ICvLicenseIssueService service = mock(ICvLicenseIssueService.class);
        CvLicenseIssueController controller = new CvLicenseIssueController(service);
        CvLicenseIssueResult issueResult = CvLicenseIssueResult.issued("{\"schemaVersion\":\"license.v1\"}", new CvLicenseIssue());
        when(service.issueManualLicense(any())).thenReturn(issueResult);
        CvLicenseIssueRequest request = new CvLicenseIssueRequest();
        request.setIssuedBy("client-forged-user");

        R<CvLicenseIssueResult> response = controller.issue(request);

        assertEquals(R.SUCCESS, response.getCode());
        assertSame(issueResult, response.getData());
        ArgumentCaptor<CvLicenseIssueRequest> requestCaptor = ArgumentCaptor.forClass(CvLicenseIssueRequest.class);
        verify(service).issueManualLicense(requestCaptor.capture());
        assertEquals("vendor-system", requestCaptor.getValue().getIssuedBy());
    }

    @Test
    void issueReturnsFailureResultWhenServiceRejectsRequest() {
        ICvLicenseIssueService service = mock(ICvLicenseIssueService.class);
        CvLicenseIssueController controller = new CvLicenseIssueController(service);
        CvLicenseIssueResult issueResult = CvLicenseIssueResult.failed(
            "UNSUPPORTED_ALGORITHM", "unsupported license algorithm");
        when(service.issueManualLicense(any())).thenReturn(issueResult);

        R<CvLicenseIssueResult> response = controller.issue(new CvLicenseIssueRequest());

        assertEquals(R.FAIL, response.getCode());
        assertEquals("UNSUPPORTED_ALGORITHM: unsupported license algorithm", response.getMsg());
        assertSame(issueResult, response.getData());
    }

    @Test
    void issueReturnsDuplicateRejectionStatusWhenServiceBlocksConservativeRetry() {
        ICvLicenseIssueService service = mock(ICvLicenseIssueService.class);
        CvLicenseIssueController controller = new CvLicenseIssueController(service);
        CvLicenseIssueResult issueResult = CvLicenseIssueResult.failed(
            "DUPLICATE_LICENSE_ISSUE", "license already issued for the same customer, installId, and validity window");
        when(service.issueManualLicense(any())).thenReturn(issueResult);

        R<CvLicenseIssueResult> response = controller.issue(new CvLicenseIssueRequest());

        assertEquals(R.FAIL, response.getCode());
        assertEquals(
            "DUPLICATE_LICENSE_ISSUE: license already issued for the same customer, installId, and validity window",
            response.getMsg());
        assertSame(issueResult, response.getData());
    }

    @Test
    void reissueOverridesRequestOperatorWithServerSideOperatorAndReturnsLicenseResult() {
        ICvLicenseIssueService service = mock(ICvLicenseIssueService.class);
        CvLicenseIssueController controller = new CvLicenseIssueController(service);
        CvLicenseIssueResult issueResult = CvLicenseIssueResult.issued("{\"schemaVersion\":\"license.v1\"}", new CvLicenseIssue());
        when(service.reissueRevokedLicense(any())).thenReturn(issueResult);
        CvLicenseReissueRequest request = new CvLicenseReissueRequest();
        request.setIssuedBy("client-forged-user");

        R<CvLicenseIssueResult> response = controller.reissue(request);

        assertEquals(R.SUCCESS, response.getCode());
        assertSame(issueResult, response.getData());
        ArgumentCaptor<CvLicenseReissueRequest> requestCaptor = ArgumentCaptor.forClass(CvLicenseReissueRequest.class);
        verify(service).reissueRevokedLicense(requestCaptor.capture());
        assertEquals("vendor-system", requestCaptor.getValue().getIssuedBy());
    }
}
