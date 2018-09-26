package com.revolut.task.http;

import com.revolut.task.di.InjectorProvider;
import com.revolut.task.model.Account;
import com.revolut.task.service.api.AccountService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Account createAccount(Map body) {
        if (body != null && body.containsKey("currency")) {
            return accountService.createAccount((String) body.get("currency"));
        } else
            return accountService.createAccount();
    }

    @POST
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String lockAccount(@PathParam("id") String accountId) {
        accountService.lockAccount(Long.valueOf(accountId));
        return "{\"locked\": true}";
    }
}
