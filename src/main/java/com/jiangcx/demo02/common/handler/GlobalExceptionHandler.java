package com.jiangcx.demo02.common.handler;

import com.jiangcx.demo02.common.exception.BusinessException;
import com.jiangcx.demo02.common.model.api.ApiResponse;
import com.jiangcx.demo02.common.enums.ResponseCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理系统中的各种异常，返回标准的错误响应
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param ex 业务异常
     * @return 响应对象
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: code={}, message={}", ex.getErrorCode(), ex.getErrorMessage());
        return ApiResponse.error(ex.getErrorCode(), ex.getErrorMessage());
    }

    /**
     * 处理方法参数校验异常 (@RequestBody @Valid)
     *
     * @param ex 参数校验异常
     * @return 响应对象
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        String errorMessage = errors.isEmpty() ? "参数验证失败" : String.join(", ", errors);
        log.warn("参数校验异常: {}", errorMessage);

        return ApiResponse.error(ResponseCode.PARAM_ERROR.getCode(), errorMessage);
    }

    /**
     * 处理对象绑定校验异常 (@Valid)
     *
     * @param ex 绑定校验异常
     * @return 响应对象
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBindException(BindException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        String errorMessage = errors.isEmpty() ? "参数绑定失败" : String.join(", ", errors);
        log.warn("参数绑定异常: {}", errorMessage);

        return ApiResponse.error(ResponseCode.PARAM_ERROR.getCode(), errorMessage);
    }

    /**
     * 处理约束校验异常 (@NotNull, @NotBlank等)
     *
     * @param ex 约束校验异常
     * @return 响应对象
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleConstraintViolationException(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

        log.warn("约束校验异常: {}", errorMessage);
        return ApiResponse.error(ResponseCode.PARAM_ERROR.getCode(), errorMessage);
    }

    /**
     * 处理空指针异常
     *
     * @param ex 空指针异常
     * @return 响应对象
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleNullPointerException(NullPointerException ex) {
        log.error("空指针异常: ", ex);
        return ApiResponse.error(ResponseCode.SYSTEM_ERROR.getCode(), "系统内部错误");
    }

    /**
     * 处理其他未捕获的异常
     *
     * @param ex 异常
     * @return 响应对象
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception ex) {
        log.error("系统异常: ", ex);
        return ApiResponse.error(ResponseCode.SYSTEM_ERROR.getCode(), "系统繁忙，请稍后重试");
    }
}