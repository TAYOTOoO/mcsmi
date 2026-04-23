package com.ruoyi.mc.service.impl;

import java.util.List;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.mc.mapper.McPromptTemplateMapper;
import com.ruoyi.mc.domain.McPromptTemplate;
import com.ruoyi.mc.service.IMcPromptTemplateService;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * MC预提示词模板Service业务层处理
 *
 * @author ruoyi
 * @date 2026-01-27
 */
@Service
public class McPromptTemplateServiceImpl implements IMcPromptTemplateService
{
    @Autowired
    private McPromptTemplateMapper mcPromptTemplateMapper;

    /**
     * 查询MC预提示词模板
     *
     * @param templateId MC预提示词模板主键
     * @return MC预提示词模板
     */
    @Override
    public McPromptTemplate selectMcPromptTemplateByTemplateId(Long templateId)
    {
        return mcPromptTemplateMapper.selectMcPromptTemplateByTemplateId(templateId);
    }

    /**
     * 查询MC预提示词模板列表
     *
     * @param mcPromptTemplate MC预提示词模板
     * @return MC预提示词模板
     */
    @Override
    public List<McPromptTemplate> selectMcPromptTemplateList(McPromptTemplate mcPromptTemplate)
    {
        return mcPromptTemplateMapper.selectMcPromptTemplateList(mcPromptTemplate);
    }

    /**
     * 获取当前启用的模板
     *
     * @return MC预提示词模板
     */
    @Override
    public McPromptTemplate getActiveTemplate()
    {
        return mcPromptTemplateMapper.getActiveTemplate();
    }

    /**
     * 获取所有启用的模板列表
     *
     * @return MC预提示词模板集合
     */
    @Override
    public List<McPromptTemplate> getActiveTemplates()
    {
        return mcPromptTemplateMapper.getActiveTemplates();
    }

    /**
     * 新增MC预提示词模板
     *
     * @param mcPromptTemplate MC预提示词模板
     * @return 结果
     */
    @Override
    public int insertMcPromptTemplate(McPromptTemplate mcPromptTemplate)
    {
        mcPromptTemplate.setCreateTime(new Date());
        try {
            mcPromptTemplate.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
            mcPromptTemplate.setCreateBy("system");
        }
        return mcPromptTemplateMapper.insertMcPromptTemplate(mcPromptTemplate);
    }

    /**
     * 修改MC预提示词模板
     *
     * @param mcPromptTemplate MC预提示词模板
     * @return 结果
     */
    @Override
    public int updateMcPromptTemplate(McPromptTemplate mcPromptTemplate)
    {
        mcPromptTemplate.setUpdateTime(new Date());
        try {
            mcPromptTemplate.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
            mcPromptTemplate.setUpdateBy("system");
        }
        return mcPromptTemplateMapper.updateMcPromptTemplate(mcPromptTemplate);
    }

    /**
     * 批量删除MC预提示词模板
     *
     * @param templateIds 需要删除的MC预提示词模板主键
     * @return 结果
     */
    @Override
    public int deleteMcPromptTemplateByTemplateIds(Long[] templateIds)
    {
        return mcPromptTemplateMapper.deleteMcPromptTemplateByTemplateIds(templateIds);
    }

    /**
     * 删除MC预提示词模板信息
     *
     * @param templateId MC预提示词模板主键
     * @return 结果
     */
    @Override
    public int deleteMcPromptTemplateByTemplateId(Long templateId)
    {
        return mcPromptTemplateMapper.deleteMcPromptTemplateByTemplateId(templateId);
    }

    /**
     * 切换模板启用状态
     *
     * @param templateId 模板ID
     * @return 结果
     */
    @Override
    public int activateTemplate(Long templateId)
    {
        // 获取当前模板
        McPromptTemplate existing = mcPromptTemplateMapper.selectMcPromptTemplateByTemplateId(templateId);
        if (existing == null)
        {
            return 0;
        }

        // 切换启用状态
        McPromptTemplate template = new McPromptTemplate();
        template.setTemplateId(templateId);
        String newActive = "1".equals(existing.getIsActive()) ? "0" : "1";
        template.setIsActive(newActive);
        template.setUpdateTime(new Date());
        try {
            template.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
            template.setUpdateBy("system");
        }
        return mcPromptTemplateMapper.updateMcPromptTemplate(template);
    }
}
