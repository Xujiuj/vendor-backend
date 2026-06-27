package org.dromara.carbon.vendor.renewal.service;

import org.dromara.carbon.vendor.renewal.domain.bo.OnlinePurchaseCreateBo;
import org.dromara.carbon.vendor.renewal.domain.bo.PaymentNotifyBo;
import org.dromara.carbon.vendor.renewal.domain.vo.OnlinePurchaseOrderVo;

public interface IOnlinePurchaseService {

    OnlinePurchaseOrderVo createOrder(OnlinePurchaseCreateBo bo);

    OnlinePurchaseOrderVo queryOrder(String orderNo);

    OnlinePurchaseOrderVo markPaid(PaymentNotifyBo bo);

}
