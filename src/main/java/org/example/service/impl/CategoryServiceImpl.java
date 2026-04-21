package org.example.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.common.Result;
import org.example.entity.Category;
import org.example.entity.Question;
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

    @Override
    public Result<Void> addCategory(Category category) {
        Result<Void> validationResult = validateCategoryName(category, null);
        if (validationResult != null) {
            return validationResult;
        }
        
        categoryMapper.insert(category);
        return Result.success("添加成功");
    }

    @Override
    public Result<Void> updateCategory(Category category) {
        Result<Void> validationResult = validateCategoryName(category, category.getId());
        if (validationResult != null) {
            return validationResult;
        }
        
        categoryMapper.updateById(category);
        return Result.success("更新成功");
    }

    @Override
    public Result<Void> deleteCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }
        
        if (category.getParentId() == 0) {
            return Result.error("一级分类不能删除");
        }
        
        // 递归获取所有子分类ID（包括子分类的子分类）
        List<Long> idsToDelete = new ArrayList<>();
        collectChildCategoryIds(id, idsToDelete);
        
        // 检查所有待删除的分类（包括自身和所有子分类）是否有关联题目
        for (Long categoryId : idsToDelete) {
            LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Question::getCategoryId, categoryId);
            queryWrapper.eq(Question::getIsDeleted, 0);
            Long questionCount = questionMapper.selectCount(queryWrapper);
            
            if (questionCount > 0) {
                Category targetCategory = categoryMapper.selectById(categoryId);
                String errorMsg = String.format("%s分类下有%d道题目，不能删除", 
                    targetCategory != null ? targetCategory.getName() : "未知分类", 
                    questionCount);
                return Result.error(errorMsg);
            }
        }
        
        // 执行逻辑删除：将所有相关分类的 is_deleted 设置为 1
        LambdaQueryWrapper<Category> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.in(Category::getId, idsToDelete);
        
        Category updateEntity = new Category();
        updateEntity.setIsDeleted((byte) 1);
        categoryMapper.update(updateEntity, deleteWrapper);
        
        log.info("删除分类成功，分类ID: {}, 同时删除了{}个子分类", id, idsToDelete.size() - 1);
        return Result.success("删除成功");
    }

    private void collectChildCategoryIds(Long id, List<Long> idsToDelete) {
        // 将当前ID加入待删除列表
        idsToDelete.add(id);
        
        // 查询当前分类的直接子分类
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getParentId, id);
        queryWrapper.eq(Category::getIsDeleted, 0);
        List<Category> children = categoryMapper.selectList(queryWrapper);
        
        // 递归处理每个子分类
        for (Category child : children) {
            collectChildCategoryIds(child.getId(), idsToDelete);
        }
    }

    /**
     * 验证分类名称是否重复
     * @param category 分类对象
     * @param excludeId 需要排除的分类ID（更新时使用，新增时传null）
     * @return 如果验证失败返回错误结果，验证通过返回null
     */
    private Result<Void> validateCategoryName(Category category, Long excludeId) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getName, category.getName());
        queryWrapper.eq(Category::getParentId, category.getParentId());
        queryWrapper.eq(Category::getIsDeleted, 0);
        
        if (excludeId != null) {
            queryWrapper.ne(Category::getId, excludeId);
        }
        
        long count = categoryMapper.selectCount(queryWrapper);
        if (count > 0) {
            Category parentCategory = categoryMapper.selectById(category.getParentId());
            String errorMsg = String.format("在%s分类下，分类%s已经存在了!",
                parentCategory != null ? parentCategory.getName() : "未知",
                category.getName());
            return Result.error(errorMsg);
        }
        
        return null;
    }
}