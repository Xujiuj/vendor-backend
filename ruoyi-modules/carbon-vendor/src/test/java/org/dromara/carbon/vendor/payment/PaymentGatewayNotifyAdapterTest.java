package org.dromara.carbon.vendor.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dromara.carbon.vendor.renewal.config.VendorPaymentProperties;
import org.dromara.carbon.vendor.renewal.domain.bo.PaymentNotifyBo;
import org.dromara.carbon.vendor.renewal.service.PaymentGatewayNotifyAdapter;
import org.dromara.common.core.exception.ServiceException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("dev")
class PaymentGatewayNotifyAdapterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parsesWechatApiV3NotifyIntoNormalizedPaymentResult() throws Exception {
        VendorPaymentProperties properties = unsignedProperties();
        properties.getWechat().setApiV3Key("12345678901234567890123456789012");
        properties.getWechat().setAppId("wx-app");
        properties.getWechat().setMerchantId("1900000109");
        PaymentGatewayNotifyAdapter adapter = new PaymentGatewayNotifyAdapter(properties, objectMapper);

        String resource = """
            {"appid":"wx-app","mchid":"1900000109","out_trade_no":"PO1001","transaction_id":"4200001","trade_state":"SUCCESS","amount":{"total":1299,"currency":"CNY"}}
            """;
        String nonce = "nonce12345678";
        String associatedData = "transaction";
        String ciphertext = encryptWechatResource(properties.getWechat().getApiV3Key(), nonce, associatedData, resource);
        String rawBody = """
            {"id":"EV-1","create_time":"2026-06-21T12:00:00+08:00","resource_type":"encrypt-resource","event_type":"TRANSACTION.SUCCESS","summary":"paid","resource":{"algorithm":"AEAD_AES_256_GCM","ciphertext":"%s","associated_data":"%s","nonce":"%s","original_type":"transaction"}}
            """.formatted(ciphertext, associatedData, nonce);

        PaymentNotifyBo bo = adapter.fromWechat(rawBody, null, null, null);

        assertEquals("WECHAT", bo.getPayChannel());
        assertEquals("PO1001", bo.getOrderNo());
        assertEquals("4200001", bo.getTradeNo());
        assertEquals("SUCCESS", bo.getTradeStatus());
        assertEquals("12.99", bo.getAmount().toPlainString());
        assertEquals("CNY", bo.getCurrency());
    }

    @Test
    void rejectsWechatMerchantMismatch() throws Exception {
        VendorPaymentProperties properties = unsignedProperties();
        properties.getWechat().setApiV3Key("12345678901234567890123456789012");
        properties.getWechat().setMerchantId("expected-merchant");
        PaymentGatewayNotifyAdapter adapter = new PaymentGatewayNotifyAdapter(properties, objectMapper);

        String resource = "{\"appid\":\"wx-app\",\"mchid\":\"actual-merchant\",\"out_trade_no\":\"PO1001\",\"transaction_id\":\"4200001\",\"trade_state\":\"SUCCESS\",\"amount\":{\"total\":1,\"currency\":\"CNY\"}}";
        String nonce = "nonce12345678";
        String associatedData = "transaction";
        String ciphertext = encryptWechatResource(properties.getWechat().getApiV3Key(), nonce, associatedData, resource);
        String rawBody = "{\"resource\":{\"ciphertext\":\"" + ciphertext + "\",\"associated_data\":\"" + associatedData + "\",\"nonce\":\"" + nonce + "\"}}";

        assertThrows(ServiceException.class, () -> adapter.fromWechat(rawBody, null, null, null));
    }

    @Test
    void parsesAlipayNotifyIntoNormalizedPaymentResult() {
        VendorPaymentProperties properties = unsignedProperties();
        properties.getAlipay().setAppId("ali-app");
        properties.getAlipay().setSellerId("seller-1");
        PaymentGatewayNotifyAdapter adapter = new PaymentGatewayNotifyAdapter(properties, objectMapper);
        Map<String, String> form = new LinkedHashMap<>();
        form.put("app_id", "ali-app");
        form.put("seller_id", "seller-1");
        form.put("out_trade_no", "PO2001");
        form.put("trade_no", "2026062122001");
        form.put("trade_status", "TRADE_SUCCESS");
        form.put("total_amount", "88.00");

        PaymentNotifyBo bo = adapter.fromAlipay(form);

        assertEquals("ALIPAY", bo.getPayChannel());
        assertEquals("PO2001", bo.getOrderNo());
        assertEquals("2026062122001", bo.getTradeNo());
        assertEquals("TRADE_SUCCESS", bo.getTradeStatus());
        assertEquals("88.00", bo.getAmount().toPlainString());
        assertEquals("CNY", bo.getCurrency());
    }

    @Test
    void verifiesAlipaySandboxRsa2Signature() throws Exception {
        KeyPair keyPair = rsaKeyPair();
        VendorPaymentProperties properties = signedProperties();
        properties.getAlipay().setAppId("ali-sandbox-app");
        properties.getAlipay().setSellerId("2088102180000000");
        properties.getAlipay().setPublicKey(publicKeyPem(keyPair));
        PaymentGatewayNotifyAdapter adapter = new PaymentGatewayNotifyAdapter(properties, objectMapper);
        Map<String, String> form = new LinkedHashMap<>();
        form.put("app_id", "ali-sandbox-app");
        form.put("seller_id", "2088102180000000");
        form.put("out_trade_no", "PO-SANDBOX-ALI-001");
        form.put("trade_no", "202606212200100000000000001");
        form.put("trade_status", "TRADE_SUCCESS");
        form.put("total_amount", "1.00");
        form.put("sign_type", "RSA2");
        form.put("sign", signAlipay(form, keyPair.getPrivate()));

        PaymentNotifyBo bo = adapter.fromAlipay(form);

        assertEquals("PO-SANDBOX-ALI-001", bo.getOrderNo());
        assertEquals("202606212200100000000000001", bo.getTradeNo());
        assertEquals("1.00", bo.getAmount().toPlainString());
    }

    private VendorPaymentProperties unsignedProperties() {
        VendorPaymentProperties properties = new VendorPaymentProperties();
        properties.setRequireSignature(false);
        return properties;
    }

    private VendorPaymentProperties signedProperties() {
        VendorPaymentProperties properties = new VendorPaymentProperties();
        properties.setRequireSignature(true);
        return properties;
    }

    private String encryptWechatResource(String apiV3Key, String nonce, String associatedData, String content) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec key = new SecretKeySpec(apiV3Key.getBytes(StandardCharsets.UTF_8), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, nonce.getBytes(StandardCharsets.UTF_8)));
        cipher.updateAAD(associatedData.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(cipher.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }

    private KeyPair rsaKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private String publicKeyPem(KeyPair keyPair) {
        return "-----BEGIN PUBLIC KEY-----\n"
            + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8))
            .encodeToString(keyPair.getPublic().getEncoded())
            + "\n-----END PUBLIC KEY-----";
    }

    private String signAlipay(Map<String, String> form, PrivateKey privateKey) throws Exception {
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(canonicalAlipayContent(form).getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signer.sign());
    }

    private String canonicalAlipayContent(Map<String, String> form) {
        return form.entrySet().stream()
            .filter(entry -> entry.getValue() != null)
            .filter(entry -> !"sign".equals(entry.getKey()) && !"sign_type".equals(entry.getKey()))
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(java.util.stream.Collectors.joining("&"));
    }
}
