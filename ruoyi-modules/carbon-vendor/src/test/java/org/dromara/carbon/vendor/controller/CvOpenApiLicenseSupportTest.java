package org.dromara.carbon.vendor.openapi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CvOpenApiLicenseSupportTest {

    @Test
    void keepsExplicitLicenseIdBeforeAuthorizationHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer LIC-FROM-HEADER");

        assertEquals("LIC-FROM-QUERY", CvOpenApiLicenseSupport.resolveLicenseId("LIC-FROM-QUERY", request));
    }

    @Test
    void resolvesLicenseIdFromBearerHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer LIC-FROM-HEADER");

        assertEquals("LIC-FROM-HEADER", CvOpenApiLicenseSupport.resolveLicenseId(null, request));
    }
}
