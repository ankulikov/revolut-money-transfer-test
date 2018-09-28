package com.revolut.task.http;

import com.revolut.task.model.Error;
import com.revolut.task.model.exceptions.AccountNotFoundException;
import com.revolut.task.model.exceptions.MoneyNegativeAmountException;
import org.slf4j.Logger;

import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status;
import static javax.ws.rs.core.Response.status;

@Provider
public class GeneralExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(GeneralExceptionMapper.class);

    @Override
    public Response toResponse(Exception e) {
        log.warn("Serializing error to JSON...", e);
        Status status = Status.INTERNAL_SERVER_ERROR;
        String message = e.getMessage();
        if (e instanceof WebApplicationException) {
            int statusCode = ((WebApplicationException) e).getResponse().getStatus();
            status = Status.fromStatusCode(statusCode);
            if (e instanceof NotFoundException) {
                message = "Can't find requested resource";
            } else if (e instanceof NotAllowedException) {
                message = "Requested resource is not allowed by specified method";
            }
        } else if (e instanceof AccountNotFoundException) {
            status = Status.NOT_FOUND;
        } else if (e instanceof MoneyNegativeAmountException) {
            status = Status.BAD_REQUEST;
        }
        return status(status)
                .entity(new Error(message))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }
}
