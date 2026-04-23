package com.ruoyi.mc.mapper;

import com.ruoyi.mc.domain.McRechargePackage;
import java.util.List;

/**
 * 充值套餐Mapper接口
 */
public interface McRechargePackageMapper {

    /**
     * 查询充值套餐列表
     */
    public List<McRechargePackage> selectMcRechargePackageList(McRechargePackage mcRechargePackage);

    /**
     * 根据套餐ID查询充值套餐
     */
    public McRechargePackage selectMcRechargePackageByPackageId(Long packageId);

    /**
     * 查询所有可用套餐（status='0'）
     */
    public List<McRechargePackage> selectAvailablePackages();

    /**
     * 新增充值套餐
     */
    public int insertMcRechargePackage(McRechargePackage mcRechargePackage);

    /**
     * 修改充值套餐
     */
    public int updateMcRechargePackage(McRechargePackage mcRechargePackage);

    /**
     * 删除充值套餐
     */
    public int deleteMcRechargePackageByPackageId(Long packageId);
}
