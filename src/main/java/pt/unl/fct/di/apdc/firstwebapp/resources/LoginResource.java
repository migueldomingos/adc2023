package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import com.google.cloud.Timestamp;

import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;

/*
 * Path 
 * 
 * Parametrizada com uma string, representa o ID do recurso
 * 
 * @Path("/login") indica que as funcionalidades desta classe sao 
 * acessiveis por um URL que ira ter /login depois da componente do
 * URL que identifica o servlet (conjunto de recursos REST)
 */

/*
 * Produces
 * 
 * Quando aplicada a uma classe, indica que, por defeito, todos
 * os endpoints (funcoes do recurso) produzem dados codificados
 * nesse formato
 */

@Path("/login") // /rest/login
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	private final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	
	private final Gson g = new Gson();
	
	public LoginResource() {} //construtor
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response loginUserV1 (LoginData data) {
		LOG.fine("Attempt to login user: " + data.username);

		Transaction txn = datastore.newTransaction();

		try{
			Key userKey = userKeyFactory.newKey(data.username);
			Entity ent = txn.get(userKey);

			if (ent == null) {
				LOG.warning("Failed login attempt for username: " + data.username);
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("User does not exist.").build();
			}

			String hashedPWD = DigestUtils.sha512Hex(data.password);

			if (hashedPWD.equals(ent.getString("user_pwd"))) {
				AuthToken token = new AuthToken(data.username);
				Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.username);
				Entity tokenEnt = txn.get(tokenKey);

				if (tokenEnt != null && tokenEnt.getLong("token_expiration") > Timestamp.now().toDate().getTime()) {
					LOG.warning("User already logged in");
					txn.rollback();
					return Response.status(Status.FORBIDDEN).entity("User already logged in.").build();
				}

				tokenEnt = Entity.newBuilder(tokenKey)
						.set("token_username", token.username)
						.set("token_id", token.tokenID)
						.set("token_creation", token.creationData)
						.set("token_expiration", token.expirationData)
						.build();
				txn.put(tokenEnt);
				LOG.info("User '" + data.username + "' logged in sucessfully.");
				txn.commit();
				return Response.ok(g.toJson(ent)).build();
			}
			else {
				LOG.warning("Wrong password for username: " + data.username);
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Incorrect password.").build();
			}

		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}

	}


}
