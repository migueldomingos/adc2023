package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import pt.unl.fct.di.apdc.firstwebapp.util.TokenData;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/token")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class TokenResource {
    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    private final Gson g = new Gson();
    private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());

    public TokenResource() {}

    @GET
    @Path("/show/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response showToken (@PathParam("username") String username) {
        Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(username);
        Entity ent = datastore.get(tokenKey);

        if (ent == null) {
            LOG.warning("Token does not exist in database");
            return Response.status(Response.Status.FORBIDDEN).entity("User does not exist.").build();
        }

        TokenData token = new TokenData(ent.getString("token_username"), ent.getString("token_id"),
                ent.getLong("token_creation"), ent.getLong("token_expiration"));

        return Response.ok(g.toJson(token)).build();
    }
}
