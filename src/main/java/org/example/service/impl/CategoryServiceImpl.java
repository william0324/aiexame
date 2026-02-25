package org.example.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.entity.Category;
import org.example.mapper.CategoryMapper;
import org.example.mapper.QuestionMapper;
import org.example.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private QuestionMapper questionMapper;

    @Override
    public List<Category> getCategories() {
        // 1.获取所有分类信息
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getIsDeleted, 0);
        queryWrapper.orderByAsc(Category::getSort);
        List<Category> categories = categoryMapper.selectList(queryWrapper);
        log.info("获取所有分类信息：{}", categories);
        // 2.获取各个分类下面的题目数量
        // 分组查询
        List<Map<Long, Long>> questionCount = questionMapper.selectCountByCategoryId();


        return List.of();
    }
}