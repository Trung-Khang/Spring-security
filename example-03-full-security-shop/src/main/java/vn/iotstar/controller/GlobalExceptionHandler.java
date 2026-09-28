package vn.iotstar.controller;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String badRequest(IllegalArgumentException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        return "error";
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String serviceUnavailable(IllegalStateException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        return "error";
    }
}
