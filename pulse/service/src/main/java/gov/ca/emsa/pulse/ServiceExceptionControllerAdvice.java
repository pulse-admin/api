package gov.ca.emsa.pulse;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import gov.ca.emsa.pulse.auth.user.UserRetrievalException;
import gov.ca.emsa.pulse.common.domain.ErrorJSONObject;
import gov.ca.emsa.pulse.service.BrokerError;

@ControllerAdvice
public class ServiceExceptionControllerAdvice {
    private static final Logger logger = LogManager.getLogger(ServiceExceptionControllerAdvice.class);

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErrorJSONObject> exception(HttpServerErrorException e) {
        String responseBody = e.getResponseBodyAsString();
        String errorMessage = responseBody;
        ObjectReader reader = new ObjectMapper().reader().forType(BrokerError.class);
        try {
            BrokerError brokerError = reader.readValue(responseBody);
            errorMessage = StringUtils.isEmpty(brokerError.getMessage()) ? brokerError.getError()
                    : brokerError.getMessage();
        } catch (IOException ex) {
            logger.warn("Could not turn " + responseBody + " into BrokerError object", ex);
        }
        logger.error(errorMessage);

        return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(errorMessage), e.getStatusCode());
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorJSONObject> exception(HttpClientErrorException e) {
        String responseBody = e.getResponseBodyAsString();
        String errorMessage = responseBody;
        ObjectReader reader = new ObjectMapper().reader().forType(BrokerError.class);
        try {
            BrokerError brokerError = reader.readValue(responseBody);
            errorMessage = brokerError.getError();
        } catch (IOException ex) {
            logger.warn("Could not turn " + responseBody + " into BrokerError object", ex);
        }
        logger.error(errorMessage);

        return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(errorMessage), e.getStatusCode());
    }

    @ExceptionHandler(UserRetrievalException.class)
    public ResponseEntity<ErrorJSONObject> exception(UserRetrievalException e) {
        logger.error(e.getMessage(), e);
        return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(e.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorJSONObject> exception(ResourceAccessException e) {
        logger.error(e.getMessage(), e);
        return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorJSONObject> exception(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return new ResponseEntity<ErrorJSONObject>(
                new ErrorJSONObject("Unknown error. Message was: " + ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ErrorJSONObject> exception(AuthenticationCredentialsNotFoundException ex) {
        logger.error(ex.getMessage(), ex);
        return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject("Access denied: " + ex.getMessage()),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorJSONObject> exception(AccessDeniedException e) {
        e.printStackTrace();
        return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject("Access denied: " + e.getMessage()),
                HttpStatus.UNAUTHORIZED);
    }
}
