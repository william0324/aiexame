package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.entity.Question;
import org.example.mapper.QuestionMapper;
import org.example.service.QuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 题目Service实现类
 * 实现题目相关的业务逻辑
 */
@Slf4j
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {


    @Override
    public Page<Question> getQuestionList(Integer page, Integer size, Long categoryId, String difficulty, String type, String keyword) {
        // 分页查询
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(categoryId != null, Question::getCategoryId, categoryId);
        queryWrapper.eq(difficulty != null, Question::getDifficulty, difficulty);
        queryWrapper.eq(type != null, Question::getType, type);
        queryWrapper.like(keyword != null, Question::getTitle, keyword);
        queryWrapper.eq(Question::getIsDeleted, 0);
        Page<Question> pageInfo = new Page<>(page, size);
        Page<Question> pageResult = this.page(pageInfo, queryWrapper);
        log.info("pageResult: {}", pageResult);
        return pageResult;
    }
}