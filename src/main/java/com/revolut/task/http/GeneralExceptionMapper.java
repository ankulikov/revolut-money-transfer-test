package com.revolut.task.http;

import com.revolut.task.model.Error;
import com.revolut.task.model.exceptions.AccountNotFoundException;
import com.revolut.task.model.exceptions.MoneyNegativeAmountException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status;
import static javax.ws.rs.core.Response.status;

@Provider
public class GeneralExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {
        Status status = Status.INTERNAL_SERVER_ERROR;
        if (e instanceof AccountNotFoundException) {
            status = Status.NOT_FOUND;
        } else if (e instanceof MoneyNegativeAmountException) {
            status = Status.BAD_REQUEST;
        }
        return status(status)
                .entity(new Error(e.getMessage()))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }
}
