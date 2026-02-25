package org.example.mapper;


import org.apache.ibatis.annotations.MapKey;
import org.example.entity.Question;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 题目Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础的CRUD操作
 */
public interface QuestionMapper extends BaseMapper<Question> {

    @MapKey("categoryId")
    List<Map<Long, Long>> selectCountByCategoryId();
}