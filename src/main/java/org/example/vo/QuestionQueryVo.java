package org.example.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 题目分页查询请求VO
 */
@Data
@Schema(description = "题目分页查询请求参数")
public class QuestionQueryVo {

    @Schema(description = "当前页码，从1开始", example = "1")
    private Integer page = 1;

    @Schema(description = "每页显示数量", example = "10")
    private Integer size = 10;

    @Schema(description = "分类ID筛选条件", example = "1")
    private Long categoryId;

    @Schema(description = "难度筛选条件", example = "MEDIUM", allowableValues = {"EASY", "MEDIUM", "HARD"})
    private String difficulty;

    @Schema(description = "题型筛选条件", example = "CHOICE", allowableValues = {"CHOICE", "JUDGE", "TEXT"})
    private String type;

    @Schema(description = "关键词搜索，对题目标题进行模糊查询", example = "Java")
    private String keyword;
}