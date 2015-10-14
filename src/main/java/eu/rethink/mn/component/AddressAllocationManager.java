package eu.rethink.mn.component;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.rethink.mn.IComponent;
import eu.rethink.mn.pipeline.PipeContext;
import eu.rethink.mn.pipeline.PipeMessage;
import eu.rethink.mn.pipeline.PipeRegistry;

public class AddressAllocationManager implements IComponent {
	final String name;
	final PipeRegistry register;

	final String baseURL;
	
	public AddressAllocationManager(String name, PipeRegistry register) {
		this.name = name;
		this.register = register;
		
		this.baseURL = "hyperty://" + register.getDomain() + "/";
	}
	
	@Override
	public String getName() { return name; }
	
	@Override
	public void handle(PipeContext ctx) {
		final PipeMessage msg = ctx.getMessage();
		
		if(msg.getType().equals("create")) {
			int number = msg.getBody().getInteger("number", 5);
			final List<String> allocated = allocate(ctx, msg.getFrom(), number);
		
			final PipeMessage reply = new PipeMessage();
			reply.setId(msg.getId());
			reply.setFrom(name);
			reply.setTo(msg.getFrom());
			
			final JsonObject body = reply.getBody();
			body.put("allocated", new JsonArray(allocated));
			
			ctx.reply(reply);
		} else {
			//TODO: deallocate !?
		}
	}

	private List<String> allocate(PipeContext ctx, String runtimeRUL, int number) {
		final ArrayList<String> list = new ArrayList<String>(number);
		int i = 0;
		while(i < number) {
			//find unique url, not in registry...
			final String url = baseURL + UUID.randomUUID().toString();
			if(register.allocate(url, runtimeRUL)) {
				list.add(url);
				i++;
			}
		}
		
		return list;
	}
}
