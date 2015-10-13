package eu.rethink.mn.pipeline;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageCodec;

import java.util.HashMap;
import java.util.Map;

import eu.rethink.mn.IComponent;

public class PipeRegistry {
	final EventBus eb;
	
	//TODO: persistence maps?
	
	final String domain; 
	
	//<RuntimeURL, resourceUID>
	final Map<String, String> runtimeSpace;
	
	//<URL, RuntimeURL>
	final Map<String, String> urlSpace;
	
	public PipeRegistry(Vertx vertx, String domain) {
		this.domain = domain;
		this.eb = vertx.eventBus();
		this.eb.registerDefaultCodec(PipeContext.class, new MessageCodec<PipeContext, PipeContext>() {

			@Override
			public byte systemCodecID() { return -1; }
			
			@Override
			public String name() { return PipeContext.class.getName(); }
			
			@Override
			public PipeContext transform(PipeContext ctx) { return ctx; }
			
			@Override
			public void encodeToWire(Buffer buffer, PipeContext ctx) {
				System.out.println("encodeToWire");
				buffer.appendString(ctx.getMessage().toString());
			}

			@Override
			public PipeContext decodeFromWire(int pos, Buffer buffer) {
				return null; //not needed in this architecture
			}
		});
		
		this.runtimeSpace = new HashMap<String, String>(); //TODO: transform into ClusterMap
		this.urlSpace = new HashMap<String, String>(); //TODO: transform into ClusterMap
	}
	
	public String getDomain() { return domain; }
	
	public EventBus getEventBus() { return eb; }
	
	public PipeRegistry install(IComponent component) {
		eb.consumer(component.getName(), msg -> {
			component.handle((PipeContext)msg.body());
		});
		
		return this;
	}
	
	public PipeRegistry bind(String runtimeURL, String resourceUID) {
		runtimeSpace.put(runtimeURL, resourceUID);
		return this;
	}
	
	public PipeRegistry unbind(String runtimeURL) {
		runtimeSpace.remove(runtimeURL);
		return this;
	}

	public PipeRegistry allocate(String url, String runtimeURL) {
		urlSpace.put(url, runtimeURL);
		return this;
	}
	
	public PipeRegistry deallocate(String url) {
		urlSpace.remove(url);
		return this;
	}
	
	public String resolve(String url) {
		final String uid = runtimeSpace.get(url);
		if(uid != null) {
			return uid;
		} else {
			final String runtimeURL = urlSpace.get(url);
			return runtimeSpace.get(runtimeURL);
		}
	}
}
