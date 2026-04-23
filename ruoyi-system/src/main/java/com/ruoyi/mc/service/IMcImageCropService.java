package com.ruoyi.mc.service;

import com.ruoyi.mc.domain.CropRequest;
import com.ruoyi.mc.domain.CropResult;
import java.io.File;
import java.util.List;

/**
 * MC图片裁剪Service接口
 *
 * @author ruoyi
 * @date 2026-02-03
 */
public interface IMcImageCropService
{
    /**
     * 裁剪图片并按版本命名规则打包
     *
     * @param request 裁剪请求
     * @return 裁剪结果
     */
    CropResult cropAndPackage(CropRequest request);

    /**
     * 裁剪单张图片为多个小图
     *
     * @param imagePath 原图路径
     * @param hLines 横线Y坐标数组
     * @param vLines 竖线X坐标数组
     * @return 裁剪后的文件列表
     */
    List<File> cropImage(String imagePath, List<Integer> hLines, List<Integer> vLines);

    /**
     * 根据版本命名规则重命名文件
     *
     * @param files 文件列表
     * @param versionName 版本号
     * @param rows 行数
     * @param cols 列数
     * @return 重命名后的文件列表
     */
    List<File> renameByVersionRule(List<File> files, String versionName, int rows, int cols);

    /**
     * 打包文件为ZIP
     *
     * @param files 文件列表
     * @param zipFileName ZIP文件名
     * @return ZIP文件路径
     */
    String packToZip(List<File> files, String zipFileName);
}
