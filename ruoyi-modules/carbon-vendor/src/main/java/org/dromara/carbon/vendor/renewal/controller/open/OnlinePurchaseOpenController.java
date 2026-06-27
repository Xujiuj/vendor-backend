package org.dromara.carbon.vendor.renewal.controller.open;

import cn.dev33.satoken.annotation.SaIgnore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.renewal.domain.bo.OnlinePurchaseCreateBo;
import org.dromara.carbon.vendor.renewal.domain.bo.PaymentNotifyBo;
import org.dromara.carbon.vendor.renewal.domain.vo.OnlinePurchaseOrderStatusVo;
import org.dromara.carbon.vendor.renewal.domain.vo.OnlinePurchaseOrderVo;
import org.dromara.carbon.vendor.renewal.service.IOnlinePurchaseService;
import org.dromara.carbon.vendor.renewal.service.PaymentGatewayNotifyAdapter;
import org.dromara.common.core.domain.R;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import org.dromara.common.core.utils.MapstructUtils;

/**
 * Public online purchase endpoints for enterprise-local deployments.
 */
@SaIgnore
@RequiredArgsConstructor
@RestController
@RequestMapping("/open/purchases")
public class OnlinePurchaseOpenController {

    private final IOnlinePurchaseService onlinePurchaseService;
    private final PaymentGatewayNotifyAdapter paymentGatewayNotifyAdapter;

    @PostMapping
    public R<OnlinePurchaseOrderVo> createOrder(@Valid @RequestBody OnlinePurchaseCreateBo bo) {
        return R.ok(onlinePurchaseService.createOrder(bo));
    }

    @GetMapping("/{orderNo}")
    public R<OnlinePurchaseOrderStatusVo> queryOrder(@PathVariable String orderNo) {
        OnlinePurchaseOrderVo full = onlinePurchaseService.queryOrder(orderNo);
        return R.ok(MapstructUtils.convert(full, OnlinePurchaseOrderStatusVo.class));
    }

    @PostMapping(value = "/notify/wechat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> notifyWechat(@RequestBody String rawBody, HttpServletRequest request) {
        PaymentNotifyBo bo = paymentGatewayNotifyAdapter.fromWechat(
            rawBody,
            firstHeader(request, "Wechatpay-Timestamp"),
            firstHeader(request, "Wechatpay-Nonce"),
            firstHeader(request, "Wechatpay-Signature")
        );
        onlinePurchaseService.markPaid(bo);
        return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "成功"));
    }

    @PostMapping(value = "/notify/alipay", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String notifyAlipay(@RequestParam Map<String, String> form) {
        PaymentNotifyBo bo = paymentGatewayNotifyAdapter.fromAlipay(form);
        onlinePurchaseService.markPaid(bo);
        return "success";
    }

    private String firstHeader(HttpServletRequest request, String... names) {
        for (String name : names) {
            String value = request.getHeader(name);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

}
