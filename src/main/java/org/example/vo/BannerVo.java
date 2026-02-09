package org.example.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "轮播图返回给前端Vo")
public class BannerVo {
    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "轮播图标题",
            example = "智能考试系统介绍")
    private String title; // 轮播图标题

    @Schema(description = "轮播图描述内容",
            example = "基于AI技术的智能考试平台，支持在线考试、智能组卷等功能")
    private String description; // 轮播图描述

    @Schema(description = "轮播图片URL地址",
            example = "https://example.com/images/banner1.jpg")
    private String imageUrl; // 图片URL

    @Schema(description = "点击跳转链接，可选",
            example = "https://example.com/about")
    private String linkUrl; // 跳转链接

    @Schema(description = "排序顺序，数字越小越靠前",
            example = "1")
    private Integer sortOrder; // 排序顺序

    @Schema(description = "是否启用显示",
            example = "true")
    private Boolean isActive; // 是否启用

}
