package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;

import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.*;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class registerResource {
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	
	public registerResource() {}
	
	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addPersonToDatastoreV1(LoginData data) {
		if (!data.validRegistration())
			return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();

		Key userKey = datastore.newKeyFactory().setKind("User1").newKey(data.username);
		Entity person = datastore.get(userKey);

		if (person == null) {
			person = Entity.newBuilder(userKey).set("user_pwd", DigestUtils.sha512Hex(data.password))
											   .set("user_creation_time", Timestamp.now()).build();
			datastore.put(person);
			return Response.ok().build();
		}

		return Response.status(Status.FORBIDDEN).entity("User already registered.").build();
	}
	
	@POST
	@Path("/v2")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addPersonToDatastoreV2(LoginDataUpgraded data) {
		LOG.fine("Attempt to register user: " + data.username);

		if (!data.validRegistration())
			return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();

		Transaction txn = datastore.newTransaction();

		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Entity user = txn.get(userKey);

			if (user != null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User already exists.").build();
			} else {
				user = Entity.newBuilder(userKey)
						.set("user_name", data.name)
						.set("user_pwd", DigestUtils.sha512Hex(data.password))
						.set("user_email", data.email)
						.set("user_creation_time", Timestamp.now())
						.build();
				txn.add(user);
				LOG.info("User registered " + data.username);
				txn.commit();
				return Response.ok("{}").build();
			}
		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}
	
}
