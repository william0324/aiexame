package org.example.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.entity.Category;
import org.example.mapper.CategoryMapper;
import org.example.mapper.QuestionMapper;
import org.example.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
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
        List<Map<String, Object>> questionListMap = questionMapper.selectCountByCategoryId();

        // 将分组查询结果转换为Map,方便根据分类ID获取题目数量
        Map<Long, Long> questionMap = questionListMap.stream()
                .collect(Collectors.toMap(
                        map -> Long.parseLong(map.get("categoryId").toString()),
                        map -> Long.parseLong(map.get("count").toString())
                ));
        log.info("获取各个分类下面的题目数量：{}", questionMap);
        // 3.设置题目数量
        categories.forEach(category ->
            category.setCount(questionMap.getOrDefault(category.getId(), 0L))
        );
        return categories;
    }

    @Override
    public List<Category> getCategoryTree() {
        // 1.获取所有分类信息
        List<Category> allNodes = getCategories();
        // 2.将分类信息转换为树形结构
        Map<Long, List<Category>> nodeMap = allNodes.stream()
                .collect(Collectors.groupingBy(Category::getParentId));
        // 3.为每个分类信息设置子类列表
        List<Category> rootNodes = new ArrayList<>();
        for (Category allNode : allNodes) {
            allNode.setChildren(nodeMap.getOrDefault(allNode.getId(), Collections.emptyList()));
            // 父分类的题目数量 = 自身直接关联的题目数 + 所有子分类（递归）的题目总数
            long childrenTotalCount = allNode.getChildren().stream()
                    .mapToLong(Category::getCount)
                    .sum();
            long directCount = allNode.getCount();
            long subtreeCount = directCount + childrenTotalCount;
            allNode.setCount(subtreeCount);
            allNode.getChildren().sort(Comparator.comparing(Category::getSort));
            if (allNode.getParentId() == 0) {
                rootNodes.add(allNode);
            }
        }
        rootNodes.sort(Comparator.comparing(Category::getSort));
        return rootNodes;
    }
}