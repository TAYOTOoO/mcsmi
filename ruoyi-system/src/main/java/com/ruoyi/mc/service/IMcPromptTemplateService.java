package com.ruoyi.mc.service;

import java.util.List;
import com.ruoyi.mc.domain.McPromptTemplate;

/**
 * MC预提示词模板Service接口
 *
 * @author ruoyi
 * @date 2026-01-27
 */
public interface IMcPromptTemplateService
{
    /**
     * 查询MC预提示词模板
     *
     * @param templateId MC预提示词模板主键
     * @return MC预提示词模板
     */
    public McPromptTemplate selectMcPromptTemplateByTemplateId(Long templateId);

    /**
     * 查询MC预提示词模板列表
     *
     * @param mcPromptTemplate MC预提示词模板
     * @return MC预提示词模板集合
     */
    public List<McPromptTemplate> selectMcPromptTemplateList(McPromptTemplate mcPromptTemplate);

    /**
     * 获取当前启用的模板
     *
     * @return MC预提示词模板
     */
    public McPromptTemplate getActiveTemplate();

    /**
     * 获取所有启用的模板列表
     *
     * @return MC预提示词模板集合
     */
    public List<McPromptTemplate> getActiveTemplates();

    /**
     * 新增MC预提示词模板
     *
     * @param mcPromptTemplate MC预提示词模板
     * @return 结果
     */
    public int insertMcPromptTemplate(McPromptTemplate mcPromptTemplate);

    /**
     * 修改MC预提示词模板
     *
     * @param mcPromptTemplate MC预提示词模板
     * @return 结果
     */
    public int updateMcPromptTemplate(McPromptTemplate mcPromptTemplate);

    /**
     * 批量删除MC预提示词模板
     *
     * @param templateIds 需要删除的MC预提示词模板主键集合
     * @return 结果
     */
    public int deleteMcPromptTemplateByTemplateIds(Long[] templateIds);

    /**
     * 删除MC预提示词模板信息
     *
     * @param templateId MC预提示词模板主键
     * @return 结果
     */
    public int deleteMcPromptTemplateByTemplateId(Long templateId);

    /**
     * 启用/禁用模板
     *
     * @param templateId 模板ID
     * @return 结果
     */
    public int activateTemplate(Long templateId);
}
