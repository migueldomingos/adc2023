package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.logging.Logger;

@Path("/logout") //
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    private final Gson g = new Gson();

    public LogoutResource() {} //construtor

    @POST
    @Path("/{username}")
    public Response logOutUser(@PathParam("username") String username) {
        Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(username);
        Entity token = datastore.get(tokenKey);

        if (token != null) {
            datastore.delete(tokenKey);
            return Response.status(Status.OK).entity("User logged out.").build();
        }

        return Response.status(Status.BAD_REQUEST).entity("User already logged out.").build();
    }
}