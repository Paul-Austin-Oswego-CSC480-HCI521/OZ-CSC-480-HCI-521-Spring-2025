package com.accounts;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/accounts")
public class AccountsResource {

    @GET
    @Path("/id")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAccount() {
        return "{\"Account\": \"Insert account info here <-.\"}";
    }
}
