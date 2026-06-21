package org.dromara.carbon.vendor.service;

import org.dromara.carbon.vendor.domain.bo.OnlinePurchaseCreateBo;
import org.dromara.carbon.vendor.domain.bo.PaymentNotifyBo;
import org.dromara.carbon.vendor.domain.vo.OnlinePurchaseOrderVo;

public interface IOnlinePurchaseService {

    OnlinePurchaseOrderVo createOrder(OnlinePurchaseCreateBo bo);

    OnlinePurchaseOrderVo queryOrder(String orderNo);

    OnlinePurchaseOrderVo markPaid(PaymentNotifyBo bo);

}
