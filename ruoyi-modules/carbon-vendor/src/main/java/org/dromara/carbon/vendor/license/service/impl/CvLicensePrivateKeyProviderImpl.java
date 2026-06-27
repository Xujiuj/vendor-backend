package org.dromara.carbon.vendor.license.service.impl;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.carbon.vendor.license.service.CvLicensePrivateKeyProvider;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * Resolves private key material from operator-controlled references.
 */
@Slf4j
@Component
public class CvLicensePrivateKeyProviderImpl implements CvLicensePrivateKeyProvider {

    private static final String ENV_PREFIX = "env:";
    private static final String FILE_PREFIX = "file:";

    @Override
    public String resolvePrivateKeyPem(String privateKeyRef) {
        if (StrUtil.isBlank(privateKeyRef)) {
            return null;
        }
        String ref = privateKeyRef.trim();
        if (ref.startsWith(ENV_PREFIX)) {
            return resolveEnvironmentKey(ref.substring(ENV_PREFIX.length()));
        }
        if (ref.startsWith(FILE_PREFIX)) {
            return resolveFileKey(ref.substring(FILE_PREFIX.length()));
        }
        return ref;
    }

    private String resolveEnvironmentKey(String name) {
        if (StrUtil.isBlank(name)) {
            return null;
        }
        String value = System.getenv(name.trim());
        return StrUtil.isBlank(value) ? null : value;
    }

    private String resolveFileKey(String filePath) {
        if (StrUtil.isBlank(filePath)) {
            return null;
        }
        try {
            String value = Files.readString(Path.of(filePath.trim()), StandardCharsets.UTF_8);
            return StrUtil.isBlank(value) ? null : value;
        } catch (InvalidPathException e) {
            log.warn("Invalid license private key file reference");
            return null;
        } catch (Exception e) {
            log.warn("Unable to read license private key file reference");
            return null;
        }
    }
}
