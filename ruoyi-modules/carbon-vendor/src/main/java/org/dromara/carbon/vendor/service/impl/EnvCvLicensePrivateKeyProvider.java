package org.dromara.carbon.vendor.service.impl;

import org.dromara.carbon.vendor.service.CvLicensePrivateKeyProvider;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Resolves vendor license signing key material from environment variables.
 */
@Component
public class EnvCvLicensePrivateKeyProvider implements CvLicensePrivateKeyProvider {

    private static final String ENV_PREFIX = "env:";

    @Override
    public String resolvePrivateKeyPem(String privateKeyRef) {
        if (StringUtils.isBlank(privateKeyRef)) {
            return null;
        }
        String envName = privateKeyRef.startsWith(ENV_PREFIX)
            ? privateKeyRef.substring(ENV_PREFIX.length())
            : privateKeyRef;
        return System.getenv(envName);
    }
}
