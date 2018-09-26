package com.revolut.task.http;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;

@Provider
public class GeneralExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {
        HashMap<String, String> map = new HashMap<>();
        map.put("error", e.getMessage());
        return Response.serverError().entity(map).build();
    }
}
