package com.trithuc.exception;

import com.trithuc.errors.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalException {


    @ExceptionHandler(value = TravelException.class)
    public ResponseEntity<Object> TravelContentExceptionHandler(TravelException exception) {
        ErrorResponse response = new ErrorResponse(exception.getErrorCode(), exception.getMessage());
        return ResponseEntity.status(exception.getHttpStatusCode()).body(response);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException exception) {
        ErrorResponse response = new ErrorResponse("url_not_match", "The requested URL was not found on this server.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
