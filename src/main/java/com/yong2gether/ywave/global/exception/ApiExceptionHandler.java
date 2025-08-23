package com.yong2gether.ywave.global.exception;

import com.yong2gether.ywave.user.service.UserService;
import com.yong2gether.ywave.mypage.service.BookmarkGroupCommandService;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UserService.DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(UserService.DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("DUPLICATE_EMAIL", ex.getMessage()));
    }

    @ExceptionHandler(BookmarkGroupCommandService.DuplicateGroupNameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateGroup(BookmarkGroupCommandService.DuplicateGroupNameException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("DUPLICATE_GROUP_NAME", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalid(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String msg = fieldErrors.isEmpty() ? "요청 값이 유효하지 않습니다."
                : fieldErrors.get(0).getField() + ": " + fieldErrors.get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_REQUEST", msg));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_REQUEST", e.getMessage()));
    }

    public record ErrorResponse(String code, String message) {}
}
