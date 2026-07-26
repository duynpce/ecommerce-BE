package org.example.userservice.infrastructure.web.exception;

import org.example.userservice.domain.exception.*;
import org.example.userservice.infrastructure.web.dto.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ResponseDto<Void>> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ResponseDto.failure(message));
    }

    // ── 400 - Validation (@Valid) ────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, message);
    }

    // ── 400 - Illegal argument ───────────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDto<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ── 401 - Unauthorized ───────────────────────────────────────────────────

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ResponseDto<Void>> handleUnauthorized(UnauthorizedException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // ── 403 - Forbidden ──────────────────────────────────────────────────────

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ResponseDto<Void>> handleForbidden(ForbiddenException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // ── 404 - Not Found ──────────────────────────────────────────────────────

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseDto<Void>> handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── 409 - Conflict ───────────────────────────────────────────────────────

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ResponseDto<Void>> handleConflict(ConflictException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ResponseDto<Void>> handleExternal(ExternalServiceException ex) {
        return build(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ResponseDto<Void>> handleNullPointer(NullPointerException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}