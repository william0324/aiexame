package org.example.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Question;
import org.example.service.QuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class QuestionServiceImplTest {

    @Resource
    private QuestionService questionService;

    @Test
    void getQuestionList() {
        Page<Question> questionList = questionService.getQuestionList(1, 10, 14L, null, null, null);
        log.info("questionList: {}", questionList);
    }
}