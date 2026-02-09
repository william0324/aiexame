package org.example.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.info("服务器发生运行时异常！异常信息为：{}", e.getMessage());
        return Result.error("服务器发生运行时异常！");
    }
}
