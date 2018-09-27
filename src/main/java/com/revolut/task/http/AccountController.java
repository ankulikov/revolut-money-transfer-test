package com.revolut.task.http;

import com.revolut.task.di.InjectorProvider;
import com.revolut.task.model.Account;
import com.revolut.task.service.api.AccountService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController() {
        accountService = InjectorProvider.provide().getInstance(AccountService.class);
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Account getAccount(@PathParam("id") String accountId) {
        return accountService.getAccount(Long.valueOf(accountId));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Account createAccount(@QueryParam("currency") String currency) {
        if (currency != null) {
            return accountService.createAccount(currency);
        } else
            return accountService.createAccount();
    }

    @POST
    @Path("{id}/lock")
    public Response lockAccount(@PathParam("id") String accountId) {
        accountService.lockAccount(Long.valueOf(accountId));
        return Response.noContent().build();
    }

    @POST
    @Path("{id}/unlock")
    public Response unlockAccount(@PathParam("id") String accountId) {
        accountService.unlockAccount(Long.valueOf(accountId));
        return Response.noContent().build();
    }

    @DELETE
    @Path("{id}")
    public Response removeAccount(@PathParam("id") String accountId) {
        accountService.removeAccount(Long.valueOf(accountId));
        return Response.noContent().build();
    }


}
