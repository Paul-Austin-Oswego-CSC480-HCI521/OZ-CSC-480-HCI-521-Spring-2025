package com.moderation;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

@Path("/report")
public class ReportResource {

    // setup Report Cluster in MongoDB
    // connect to Report cluster

    @POST
    @Path("/id")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response reportQuote(ReportRequest reportRequest) {
        String quoteID = reportRequest.getQuoteID();
        String jsonResponse = "{\"Report\": \"You made a report on quote: " + quoteID + "\"}";
        return Response
                .ok(jsonResponse)
                .location(UriBuilder.fromPath("/report/id").build())
                .build();
    }

}
