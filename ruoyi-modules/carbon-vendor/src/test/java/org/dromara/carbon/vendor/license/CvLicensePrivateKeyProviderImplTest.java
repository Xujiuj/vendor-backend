package org.dromara.carbon.vendor.license;

import org.dromara.carbon.vendor.license.service.impl.CvLicensePrivateKeyProviderImpl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("dev")
class CvLicensePrivateKeyProviderImplTest {

    private final CvLicensePrivateKeyProviderImpl provider = new CvLicensePrivateKeyProviderImpl();

    @Test
    void returnsDirectPrivateKeyMaterial() {
        assertEquals("PRIVATE-KEY-MATERIAL", provider.resolvePrivateKeyPem(" PRIVATE-KEY-MATERIAL "));
    }

    @Test
    void resolvesPrivateKeyMaterialFromFileReference() throws Exception {
        Path keyFile = Files.createTempFile("license-key-", ".pem");
        Files.writeString(keyFile, "FILE-PRIVATE-KEY", StandardCharsets.UTF_8);

        assertEquals("FILE-PRIVATE-KEY", provider.resolvePrivateKeyPem("file:" + keyFile));
    }

    @Test
    void returnsNullForMissingOrBlankReferences() {
        assertNull(provider.resolvePrivateKeyPem(null));
        assertNull(provider.resolvePrivateKeyPem(" "));
        assertNull(provider.resolvePrivateKeyPem("env:"));
        assertNull(provider.resolvePrivateKeyPem("file:Z:\\not-exists\\license.pem"));
    }
}
