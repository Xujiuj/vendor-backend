package org.dromara.carbon.vendor.service;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.config.VendorPaymentProperties;
import org.dromara.carbon.vendor.domain.bo.PaymentNotifyBo;
import org.dromara.carbon.vendor.domain.bo.WechatPaymentNotifyBo;
import org.dromara.carbon.vendor.domain.bo.WechatPaymentResourceBo;
import org.dromara.common.core.exception.ServiceException;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts gateway-native webhook payloads into normalized payment results.
 */
@Component
@RequiredArgsConstructor
public class PaymentGatewayNotifyAdapter {

    private static final String CHANNEL_WECHAT = "WECHAT";
    private static final String CHANNEL_ALIPAY = "ALIPAY";
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final VendorPaymentProperties paymentProperties;
    private final ObjectMapper objectMapper;

    public PaymentNotifyBo fromWechat(String rawBody, String timestamp, String nonce, String signatureText) {
        if (paymentProperties.isRequireSignature()) {
            verifyWechatSignature(rawBody, timestamp, nonce, signatureText);
        }
        try {
            WechatPaymentNotifyBo notify = objectMapper.readValue(rawBody, WechatPaymentNotifyBo.class);
            if (notify.getResource() == null) {
                throw new ServiceException("Wechat payment notify resource is missing");
            }
            String decrypted = decryptWechatResource(notify.getResource());
            WechatPaymentResourceBo resource = objectMapper.readValue(decrypted, WechatPaymentResourceBo.class);
            validateWechatMerchant(resource);

            PaymentNotifyBo bo = new PaymentNotifyBo();
            bo.setPayChannel(CHANNEL_WECHAT);
            bo.setOrderNo(resource.getOutTradeNo());
            bo.setTradeNo(resource.getTransactionId());
            bo.setTradeStatus(resource.getTradeState());
            if (resource.getAmount() != null) {
                bo.setAmount(resource.getAmount().totalYuan());
                bo.setCurrency(resource.getAmount().getCurrency());
            }
            bo.setSignature(signatureText);
            bo.setRawBody(rawBody);
            return bo;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Wechat payment notify parse failed");
        }
    }

    public PaymentNotifyBo fromAlipay(Map<String, String> form) {
        if (paymentProperties.isRequireSignature()) {
            verifyAlipaySignature(form);
        }
        validateAlipayMerchant(form);
        PaymentNotifyBo bo = new PaymentNotifyBo();
        bo.setPayChannel(CHANNEL_ALIPAY);
        bo.setOrderNo(form.get("out_trade_no"));
        bo.setTradeNo(form.get("trade_no"));
        bo.setTradeStatus(form.get("trade_status"));
        bo.setSignature(form.get("sign"));
        bo.setRawBody(canonicalAlipayContent(form));
        if (StrUtil.isNotBlank(form.get("total_amount"))) {
            bo.setAmount(new java.math.BigDecimal(form.get("total_amount")));
        }
        bo.setCurrency("CNY");
        return bo;
    }

    private void verifyWechatSignature(String rawBody, String timestamp, String nonce, String signatureText) {
        if (StrUtil.hasBlank(timestamp, nonce, signatureText)) {
            throw new ServiceException("Wechat payment notify signature headers are missing");
        }
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(readWechatPublicKey());
            signature.update((timestamp + "\n" + nonce + "\n" + rawBody + "\n").getBytes(StandardCharsets.UTF_8));
            if (!signature.verify(Base64.getDecoder().decode(signatureText))) {
                throw new ServiceException("Wechat payment notify signature is invalid");
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Wechat payment notify signature verify failed");
        }
    }

    private String decryptWechatResource(WechatPaymentNotifyBo.Resource resource) throws Exception {
        VendorPaymentProperties.Channel wechat = paymentProperties.getWechat();
        if (StrUtil.isBlank(wechat.getApiV3Key())) {
            throw new ServiceException("Wechat Pay API v3 key is not configured");
        }
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec key = new SecretKeySpec(wechat.getApiV3Key().getBytes(StandardCharsets.UTF_8), "AES");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, resource.getNonce().getBytes(StandardCharsets.UTF_8)));
        if (StrUtil.isNotBlank(resource.getAssociatedData())) {
            cipher.updateAAD(resource.getAssociatedData().getBytes(StandardCharsets.UTF_8));
        }
        byte[] plain = cipher.doFinal(Base64.getDecoder().decode(resource.getCiphertext()));
        return new String(plain, StandardCharsets.UTF_8);
    }

    private void validateWechatMerchant(WechatPaymentResourceBo resource) {
        VendorPaymentProperties.Channel wechat = paymentProperties.getWechat();
        if (StrUtil.isNotBlank(wechat.getAppId()) && !StrUtil.equals(wechat.getAppId(), resource.getAppId())) {
            throw new ServiceException("Wechat Pay appId mismatch");
        }
        if (StrUtil.isNotBlank(wechat.getMerchantId()) && !StrUtil.equals(wechat.getMerchantId(), resource.getMerchantId())) {
            throw new ServiceException("Wechat Pay merchantId mismatch");
        }
    }

    private PublicKey readWechatPublicKey() throws Exception {
        String certificate = paymentProperties.getWechat().getPlatformCertificate();
        if (StrUtil.isBlank(certificate)) {
            throw new ServiceException("Wechat Pay platform certificate is not configured");
        }
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) factory.generateCertificate(
            new java.io.ByteArrayInputStream(Base64.getDecoder().decode(normalizePem(certificate))));
        return cert.getPublicKey();
    }

    private void verifyAlipaySignature(Map<String, String> form) {
        String sign = form.get("sign");
        if (StrUtil.isBlank(sign)) {
            throw new ServiceException("Alipay notify signature is missing");
        }
        try {
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(readAlipayPublicKey());
            verifier.update(canonicalAlipayContent(form).getBytes(StandardCharsets.UTF_8));
            if (!verifier.verify(Base64.getDecoder().decode(sign))) {
                throw new ServiceException("Alipay notify signature is invalid");
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Alipay notify signature verify failed");
        }
    }

    private String canonicalAlipayContent(Map<String, String> form) {
        return form.entrySet().stream()
            .filter(entry -> StrUtil.isNotBlank(entry.getKey()))
            .filter(entry -> !"sign".equals(entry.getKey()) && !"sign_type".equals(entry.getKey()))
            .filter(entry -> entry.getValue() != null)
            .sorted(Comparator.comparing(Map.Entry::getKey))
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("&"));
    }

    private void validateAlipayMerchant(Map<String, String> form) {
        VendorPaymentProperties.Channel alipay = paymentProperties.getAlipay();
        if (StrUtil.isNotBlank(alipay.getAppId()) && !StrUtil.equals(alipay.getAppId(), form.get("app_id"))) {
            throw new ServiceException("Alipay appId mismatch");
        }
        if (StrUtil.isNotBlank(alipay.getSellerId()) && !StrUtil.equals(alipay.getSellerId(), form.get("seller_id"))) {
            throw new ServiceException("Alipay sellerId mismatch");
        }
    }

    private PublicKey readAlipayPublicKey() throws Exception {
        String publicKey = paymentProperties.getAlipay().getPublicKey();
        if (StrUtil.isBlank(publicKey)) {
            throw new ServiceException("Alipay public key is not configured");
        }
        byte[] encoded = Base64.getDecoder().decode(normalizePem(publicKey));
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
    }

    private String normalizePem(String value) {
        return value
            .replace("-----BEGIN CERTIFICATE-----", "")
            .replace("-----END CERTIFICATE-----", "")
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\n", "")
            .replace("\r", "")
            .replace("\n", "")
            .replace(" ", "");
    }
}
