package com.revolut.task.http;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.revolut.task.model.Error;
import org.slf4j.Logger;

import javax.annotation.Priority;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.status;

@Provider
@Priority(1000)
public class JsonMappingExceptionResponse implements ExceptionMapper<JsonMappingException> {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(JsonMappingExceptionResponse.class);

    @Override
    public Response toResponse(JsonMappingException exception) {
        log.warn("Invalid JSON request", exception);
        return status(Response.Status.BAD_REQUEST)
                .entity(new Error("Invalid JSON request"))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }
}