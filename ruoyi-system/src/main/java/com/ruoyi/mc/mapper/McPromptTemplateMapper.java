package com.ruoyi.mc.mapper;

import java.util.List;
import com.ruoyi.mc.domain.McPromptTemplate;

/**
 * MC预提示词模板Mapper接口
 *
 * @author ruoyi
 * @date 2026-01-27
 */
public interface McPromptTemplateMapper
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
     * 删除MC预提示词模板
     *
     * @param templateId MC预提示词模板主键
     * @return 结果
     */
    public int deleteMcPromptTemplateByTemplateId(Long templateId);

    /**
     * 批量删除MC预提示词模板
     *
     * @param templateIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMcPromptTemplateByTemplateIds(Long[] templateIds);
}
