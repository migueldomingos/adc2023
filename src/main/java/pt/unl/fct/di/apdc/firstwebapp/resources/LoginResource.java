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
import pt.unl.fct.di.apdc.firstwebapp.util.LoginDataUpgraded;

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

	private final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	
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
		LOG.fine("Attempt to login user: " + data.username);

		Key userKey = userKeyFactory.newKey(data.username);
		Entity ent = datastore.get(userKey);

		if (ent != null) {
			String hashedPWD = DigestUtils.sha512Hex(data.password);
			if (hashedPWD.equals(ent.getString("password"))) {
				AuthToken at = new AuthToken(data.username);
				LOG.info("User '" + data.username + "' logged in sucessfully.");
				return Response.ok(g.toJson(at)).build();
			}
			else {
				LOG.warning("Wrong password for username: " + data.username);
				return Response.status(Status.FORBIDDEN).entity("Incorrect password.").build();
			}
		}
		else {
			LOG.warning("Failed login attempt for username: " + data.username);
			return Response.status(Status.FORBIDDEN).entity("User does not exist.").build();
		}
	}
	
	@POST
	@Path("/v2")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset = utf-8")
	public Response loginUserV2 (LoginData data, @Context HttpServletRequest request, @Context HttpHeaders headers) {
		LOG.fine("Attempt to login user: " + data.username);

		Key userKey = userKeyFactory.newKey(data.username);
		Key ctrsKey = datastore.newKeyFactory()
				.addAncestors(PathElement.of("User", data.username))
				.setKind("UserStats")
				.newKey("counters");

		Key logKey = datastore.allocateId(
				datastore.newKeyFactory()
						.addAncestors(PathElement.of("User", data.username))
						.setKind("UserLog")
						.newKey()
		);

		Transaction txn = datastore.newTransaction();

		try {
			Entity user = txn.get(userKey);
			if (user == null) {
				//Username does not exist
				LOG.warning("Failed login attempt for username: " + data.username);
				return Response.status(Status.FORBIDDEN).build();
			}

			//We get the user stats from the storage
			Entity stats = txn.get(ctrsKey);
			if (stats == null) {
				stats = Entity.newBuilder(ctrsKey)
						.set("user_stats_logins", 0L)
						.set("user_stats_failed", 0L)
						.set("user_first_login", Timestamp.now())
						.set("user_last_login", Timestamp.now())
						.build();
			}
			String hashedPWD = (String) user.getString("user_pwd");
			if (hashedPWD.equals(DigestUtils.sha512Hex(data.password))) {
				//Password is correct, construct the log
				Entity log = Entity.newBuilder(logKey)
						.set("user_login_ip", request.getRemoteAddr())
						.set("user_login_host", request.getRemoteHost())
						.set("user_login_latlon",
								//Does not index this property
								StringValue.newBuilder(headers.getHeaderString("X-AppEngine-CityLatLong")).setExcludeFromIndexes(true).build())
						.set("user_login_city", headers.getHeaderString("X-AppEngine-City"))
						.set("user_login_country", headers.getHeaderString("X-AppEngine-Country"))
						.set("user_login_time", Timestamp.now())
						.build();

				//Get the user stats and updates it
				updateStats(true, txn, log, ctrsKey, stats);

				AuthToken token = new AuthToken(data.username);
				LOG.info("User '" + data.username + "' logged in sucessfully.");
				return Response.ok(g.toJson(token)).build();
			} else {
				updateStats(false, txn, null, ctrsKey, stats);
				LOG.warning("Wrong password for username: " + data.username);
				return Response.status(Status.FORBIDDEN).build();
			}
		} catch (Exception e) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}

	}

	private void updateStats(boolean isLogged, Transaction txn, Entity log, Key ctrsKey, Entity stats) {
		if (isLogged) {
			Entity ustats = Entity.newBuilder(ctrsKey)
					.set("user_stats_logins", 1L + stats.getLong("user_stats_logins"))
					.set("user_stats_failed", stats.getLong("user_stats_failed"))
					.set("user_first_login", stats.getTimestamp("user_first_login"))
					.set("user_last_login", Timestamp.now())
					.build();

			txn.put(log, ustats);
		} else {
			Entity ustats = Entity.newBuilder(ctrsKey)
					.set("user_stats_logins", stats.getLong("user_stats_logins"))
					.set("user_stats_failed", 1L + stats.getLong("user_stats_failed"))
					.set("user_first_login", stats.getTimestamp("user_first_login"))
					.set("user_last_login", Timestamp.now())
					.build();

			txn.put(ustats);
		}

		txn.commit();

	}

	@POST
	@Path("/user")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkUsernameAvailable (LoginDataUpgraded data) {
		Key userKey = userKeyFactory.newKey(data.username);
		Entity user = datastore.get(userKey);
		if (user != null && user.getString("user_pwd").equals(DigestUtils.sha512Hex(data.password))) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -1);
			Timestamp yesterday = Timestamp.of(cal.getTime());

			Query<Entity> query = Query.newEntityQueryBuilder()
					.setKind("UserLog")
					.setFilter(
							StructuredQuery.CompositeFilter.and(
									StructuredQuery.PropertyFilter.hasAncestor(userKeyFactory.newKey(data.username)),
									StructuredQuery.PropertyFilter.ge("user_login_time", yesterday)
							)
					)
					.setOrderBy(StructuredQuery.OrderBy.desc("user_login_time"))
					.setLimit(3)
					.build();

			QueryResults<Entity> logs = datastore.run(query);

			List<Date> loginDates = new ArrayList<>();
			logs.forEachRemaining(userlog -> {
				loginDates.add(userlog.getTimestamp("user_login_time").toDate());
			});
			return Response.ok(g.toJson(loginDates)).build();
		} else {
			return Response.status(Status.FORBIDDEN).entity("Incorrect username or password.").build();
		}
	}


}
