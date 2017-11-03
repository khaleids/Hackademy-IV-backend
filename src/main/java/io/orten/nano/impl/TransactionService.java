package io.orten.nano.impl;

import io.orten.nano.model.Donor;
import io.orten.nano.model.Project;
import io.orten.nano.model.Transaction;
import io.orten.nano.util.Database;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.util.List;

public class TransactionService {

    public Response getSumOfAmountFunded(int projectID){
        try{
            double sumOfAmountFunded=Database.getSumOfAmountFunded(projectID);
            return Response.ok(sumOfAmountFunded).build();
        }catch (Exception e){
            System.out.println(e.getMessage());
            return Response.status(HttpServletResponse.SC_OK).entity(e.getMessage()).build();
            //return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
        }
    }
    public Response getListOfTransaction(){
        try{
            List<Donor> donors=Database.getListOfTransaction();
            return Response.ok(donors).build();
        }catch (Exception e){
            return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
        }

    }
}
