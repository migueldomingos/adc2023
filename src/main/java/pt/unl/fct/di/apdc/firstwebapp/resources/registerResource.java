package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;

import pt.unl.fct.di.apdc.firstwebapp.util.*;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class registerResource {
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	private static final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
	
	public registerResource() {}
	
	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addPersonToDatastoreV1(LoginData data) {	
		Key userKey = datastore.newKeyFactory().setKind("Person").newKey(data.username);
		Entity person = Entity.newBuilder(userKey).set("password", data.password)
												  .set("timestamp", fmt.format(new Date())).build();
		datastore.put(person);
		return Response.ok().build();
	}
	
	@POST
	@Path("/v2")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addPersonToDatastoreV2(LoginDataUpgraded data) {
		Key userKey = datastore.newKeyFactory().setKind("PersonV2").newKey(data.username);
		Entity person = datastore.get(userKey);
		
		if (person == null) {
			if (!data.password.equals("") && !data.confirmation.equals("") && !data.email.equals("") && !data.name.equals("")) {
				if (data.password.equals(data.confirmation)) {
					person = Entity.newBuilder(userKey).set("password", data.password)
							  						   .set("email", data.email)
							  						   .set("name", data.name).build();
					datastore.put(person);
					return Response.ok().build();
				} else
					return Response.status(Status.FORBIDDEN).entity("Password is different from the confirmation.").build(); //pass e conf nao sao iguais
			} else
				return Response.status(Status.FORBIDDEN).entity("Data required is missing.").build(); //dados nao estao todos preenchidos	
		} else
			return Response.status(Status.FORBIDDEN).entity("User already registered.").build();  //user ja existe
			
	}
	
}
