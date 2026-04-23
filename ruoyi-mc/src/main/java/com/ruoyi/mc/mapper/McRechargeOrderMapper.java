package com.ruoyi.mc.mapper;

import com.ruoyi.mc.domain.McRechargeOrder;
import java.util.List;

/**
 * 充值订单Mapper接口
 */
public interface McRechargeOrderMapper {

    /**
     * 查询充值订单列表
     */
    public List<McRechargeOrder> selectMcRechargeOrderList(McRechargeOrder mcRechargeOrder);

    /**
     * 根据订单ID查询充值订单
     */
    public McRechargeOrder selectMcRechargeOrderByOrderId(Long orderId);

    /**
     * 根据订单号查询充值订单
     */
    public McRechargeOrder selectMcRechargeOrderByOrderNo(String orderNo);

    /**
     * 新增充值订单
     */
    public int insertMcRechargeOrder(McRechargeOrder mcRechargeOrder);

    /**
     * 修改充值订单
     */
    public int updateMcRechargeOrder(McRechargeOrder mcRechargeOrder);

    /**
     * 删除充值订单
     */
    public int deleteMcRechargeOrderByOrderId(Long orderId);
}
