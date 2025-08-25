package com.yong2gether.ywave.global.exception;

import com.yong2gether.ywave.user.service.UserService;
import com.yong2gether.ywave.mypage.service.BookmarkGroupCommandService;
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

    @ExceptionHandler(BookmarkGroupCommandService.GroupNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGroupNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("GROUP_NOT_FOUND", "요청한 그룹을 찾을 수 없습니다."));
    }

    @ExceptionHandler(BookmarkGroupCommandService.NotOwnerOfGroupException.class)
    public ResponseEntity<ErrorResponse> handleNotOwner() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("FORBIDDEN", "해당 그룹에 대한 권한이 없습니다."));
    }

    @ExceptionHandler(BookmarkGroupCommandService.CannotDeleteDefaultGroupException.class)
    public ResponseEntity<ErrorResponse> handleCannotDeleteDefault() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("CANNOT_DELETE_DEFAULT_GROUP", "기본 그룹은 삭제할 수 없습니다."));
    }

    public record ErrorResponse(String code, String message) {}
}
