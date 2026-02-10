package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.entity.Banner;
import org.example.mapper.BannerMapper;
import org.example.service.BannerService;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.service.FileUploadService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 轮播图服务实现类
 */
@Service
@Slf4j
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Resource
    private BannerMapper bannerMapper;
    @Resource
    private FileUploadService fileUploadService;
    @Override
    public List<Banner> getAllBanners() {
        LambdaQueryWrapper<Banner> query = new LambdaQueryWrapper<>();
        query.eq(Banner::getIsDeleted, 0);
        query.orderByAsc(Banner::getSortOrder);
        List<Banner> banners = bannerMapper.selectList(query);
        log.info("获取所有轮播图列表：{}", banners);
        return banners;
    }

    @Override
    public List<Banner> getActiveBanners() {
        LambdaQueryWrapper<Banner> query = new LambdaQueryWrapper<>();
        query.eq(Banner::getIsDeleted, 0);
        query.eq(Banner::getIsActive, true);
        query.orderByAsc(Banner::getSortOrder);
        List<Banner> banners = bannerMapper.selectList(query);
        log.info("获取所有启用的轮播图列表：{}", banners);
        return banners;
    }

    @Override
    public Boolean toggleBannerStatus(Long id, Boolean isActive) {
        LambdaUpdateWrapper<Banner> update = new LambdaUpdateWrapper<>();
        update.eq(Banner::getId, id);
        update.set(Banner::getIsActive, isActive);
        int updated = bannerMapper.update(null, update);
        log.info("更新轮播图状态：id={}, isActive={}, updated={}", id, isActive, updated);
        return updated > 0;
    }

    @Override
    public Boolean deleteBanner(Long id) {
        LambdaUpdateWrapper<Banner> update = new LambdaUpdateWrapper<>();
        update.eq(Banner::getId, id);
        update.set(Banner::getIsDeleted, 1);
        int updated = bannerMapper.update(null, update);
        log.info("删除轮播图：id={}, updated={}", id, updated);
        return updated > 0;
    }

    @Override
    public Banner getBannerById(Long id) {
        return bannerMapper.selectById(id);
    }

    @Override
    public String uploadBannerImage(MultipartFile file) throws Exception {
        //1.文件非空校验
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }
        //2.文件格式校验
        String contentType = file.getContentType();
        if (StringUtils.isEmpty(contentType) || !contentType.startsWith("image/")) {
            throw new RuntimeException("上传文件格式不正确,请上传图片！");
        }
        //3.文件大小小于5Mb
        long size = file.getSize();
        if (size > 5 * 1024 * 1024) {
            throw new RuntimeException("上传文件大小不能超过5Mb");
        }
        String imgUrl = fileUploadService.uploadFile(file, "banner");
        log.info("上传轮播图成功，照片回显地址:{}", imgUrl);
        return imgUrl;
    }

    @Override
    public void addBanner(Banner banner) {
        // 1. 默认启用
        if (banner.getIsActive() == null) {
            banner.setIsActive(true);
        }
        // 2.优先级默认0
        if (banner.getSortOrder() == null) {
            banner.setSortOrder(0);
        }
        // 3.保存到数据库
        int insert = bannerMapper.insert(banner);
        if (insert > 0) {
            log.info("添加轮播图成功：{}", banner);
        } else {
            throw new RuntimeException("添加轮播图失败");
        }
    }

    @Override
    public void updateBanner(Banner banner) {
        int update = bannerMapper.updateById(banner);
        if (update > 0) {
            log.info("更新轮播图成功：{}", banner);
        } else {
            throw new RuntimeException("更新轮播图失败");
        }
    }
}