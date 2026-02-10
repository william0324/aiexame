package org.example.service;

import org.example.entity.Banner;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

/**
 * 轮播图服务接口
 */
public interface BannerService extends IService<Banner> {

    List<Banner> getAllBanners();

    List<Banner> getActiveBanners();

    Boolean toggleBannerStatus(Long id, Boolean isActive);

    Boolean deleteBanner(Long id);

    Banner getBannerById(Long id);

    String uploadBannerImage(MultipartFile file) throws Exception;

    void addBanner(Banner banner);

    void updateBanner(Banner banner);
}