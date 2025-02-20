package com.accounts;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/accounts")
public class AccountsResource {

    public static AccountService accountService = new AccountService();

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(String json) {
        return accountService.newUser(json);
    }

    @GET
    @Path("/search/{id}")
    public Response search(@PathParam("id") String id) {
        return accountService.retrieveUser(id, true);
    }

    @DELETE
    @Path("/delete/{id}")
    public Response delete(@PathParam("id") String id) {
        return accountService.deleteUser(id);
    }

    @PUT
    @Path("/update/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") String id, String accountJson) {
        return accountService.updateUser(accountJson, id);
    }

    @GET
    @Path("/admin")
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("admin")
    public Response adminOnly() {
        return Response.ok("Access granted to admin").build();
    }
}
