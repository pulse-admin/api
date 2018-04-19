package gov.ca.emsa.pulse.service;

import java.sql.SQLException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import gov.ca.emsa.pulse.common.domain.ErrorJSONObject;

@ControllerAdvice
public class ApiExceptionControllerAdvice {

    @ExceptionHandler(AcfChangesNotAllowedException.class)
    public ResponseEntity<ErrorJSONObject> exception(AcfChangesNotAllowedException e) {
        e.printStackTrace();
        String msg = e.getMessage();
        if (StringUtils.isEmpty(msg)) {
            msg = "ACF changes are not allowed.";
        }
        return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(msg), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidArgumentsException.class)
    public ResponseEntity<ErrorJSONObject> exception(InvalidArgumentsException e) {
        e.printStackTrace();
        return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErrorJSONObject> exception(SQLException e) {
        e.printStackTrace();
        return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorJSONObject> exception(Exception e) {
        e.printStackTrace();
        return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ErrorJSONObject> exception(AuthenticationCredentialsNotFoundException e) {
        e.printStackTrace();
        return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject("Access denied: " + e.getMessage()),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorJSONObject> exception(AccessDeniedException e) {
        e.printStackTrace();
        return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject("Access denied: " + e.getMessage()),
                HttpStatus.UNAUTHORIZED);
    }

}
