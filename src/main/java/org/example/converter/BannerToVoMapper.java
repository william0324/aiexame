package org.example.converter;

import org.example.entity.Banner;
import org.example.vo.BannerVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Banner 转换为 BannerVo 的映射器
 */
@Mapper(componentModel = "spring")
public interface BannerToVoMapper {
    /**
     * 获取 BannerToVoMapper 的实例
     */
    BannerToVoMapper INSTANCE = Mappers.getMapper(BannerToVoMapper.class);

    /**
     * 将 Banner 对象转换为 BannerVo 对象
     *
     * @param banner 待转换的 Banner 对象
     * @return 转换后的 BannerVo 对象
     */
    BannerVo toVo(Banner banner);

    /**
     * 将 Banner 对象列表转换为 BannerVo 对象列表
     *
     * @param banners 待转换的 Banner 对象列表
     * @return 转换后的 BannerVo 对象列表
     */
    List<BannerVo> toVoList(List<Banner> banners);
}
