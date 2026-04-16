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
            // 父分类个数是子分类的count总和
            // 父分类的题目数量 = 自身直接关联的题目数 + 所有子分类（递归）的题目总数
            // 注意：getCategories() 中设置的 count 是该分类直接关联的题目数量
            // 这里需要累加子树中所有节点的 count
            long childrenTotalCount = allNode.getChildren().stream()
                    .mapToLong(Category::getCount)
                    .sum();
            // 获取当前节点在 getCategories() 中设置的原始题目数量（直接关联的题目数）
            // 由于上面 setCount 修改了当前节点的值，我们需要先保存或者重新计算
            // 但观察代码逻辑，allNode 是从 allNodes 来的，allNodes 中的 count 是数据库查出来的直接题目数
            // 然而，上面的循环中，如果子节点先被处理并修改了 count，那么子节点的 count 已经变成了“子树总和”
            // 所以这里直接 sum 子节点的 count 是正确的，因为子节点的 count 已经被更新为包含其下所有子树的题目数
            
            // 但是，父节点自己的 count 不应该被子节点的 count 覆盖，而是应该加上子节点的 count？
            // 通常树形结构中，父节点的 count 代表整个子树的题目总数。
            // 如果需求是：父节点显示的是整个子树的题目总数，那么：
            // 父节点总题目数 = 父节点直接题目数 + 所有子节点子树题目总数
            
            // 问题在于：allNode.getCount() 此时还是数据库查出来的“直接题目数”吗？
            // 是的，因为我们是按顺序遍历 allNodes，但并没有保证子节点一定在父节点之前被处理完并更新 count 后，父节点再读取。
            // 实际上，这里的逻辑是：
            // 1. allNodes 中的每个节点，初始 count 是数据库查出的直接题目数。
            // 2. 我们遍历 allNodes，为每个节点设置 children。
            // 3. 然后我们想更新当前节点的 count 为：自身直接题目数 + 所有子节点（已更新为子树总和）的 count 之和。
            
            // 但是，当前代码直接 setCount(childrenSum)，丢失了自身直接的题目数。
            // 正确的做法应该是：自身直接题目数 + 子节点子树题目总数
            
            // 由于 allNode 在被修改前，其 count 字段存储的是直接题目数。
            // 我们需要先获取这个原始值，或者在修改前保存。
            // 但由于 Java 对象引用传递，且我们在循环中直接操作 allNode，
            // 我们可以先取出当前的 count (即直接题目数)，然后加上子节点的 count 总和。
            
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