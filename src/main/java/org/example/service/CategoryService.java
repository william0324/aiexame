package org.example.service;

import org.example.common.Result;
import org.example.entity.Category;

import java.util.List;

public interface CategoryService {


    List<Category> getCategories();

    List<Category> getCategoryTree();

    Result<Void> addCategory(Category category);

    Result<Void> updateCategory(Category category);

    Result<Void> deleteCategory(Long id);
}