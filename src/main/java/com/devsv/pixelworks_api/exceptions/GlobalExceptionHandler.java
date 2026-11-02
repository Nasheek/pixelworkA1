package com.devsv.pixelworks_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> manejarImagenDemasiadoGrande(MaxUploadSizeExceededException ex) {
        return new ResponseEntity<>(new ErrorResponse("La imagen supera el limite de 10 MB."), HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> manejarResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> manejarIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> manejarSolicitudInvalida(Exception ex) {
        return new ResponseEntity<>(new ErrorResponse("La solicitud contiene datos inválidos."), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarGlobalException(Exception ex) {
        if (ex instanceof org.springframework.web.ErrorResponse error) {
            return new ResponseEntity<>(
                    new ErrorResponse("La solicitud no es válida para este recurso."),
                    error.getHeaders(), error.getStatusCode());
        }
        return new ResponseEntity<>(
                new ErrorResponse("Ocurrió un error inesperado en el servidor."),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("mensaje", "No tienes los permisos necesarios para realizar esta acción."));
    }
}
