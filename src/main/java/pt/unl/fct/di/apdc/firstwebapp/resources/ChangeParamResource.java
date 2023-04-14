package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;

import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.*;

@Path("/change")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangeParamResource {

    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    private final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

    private static final Logger LOG = Logger.getLogger(ChangeParamResource.class.getName());

    public ChangeParamResource() {}

    @POST
    @Path("/params/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeParams (@PathParam("username") String username, UserClass data) {
        //username do pathparam vai ser o user que quer mudar a info
        //data vai ter a info que vais ser modificada

        Transaction txn = datastore.newTransaction();

        try {
            Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(username);
            Entity token = txn.get(tokenKey);

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

            Key userKey = userKeyFactory.newKey(username);
            Entity user = txn.get(userKey);

            if (user == null) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("User does not exist").build();
            }

            Key userModKey = userKeyFactory.newKey(data.username);
            Entity userMod = txn.get(userModKey);

            if (userMod == null) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("User to be modified does not exist").build();
            }

            String phoneNumAux = userMod.getString("user_phone_num");
            String mobileNumAux = userMod.getString("user_mobile_num");
            long userStateAux = userMod.getLong("user_state");
            String userNameAux = userMod.getString("user_name");
            String userEmailAux = userMod.getString("user_email");
            String userRoleAux = userMod.getString("user_role");
            String userPerfilAux = userMod.getString("user_perfil");
            String userOcupationAux = userMod.getString("user_ocupation");
            String userWorkSpaceAux = userMod.getString("user_workspace");
            String userAddrAux = userMod.getString("user_addr");
            String userPostalAux = userMod.getString("user_postal");
            String userNIFAux = userMod.getString("user_NIF");

            if (data.phoneNum != "" && !phoneNumAux.equals(data.phoneNum))
                phoneNumAux = data.phoneNum;

            if (data.mobileNum != "" && !mobileNumAux.equals(data.mobileNum))
                mobileNumAux = data.mobileNum;

            if (userStateAux != data.state && (data.state == 1 || data.state == 0))
                userStateAux = data.state;

            if (data.name != "" && !userNameAux.equals(data.name))
                userNameAux = data.name;

            if (data.email != "" && !userEmailAux.equals(data.email))
                userEmailAux = data.email;

            if (!userRoleAux.equals(data.role) && (userRoleAux.equals("USER") || userRoleAux.equals("GBO")
                    || userRoleAux.equals("GA") || userRoleAux.equals("GS") || userRoleAux.equals("SU")))
                userRoleAux = data.role;

            if (data.perfil != "" && !userPerfilAux.equals(data.perfil))
                userPerfilAux = data.perfil;

            if (data.ocupation != "" && !userOcupationAux.equals(data.ocupation))
                userOcupationAux = data.ocupation;

            if (data.workspace != "" && !userWorkSpaceAux.equals(data.workspace))
                userWorkSpaceAux = data.workspace;

            if (data.addr != "" && !userAddrAux.equals(data.addr))
                userAddrAux = data.addr;

            if (data.postalCode != "" && !userPostalAux.equals(data.postalCode))
                userPostalAux = data.postalCode;

            if (data.NIF != "" && !userNIFAux.equals(data.NIF))
                userNIFAux = data.NIF;

            Entity userAux;

            if (user.getString("user_role").equals("USER") && user.getString("user_username").equals(data.username)) {
                userAux = Entity.newBuilder(userKey)
                        .set("user_username", data.username) //NAO MUDA
                        .set("user_name", user.getString("user_name")) //NAO MUDA
                        .set("user_pwd", user.getString("user_pwd")) //NAO MUDA
                        .set("user_email", user.getString("user_email")) //NAO MUDA
                        .set("user_creation_time", user.getTimestamp("user_creation_time")) //NAO MUDA
                        .set("user_phone_num", phoneNumAux)
                        .set("user_role", user.getString("user_role")) //NAO MUDA
                        .set("user_state", user.getLong("user_state")) //NAO MUDA
                        .set("user_perfil", userPerfilAux)
                        .set("user_mobile_num", mobileNumAux)
                        .set("user_ocupation", userOcupationAux)
                        .set("user_workspace", userWorkSpaceAux)
                        .set("user_addr", userAddrAux)
                        .set("user_postal", userPostalAux)
                        .set("user_NIF", userNIFAux)
                        .build();

            } else if (user.getString("user_role").equals("GBO") && userMod.getString("user_role").equals("USER")) {
                userAux = Entity.newBuilder(userKey)
                        .set("user_username", data.username) //NAO MUDA
                        .set("user_name", userNameAux)
                        .set("user_pwd", userMod.getString("user_pwd")) //NAO MUDA
                        .set("user_email", userEmailAux)
                        .set("user_creation_time", userMod.getTimestamp("user_creation_time")) //NAO MUDA
                        .set("user_phone_num", phoneNumAux)
                        .set("user_role", userRoleAux)
                        .set("user_state", userStateAux)
                        .set("user_perfil", userPerfilAux)
                        .set("user_mobile_num", mobileNumAux)
                        .set("user_ocupation", userOcupationAux)
                        .set("user_workspace", userWorkSpaceAux)
                        .set("user_addr", userAddrAux)
                        .set("user_postal", userPostalAux)
                        .set("user_NIF", userNIFAux)
                        .build();

            } else if (user.getString("user_role").equals("GS") && (userMod.getString("user_role").equals("USER") || userMod.getString("user_role").equals("GBO"))) {
                userAux = Entity.newBuilder(userKey)
                        .set("user_username", data.username) //NAO MUDA
                        .set("user_name", userNameAux)
                        .set("user_pwd", userMod.getString("user_pwd")) //NAO MUDA
                        .set("user_email", userEmailAux)
                        .set("user_creation_time", userMod.getTimestamp("user_creation_time")) //NAO MUDA
                        .set("user_phone_num", phoneNumAux)
                        .set("user_role", userRoleAux)
                        .set("user_state", userStateAux)
                        .set("user_perfil", userPerfilAux)
                        .set("user_mobile_num", mobileNumAux)
                        .set("user_ocupation", userOcupationAux)
                        .set("user_workspace", userWorkSpaceAux)
                        .set("user_addr", userAddrAux)
                        .set("user_postal", userPostalAux)
                        .set("user_NIF", userNIFAux)
                        .build();
            } else {
                userAux = Entity.newBuilder(userKey)
                        .set("user_username", data.username) //NAO MUDA
                        .set("user_name", userNameAux)
                        .set("user_pwd", userMod.getString("user_pwd")) //NAO MUDA
                        .set("user_email", userEmailAux)
                        .set("user_creation_time", userMod.getTimestamp("user_creation_time")) //NAO MUDA
                        .set("user_phone_num", phoneNumAux)
                        .set("user_role", userRoleAux)
                        .set("user_state", userStateAux)
                        .set("user_perfil", userPerfilAux)
                        .set("user_mobile_num", mobileNumAux)
                        .set("user_ocupation", userOcupationAux)
                        .set("user_workspace", userWorkSpaceAux)
                        .set("user_addr", userAddrAux)
                        .set("user_postal", userPostalAux)
                        .set("user_NIF", userNIFAux)
                        .build();
            }

            txn.put(userAux);
            LOG.info("User updated " + data.username);
            txn.commit();
            return Response.ok("User updated!").build();

        } finally {
            if (txn.isActive())
                txn.rollback();
        }
    }

    @POST
    @Path("/pwd")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changePWD (ChangePWDData data) {

        if (!data.newPwd.equals(data.confNewPwd))
            return Response.status(Status.BAD_REQUEST).entity("New password is different from confirmation.").build();

        Transaction txn = datastore.newTransaction();

        try {
            Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.username);
            Entity token = txn.get(tokenKey);

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

            Key userKey = userKeyFactory.newKey(data.username);
            Entity user = txn.get(userKey);

            if (user == null) {
                txn.rollback();
                return Response.status(Status.CONFLICT).entity("User does not exist").build();
            }

            Entity userAux = Entity.newBuilder(userKey)
                    .set("user_username", data.username)
                    .set("user_name", user.getString("user_name"))
                    .set("user_pwd", DigestUtils.sha512Hex(data.newPwd))
                    .set("user_email", user.getString("user_email"))
                    .set("user_creation_time", user.getTimestamp("user_creation_time"))
                    .set("user_phone_num", user.getLong("user_phone_num"))
                    .set("user_role", user.getString("user_role"))
                    .set("user_state", user.getLong("user_state")) //user_state = INATIVO
                    .set("user_perfil", user.getString("user_perfil"))
                    .set("user_mobile_num", user.getLong("user_mobile_num"))
                    .set("user_ocupation", user.getString("user_ocupation"))
                    .set("user_workspace", user.getString("user_workspace"))
                    .set("user_addr", user.getString("user_addr"))
                    .set("user_postal", user.getString("user_postal"))
                    .set("user_NIF", user.getString("user_NIF"))
                    .build();

            txn.put(userAux);
            LOG.info("User registered " + data.username);
            txn.commit();
            return Response.ok("User password updated!").build();

        } finally {
            if (txn.isActive())
                txn.rollback();
        }
    }
}
