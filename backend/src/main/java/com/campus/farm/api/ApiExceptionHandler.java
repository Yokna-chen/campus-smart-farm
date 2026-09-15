package com.campus.farm.api;

import com.campus.farm.irrigation.IrrigationBadRequestException;
import com.campus.farm.irrigation.IrrigationConflictException;
import com.campus.farm.irrigation.IrrigationForbiddenException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(IrrigationBadRequestException.class)
  public ResponseEntity<Map<String, Object>> badRequest(IrrigationBadRequestException e) { return response(HttpStatus.BAD_REQUEST, e.getMessage()); }
  @ExceptionHandler(IrrigationForbiddenException.class)
  public ResponseEntity<Map<String, Object>> forbidden(IrrigationForbiddenException e) { return response(HttpStatus.FORBIDDEN, e.getMessage()); }
  @ExceptionHandler(IrrigationConflictException.class)
  public ResponseEntity<Map<String, Object>> conflict(IrrigationConflictException e) { return response(HttpStatus.CONFLICT, e.getMessage()); }
  private ResponseEntity<Map<String, Object>> response(HttpStatus status, String message) {
    Map<String, Object> body = new LinkedHashMap<>(); body.put("status", status.value()); body.put("error", status.getReasonPhrase()); body.put("message", message);
    return ResponseEntity.status(status).body(body);
  }
}
