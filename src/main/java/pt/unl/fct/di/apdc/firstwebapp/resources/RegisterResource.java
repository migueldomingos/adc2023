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
public class RegisterResource {
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	
	public RegisterResource() {}
	
	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addPersonToDatastoreV1(RegisterData data) {
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
	@Path("/add")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addPersonToDatastore(RegisterData data) {
		LOG.fine("Attempt to register user: " + data.username);

		if (!data.validRegistration())
			return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();

		if (!data.password.equals(data.confirmation))
			return Response.status(Status.BAD_REQUEST).entity("Password and confirmation are not the same.").build();

		Transaction txn = datastore.newTransaction();

		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Entity user = txn.get(userKey);

			if (user != null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User already exists.").build();
			} else {
				//user_role = USER | GS | GBO | SU
				//user_state = 0 (INATIVO) | 1 (ATIVO)

				user = Entity.newBuilder(userKey)
						.set("user_username", data.username)
						.set("user_name", data.name)
						.set("user_pwd", DigestUtils.sha512Hex(data.password))
						.set("user_email", data.email)
						.set("user_creation_time", Timestamp.now())
						.set("user_phone_num", data.phoneNum)
						.set("user_role", "USER")
						.set("user_state", 0)
						.set("user_perfil", "PUBLIC")
						.set("user_mobile_num", data.mobileNum)
						.set("user_ocupation", data.ocupation)
						.set("user_workspace", data.workspace)
						.set("user_addr", data.addr)
						.set("user_postal", data.postalCode)
						.set("user_NIF", data.NIF)
						.build();

				txn.add(user);
				LOG.info("User registered " + data.username);
				txn.commit();
				return Response.ok("User registered!").build();
			}
		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}
	
}
