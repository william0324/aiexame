package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.common.CacheConstants;
import org.example.common.Result;
import org.example.entity.Category;
import org.example.entity.Question;
import org.example.entity.QuestionAnswer;
import org.example.entity.QuestionChoice;
import org.example.mapper.CategoryMapper;
import org.example.mapper.QuestionAnswerMapper;
import org.example.mapper.QuestionChoiceMapper;
import org.example.mapper.QuestionMapper;
import org.example.service.QuestionService;
import org.example.utils.RedisUtil;
import org.example.vo.QuestionQueryVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * 题目Service实现类
 * 实现题目相关的业务逻辑
 */
@Slf4j
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private QuestionAnswerMapper questionAnswerMapper;

    @Resource
    private QuestionChoiceMapper questionChoiceMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private RedisUtil redisUtil;

    @Resource(name = "questionHotExecutor")
    private ThreadPoolTaskExecutor questionHotExecutor;

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

        // 嵌套查询
        List<Question> records = pageResult.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return pageResult;
        }

        List<Long> questionIds = records.stream().map(Question::getId).toList();
        List<Long> categoryIds = records.stream().map(Question::getCategoryId).distinct().toList();

        // 查询三次数据库，得到三个列表
        List<QuestionAnswer> answers = questionAnswerMapper.selectList(
                new LambdaQueryWrapper<QuestionAnswer>()
                        .eq(QuestionAnswer::getIsDeleted, 0)
                        .in(QuestionAnswer::getQuestionId, questionIds));
        List<QuestionChoice> choices = questionChoiceMapper.selectList(
                new LambdaQueryWrapper<QuestionChoice>()
                        .eq(QuestionChoice::getIsDeleted, 0)
                        .in(QuestionChoice::getQuestionId, questionIds));

        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getIsDeleted, 0)
                        .in(Category::getId, categoryIds));

        Map<Long, List<QuestionAnswer>> answerMap = answers.stream().collect(Collectors.groupingBy(QuestionAnswer::getQuestionId));
        Map<Long, List<QuestionChoice>> choiceMap = choices.stream().collect(Collectors.groupingBy(QuestionChoice::getQuestionId));
        Map<Long, Category> categoryMap = categories.stream().collect(Collectors.toMap(Category::getId, v -> v));

        records.forEach(question -> {
            Long questionId = question.getId();
            Long categoryId = question.getCategoryId();
            question.setAnswer(answerMap.get(questionId).get(0));
            question.setChoices(choiceMap.get(questionId));
            question.setCategory(categoryMap.get(categoryId));
        });
        pageResult.setRecords(records);
        log.info("分页查询题目列表 - 页码: {}, 每页: {}, 总数: {}", page, size, pageResult.getTotal());
        return pageResult;
    }

    @Override
    public Question getQuestionById(Long id) {
        Question question = questionMapper.getQuestionById(id);
        if (question == null) {
            throw new RuntimeException("查询题目失败!");
        }
        // 开启一个线程记录热点题目
        questionHotExecutor.execute(() -> {
            try {
                double zIncrementScore = redisUtil.zIncrementScore(CacheConstants.QUESTION_CACHE, id, 1);
                log.info("记录题目：{}，当前分数：{}", id, zIncrementScore);
            } catch (Exception e) {
                log.error("记录热点题目失败，questionId={}", id, e);
            }
        });
        return question;
    }

    @Override
    public Result<String> createQuestion(Question question) {
        // 同一个类型不能题目title相同
        LambdaQueryWrapper<Question> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Question::getType, question.getType());
        lambdaQueryWrapper.eq(Question::getTitle, question.getTitle());
        lambdaQueryWrapper.eq(Question::getIsDeleted, 0);
        boolean exists = questionMapper.exists(lambdaQueryWrapper);
        if (exists) {
            log.info("在类型为{}的题目中，已经存在名为{}的题目,创建失败!", question.getType(), question.getTitle());
            return Result.error("在类型为%s的题目中，已经存在名为%s的题目,创建失败!".formatted(question.getType(), question.getTitle()));
        }



        return null;
    }
}