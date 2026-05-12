package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.common.Result;
import org.example.entity.Question;
import org.example.entity.QuestionAnswer;
import org.example.entity.QuestionChoice;
import org.example.mapper.QuestionAnswerMapper;
import org.example.mapper.QuestionChoiceMapper;
import org.example.mapper.QuestionMapper;
import org.example.service.QuestionService;
import org.example.vo.QuestionQueryVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@DisplayName("题目创建功能测试")
class QuestionServiceImplTest {

    @MockBean
    private QuestionMapper questionMapper;

    @MockBean
    private QuestionAnswerMapper questionAnswerMapper;

    @MockBean
    private QuestionChoiceMapper questionChoiceMapper;

    @MockBean(name = "questionHotExecutor")
    private ThreadPoolTaskExecutor questionHotExecutor;

    @Resource
    private QuestionService questionService;

    @BeforeEach
    void setUp() throws Exception {
        questionService = new QuestionServiceImpl();

        // 使用反射注入Mock对象
        injectMock(questionService, "questionMapper", questionMapper);
        injectMock(questionService, "questionAnswerMapper", questionAnswerMapper);
        injectMock(questionService, "questionChoiceMapper", questionChoiceMapper);
        injectMock(questionService, "questionHotExecutor", questionHotExecutor);
    }

    /**
     * 使用反射注入Mock对象
     */
    private void injectMock(Object target, String fieldName, Object mockObject) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mockObject);
    }

    /**
     * 创建基础的选择题测试数据
     */
    private Question createBaseChoiceQuestion() {
        Question question = new Question();
        question.setTitle("Java中以下哪个关键字用于定义类？");
        question.setType("CHOICE");
        question.setMulti(false);
        question.setCategoryId(1L);
        question.setDifficulty("EASY");
        question.setScore(5);
        question.setAnalysis("class是Java中用于定义类的关键字。");
        return question;
    }

    /**
     * 创建选择题的选项列表
     */
    private List<QuestionChoice> createChoiceOptions() {
        List<QuestionChoice> choices = new ArrayList<>();

        QuestionChoice optionA = new QuestionChoice();
        optionA.setContent("public");
        optionA.setIsCorrect(false);
        choices.add(optionA);

        QuestionChoice optionB = new QuestionChoice();
        optionB.setContent("class");
        optionB.setIsCorrect(true);
        choices.add(optionB);

        QuestionChoice optionC = new QuestionChoice();
        optionC.setContent("static");
        optionC.setIsCorrect(false);
        choices.add(optionC);

        QuestionChoice optionD = new QuestionChoice();
        optionD.setContent("void");
        optionD.setIsCorrect(false);
        choices.add(optionD);

        return choices;
    }

    /**
     * 创建多选题的选项列表
     */
    private List<QuestionChoice> createMultipleChoiceOptions() {
        List<QuestionChoice> choices = new ArrayList<>();

        QuestionChoice optionA = new QuestionChoice();
        optionA.setContent("封装");
        optionA.setIsCorrect(true);
        choices.add(optionA);

        QuestionChoice optionB = new QuestionChoice();
        optionB.setContent("继承");
        optionB.setIsCorrect(true);
        choices.add(optionB);

        QuestionChoice optionC = new QuestionChoice();
        optionC.setContent("多态");
        optionC.setIsCorrect(true);
        choices.add(optionC);

        QuestionChoice optionD = new QuestionChoice();
        optionD.setContent("编译");
        optionD.setIsCorrect(false);
        choices.add(optionD);

        return choices;
    }

    /**
     * 创建判断题测试数据
     */
    private Question createJudgeQuestion() {
        Question question = new Question();
        question.setTitle("Java是一种纯面向对象编程语言。");
        question.setType("JUDGE");
        question.setCategoryId(1L);
        question.setDifficulty("EASY");
        question.setScore(2);
        question.setAnalysis("Java支持基本数据类型，因此不是纯面向对象语言。");
        return question;
    }

    /**
     * 创建简答题测试数据
     */
    private Question createTextQuestion() {
        Question question = new Question();
        question.setTitle("请简述Java中的多态性是什么？");
        question.setType("TEXT");
        question.setCategoryId(1L);
        question.setDifficulty("MEDIUM");
        question.setScore(10);
        question.setAnalysis("多态性是面向对象的三大特性之一...");
        return question;
    }


    @Test
    void getQuestionList() {
        QuestionQueryVo queryVo = new QuestionQueryVo();
        queryVo.setPage(1);
        queryVo.setSize(10);
//        queryVo.setCategoryId(14L);
        
        Page<Question> questionList = questionService.getQuestionList(queryVo);
        log.info("questionList: {}", questionList);
    }

    @Test
    void getQuestionById() throws InterruptedException {
        Question questionById = questionService.getQuestionById(71L);
        log.info("questionById: {}", questionById);
        Thread.sleep(3000);
    }

    @Test
    void createQuestion() {
        Question existingQuestion = new Question();
        existingQuestion.setTitle("Java中以下哪个关键字用于定义类？");
        existingQuestion.setType("CHOICE");
        existingQuestion.setCategoryId(1L);
        existingQuestion.setDifficulty("EASY");
        existingQuestion.setScore(5);

        questionService.createQuestion(existingQuestion);
    }

    @Test
    @DisplayName("测试创建单选题 - 成功")
    void testCreateSingleChoiceQuestion_Success() {
        // 准备测试数据
        Question question = createBaseChoiceQuestion();
        List<QuestionChoice> choices = createChoiceOptions();
        question.setChoices(choices);

        // Mock行为
        when(questionMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);
        when(questionMapper.insert(any(Question.class))).thenAnswer(invocation -> {
            Question q = invocation.getArgument(0);
            q.setId(1L); // 模拟生成ID
            return 1;
        });
        when(questionChoiceMapper.insert(any(QuestionChoice.class))).thenReturn(1);
        when(questionAnswerMapper.insert(any(QuestionAnswer.class))).thenReturn(1);

        // 执行测试
        Result<String> result = questionService.createQuestion(question);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("添加成功!", result.getMessage());

        // 验证调用次数
        verify(questionMapper, times(1)).exists(any(LambdaQueryWrapper.class));
        verify(questionMapper, times(1)).insert(any(Question.class));
        verify(questionChoiceMapper, times(4)).insert(any(QuestionChoice.class));
        verify(questionAnswerMapper, times(1)).insert(any(QuestionAnswer.class));
    }

}