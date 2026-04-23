package com.ruoyi.mc.mapper;

import java.util.List;
import com.ruoyi.mc.domain.McAnnouncement;

/**
 * 公告Mapper接口
 *
 * @author ruoyi
 * @date 2026-02-04
 */
public interface McAnnouncementMapper
{
    /**
     * 查询公告
     *
     * @param announcementId 公告主键
     * @return 公告
     */
    public McAnnouncement selectMcAnnouncementByAnnouncementId(Long announcementId);

    /**
     * 查询公告列表
     *
     * @param mcAnnouncement 公告
     * @return 公告集合
     */
    public List<McAnnouncement> selectMcAnnouncementList(McAnnouncement mcAnnouncement);

    /**
     * 查询当前有效的公告列表
     *
     * @return 公告集合
     */
    public List<McAnnouncement> selectActiveAnnouncements();

    /**
     * 新增公告
     *
     * @param mcAnnouncement 公告
     * @return 结果
     */
    public int insertMcAnnouncement(McAnnouncement mcAnnouncement);

    /**
     * 修改公告
     *
     * @param mcAnnouncement 公告
     * @return 结果
     */
    public int updateMcAnnouncement(McAnnouncement mcAnnouncement);

    /**
     * 删除公告
     *
     * @param announcementId 公告主键
     * @return 结果
     */
    public int deleteMcAnnouncementByAnnouncementId(Long announcementId);

    /**
     * 批量删除公告
     *
     * @param announcementIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMcAnnouncementByAnnouncementIds(Long[] announcementIds);
}
