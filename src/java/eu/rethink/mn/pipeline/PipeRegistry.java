package eu.rethink.mn.pipeline;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageCodec;

import java.util.HashMap;
import java.util.Map;

public class PipeRegistry {
	final EventBus eb;
	
	//TODO: persistence maps?
	
	//<url, resourceUID>
	final Map<String, String> address;
	
	public PipeRegistry(Vertx vertx) {
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
				buffer.appendString(ctx.getMessage().toString());
			}

			@Override
			public PipeContext decodeFromWire(int pos, Buffer buffer) {
				return null; //not needed in this architecture
			}
		});
		
		this.address = new HashMap<String, String>(); //TODO: transform into ClusterMap
	}
	
	public EventBus getEventBus() { return eb; }
	
	public PipeRegistry install(String url, Handler<PipeContext> handler) {
		eb.consumer(url, msg -> {
			handler.handle((PipeContext)msg.body());
		});
		
		return this;
	}
	
	public PipeRegistry bind(String url, String resourceUID) {
		address.put(url, resourceUID);
		return this;
	}
	
	public PipeRegistry unbind(String url) {
		address.remove(url);
		return this;
	}
	
	public String resolve(String url) {
		final String uid = address.get(url);
		if(uid == null) return url;
		return address.get(url);
	}
}
