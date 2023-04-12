package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;

import com.google.gson.Gson;
import pt.unl.fct.di.apdc.firstwebapp.util.*;

@Path("/list")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ListUsersResource {
    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    private static final Logger LOG = Logger.getLogger(ListUsersResource.class.getName());
    private final Gson g = new Gson();

    public ListUsersResource() {}

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUsers(@PathParam("username") String username) {
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
        Entity user = datastore.get(userKey);

        if (user == null)
            return Response.status(Status.BAD_REQUEST).entity("User does not exist.").build();

        Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(username);
        Entity token = datastore.get(tokenKey);

        LOG.warning(String.valueOf(token));

        Transaction txn = datastore.newTransaction();

        try{
            if (token == null) {
                LOG.warning("token was null");
                txn.rollback();
                return Response.status(Status.FORBIDDEN).entity("User not logged in.").build();
            }
            else if (token.getLong("token_expiration") < Timestamp.now().toDate().getTime()) {
                LOG.warning("token expired");
                txn.delete(tokenKey);
                txn.commit();
                return Response.status(Status.FORBIDDEN).entity("User not logged in.").build();
            }
        } finally {
            if (txn.isActive())
                txn.rollback();
        }

        QueryResults<Entity> usersIt = listUsersIt();
        List<UserClass> listU = new LinkedList<>();

        if (user.getString("user_role").equals("USER")) {
            while (usersIt.hasNext()) {
                Entity next = usersIt.next();

                if (next.getString("user_role").equals("USER") && next.getLong("user_state") == 1 && next.getString("user_perfil").equals("PUBLIC")) {
                    listU.add(new UserClass(next.getString("user_username"), next.getString("user_email"), next.getString("user_name")));
                }
            }
        } else if (user.getString("user_role").equals("GBO")) {
            while (usersIt.hasNext()) {
                Entity next = usersIt.next();

                if (next.getString("user_role").equals("USER"))
                    listU.add(new UserClass(next.getString("user_username"), next.getString("user_pwd") ,next.getString("user_email"), next.getString("user_name"), next.getString("user_phone_num"),
                            next.getString("user_perfil"), next.getString("user_mobile_num"), next.getLong("user_state"),next.getString("user_ocupation"), next.getString("user_workspace"),
                            next.getString("user_addr"), next.getString("user_postal"), next.getString("user_NIF"), next.getString("user_role")));
            }
        } else if (user.getString("user_role").equals("GS")) {
            while (usersIt.hasNext()) {
                Entity next = usersIt.next();

                if (next.getString("user_role").equals("USER") || next.getString("user_role").equals("GBO"))
                    listU.add(new UserClass(next.getString("user_username"), next.getString("user_pwd") ,next.getString("user_email"), next.getString("user_name"), next.getString("user_phone_num"),
                            next.getString("user_perfil"), next.getString("user_mobile_num"),next.getLong("user_state") ,next.getString("user_ocupation"), next.getString("user_workspace"),
                            next.getString("user_addr"), next.getString("user_postal"), next.getString("user_NIF"), next.getString("user_role")));
            }
        } else {
            while (usersIt.hasNext()) {
                Entity next = usersIt.next();

                listU.add(new UserClass(next.getString("user_username"), next.getString("user_pwd") ,next.getString("user_email"), next.getString("user_name"), next.getString("user_phone_num"),
                        next.getString("user_perfil"), next.getString("user_mobile_num"),next.getLong("user_state") ,next.getString("user_ocupation"), next.getString("user_workspace"),
                        next.getString("user_addr"), next.getString("user_postal"), next.getString("user_NIF"), next.getString("user_role")));
            }
        }

        return Response.ok(g.toJson(listU)).build();

    }

    private QueryResults<Entity> listUsersIt() {
        Query<Entity> query = Query.newEntityQueryBuilder().setKind("User")
                .setOrderBy(StructuredQuery.OrderBy.asc("user_creation_time"))
                .build();

        return datastore.run(query);
    }
}