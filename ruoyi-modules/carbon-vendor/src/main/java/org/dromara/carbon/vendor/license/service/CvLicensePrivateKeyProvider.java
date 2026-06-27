package org.dromara.carbon.vendor.license.service;

/**
 * Resolves protected private key material from an operator-controlled reference.
 */
public interface CvLicensePrivateKeyProvider {

    String resolvePrivateKeyPem(String privateKeyRef);
}
