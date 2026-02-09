package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Banner;
import org.example.mapper.BannerMapper;
import org.example.service.BannerService;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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
}