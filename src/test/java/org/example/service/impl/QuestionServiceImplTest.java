package org.example.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Question;
import org.example.service.QuestionService;
import org.example.vo.QuestionQueryVo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class QuestionServiceImplTest {

    @Resource
    private QuestionService questionService;

    @Test
    void getQuestionList() {
        QuestionQueryVo queryVo = new QuestionQueryVo();
        queryVo.setPage(1);
        queryVo.setSize(10);
        queryVo.setCategoryId(14L);
        
        Page<Question> questionList = questionService.getQuestionList(queryVo);
        log.info("questionList: {}", questionList);
    }
}