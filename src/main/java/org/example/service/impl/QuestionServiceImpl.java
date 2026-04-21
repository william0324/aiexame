package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.entity.Question;
import org.example.mapper.QuestionMapper;
import org.example.service.QuestionService;
import org.example.vo.QuestionQueryVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 题目Service实现类
 * 实现题目相关的业务逻辑
 */
@Slf4j
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {


    @Override
    public Page<Question> getQuestionList(QuestionQueryVo queryVo) {
        // 参数校验和默认值设置
        int page = queryVo.getPage() != null && queryVo.getPage() > 0 ? queryVo.getPage() : 1;
        int size = queryVo.getSize() != null && queryVo.getSize() > 0 ? queryVo.getSize() : 10;
        
        // 构建查询条件
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(queryVo.getCategoryId() != null, Question::getCategoryId, queryVo.getCategoryId());
        queryWrapper.eq(StringUtils.hasText(queryVo.getDifficulty()), Question::getDifficulty, queryVo.getDifficulty());
        queryWrapper.eq(StringUtils.hasText(queryVo.getType()), Question::getType, queryVo.getType());
        queryWrapper.like(StringUtils.hasText(queryVo.getKeyword()), Question::getTitle, queryVo.getKeyword());
        queryWrapper.eq(Question::getIsDeleted, 0);
        queryWrapper.orderByDesc(Question::getCreateTime);
        
        // 执行分页查询
        Page<Question> pageInfo = new Page<>(page, size);
        Page<Question> pageResult = this.page(pageInfo, queryWrapper);
        
        log.info("分页查询题目列表 - 页码: {}, 每页: {}, 总数: {}", page, size, pageResult.getTotal());
        return pageResult;
    }
}