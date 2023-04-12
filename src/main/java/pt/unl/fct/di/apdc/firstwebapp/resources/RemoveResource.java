package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;

@Path("/remove")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RemoveResource {
    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
    private static final Logger LOG = Logger.getLogger(RemoveResource.class.getName());

    public RemoveResource() {}

    @DELETE
    @Path("/{username}/{userToRemove}")
    public Response removeUser(@PathParam("username") String username, @PathParam("userToRemove") String userToRemove) {
        LOG.fine("Attempt to remove user: " + userToRemove);

        Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(username);
        Entity token = datastore.get(tokenKey);

        if (token == null) {
            return Response.status(Status.FORBIDDEN).entity("User not logged in.").build();
        }
        else if (token.getLong("user_expiration") < Timestamp.now().toDate().getTime()) {
            datastore.delete(tokenKey);
            return Response.status(Status.FORBIDDEN).entity("User not logged in.").build();
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key userKeyRem = userKeyFactory.newKey(userToRemove);
            Entity userToRem = txn.get(userKeyRem);
            Key userKey = userKeyFactory.newKey(username);
            Entity user = txn.get(userKey);

            if (user == null) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("User does not exist").build();
            }

            if (userToRem == null) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("User to remove does not exist").build();
            }

            if (user.getString("user_role").equals("USER")) {
                if (username.equals(userToRemove)) {
                    txn.delete(userKeyRem);
                    txn.commit();
                    return Response.status(Status.OK).entity("User removed!").build();
                } else {
                    txn.rollback();
                    return Response.status(Status.FORBIDDEN).entity(username + " cannot remove " + userToRemove + ".").build();
                }

            } else if (user.getString("user_role").equals("GBO")) {
                if (username.equals(userToRemove) || userToRem.getString("user_role").equals("USER")) {
                    txn.delete(userKeyRem);
                    txn.commit();
                    return Response.status(Status.OK).entity("User removed!").build();
                } else {
                    txn.rollback();
                    return Response.status(Status.FORBIDDEN).entity(username + " cannot remove " + userToRemove + ".").build();
                }

            } else if (user.getString("user_role").equals("GS")) {
                if (username.equals(userToRemove) || userToRem.getString("user_role").equals("USER") || userToRem.getString("user_role").equals("GBO")) {
                    txn.delete(userKeyRem);
                    txn.commit();
                    return Response.status(Status.OK).entity("User removed!").build();
                } else {
                    txn.rollback();
                    return Response.status(Status.FORBIDDEN).entity(username + " cannot remove " + userToRemove + ".").build();
                }

            } else if (user.getString("user_role").equals("SU")) {
                txn.delete(userKeyRem);
                txn.commit();
                return Response.status(Status.OK).entity("User removed!").build();
            }

            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error").build();

        } finally {
            if (txn.isActive())
                txn.rollback();
        }

    }
}