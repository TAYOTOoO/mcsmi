package com.ruoyi.mc.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.mc.mapper.McAnnouncementMapper;
import com.ruoyi.mc.domain.McAnnouncement;
import com.ruoyi.mc.service.IMcAnnouncementService;

/**
 * 公告Service业务层处理
 *
 * @author ruoyi
 * @date 2026-02-04
 */
@Service
public class McAnnouncementServiceImpl implements IMcAnnouncementService
{
    @Autowired
    private McAnnouncementMapper mcAnnouncementMapper;

    /**
     * 查询公告
     *
     * @param announcementId 公告主键
     * @return 公告
     */
    @Override
    public McAnnouncement selectMcAnnouncementByAnnouncementId(Long announcementId)
    {
        return mcAnnouncementMapper.selectMcAnnouncementByAnnouncementId(announcementId);
    }

    /**
     * 查询公告列表
     *
     * @param mcAnnouncement 公告
     * @return 公告
     */
    @Override
    public List<McAnnouncement> selectMcAnnouncementList(McAnnouncement mcAnnouncement)
    {
        return mcAnnouncementMapper.selectMcAnnouncementList(mcAnnouncement);
    }

    @Override
    public List<McAnnouncement> selectActiveAnnouncements() {
        return mcAnnouncementMapper.selectActiveAnnouncements();
    }

    /**
     * 新增公告
     *
     * @param mcAnnouncement 公告
     * @return 结果
     */
    @Override
    public int insertMcAnnouncement(McAnnouncement mcAnnouncement)
    {
        mcAnnouncement.setCreateTime(DateUtils.getNowDate());
        return mcAnnouncementMapper.insertMcAnnouncement(mcAnnouncement);
    }

    /**
     * 修改公告
     *
     * @param mcAnnouncement 公告
     * @return 结果
     */
    @Override
    public int updateMcAnnouncement(McAnnouncement mcAnnouncement)
    {
        mcAnnouncement.setUpdateTime(DateUtils.getNowDate());
        return mcAnnouncementMapper.updateMcAnnouncement(mcAnnouncement);
    }

    /**
     * 批量删除公告
     *
     * @param announcementIds 需要删除的公告主键
     * @return 结果
     */
    @Override
    public int deleteMcAnnouncementByAnnouncementIds(Long[] announcementIds)
    {
        return mcAnnouncementMapper.deleteMcAnnouncementByAnnouncementIds(announcementIds);
    }

    /**
     * 删除公告信息
     *
     * @param announcementId 公告主键
     * @return 结果
     */
    @Override
    public int deleteMcAnnouncementByAnnouncementId(Long announcementId)
    {
        return mcAnnouncementMapper.deleteMcAnnouncementByAnnouncementId(announcementId);
    }
}
