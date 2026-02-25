package org.example.service.impl;

import jakarta.annotation.Resource;
import org.example.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CategoryServiceImplTest {

    @Resource
    private CategoryService categoryService;

    @Test
    void getCategories() {
        categoryService.getCategories();
    }
}