package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.Calendar;
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

import com.google.gson.Gson;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.Timestamp;

import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;

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

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	private final Gson g = new Gson();
	
	public LoginResource() {} //construtor
	
	@POST //indica que este metodo e acessivel atraves da operacao REST:POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON) //funcao vai receber dados dos clientes que vem no corpo do pedido HTTP
	public Response doLogin(LoginData data) {
		LOG.fine("Login attempt by user: " + data.username);
		
		if(data.username.equals("miguel") && data.password.equals("password")) {
			AuthToken at = new AuthToken(data.username);
			return Response.ok(g.toJson(at)).build();
		}
		
		return Response.status(Status.FORBIDDEN).entity("Incorrect username or password.").build();
	}
	
	@GET
	@Path("/{username}")
	public Response checkUsernameAvailable(@PathParam("username") String username) {
		if(username.equals("miguel")) {
			return Response.ok().entity(g.toJson(false)).build();
		} else {
			return Response.ok().entity(g.toJson(true)).build();
		}
	}
	
	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loginUserV1 (LoginData data) {
		Key userKey = datastore.newKeyFactory().setKind("Person").newKey(data.username);
		Entity ent = datastore.get(userKey);

		if (ent != null) {
			String hashedPWD = DigestUtils.sha512Hex(data.password);
			if (hashedPWD.equals(ent.getString("password"))) {
				AuthToken at = new AuthToken(data.username);
				return Response.ok(g.toJson(at)).build();
			}
			else
				return Response.status(Status.FORBIDDEN).entity("Incorrect password.").build();
		}
		else
			return Response.status(Status.FORBIDDEN).entity("User does not exist.").build();

		

	}
	
	@POST
	@Path("/v2")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset = utf-8")
	public Response loginUserV2 (LoginData data, @Context HttpServletRequest request, @Context HttpHeaders headers) {
		Key userKey = datastore.newKeyFactory().setKind("Person").newKey(data.username);
		Entity ent = datastore.get(userKey);

		if (ent != null) {
			String hashedPWD = DigestUtils.sha512Hex(data.password);
			if (hashedPWD.equals(ent.getString("password"))) {
				AuthToken at = new AuthToken(data.username);

				KeyFactory logRegistKey = datastore.newKeyFactory().setKind("LogInfo");
				Key k = datastore.allocateId(logRegistKey.newKey());
				Entity logEnt = Entity.newBuilder(k).set("Connection local", headers.getHeaderString("X-Appengine-CityLatLong"))
													.set("Connection IP", headers.getHeaderString("X-Appengine-User-IP"))
													.build();
				datastore.put(logEnt);

				putSuccessOrErrorLogin("sucesso");

				return Response.ok(g.toJson(at)).build();
			}

			putSuccessOrErrorLogin("erro");
			return Response.status(Status.FORBIDDEN).entity("Incorrect password.").build();
		}

		return Response.status(Status.FORBIDDEN).entity("User does not exist.").build();

	}
	
	private void putSuccessOrErrorLogin(String aux) {
		int counter = 1;
		Key loginKey = datastore.newKeyFactory().setKind("Login").newKey(aux);
		Entity loginEnt = datastore.get(loginKey);
		if (loginEnt != null) 
			counter += loginEnt.getLong("counter");
		
		loginEnt = Entity.newBuilder(loginKey).set("counter", counter).build();
		datastore.put(loginEnt);
	}

}