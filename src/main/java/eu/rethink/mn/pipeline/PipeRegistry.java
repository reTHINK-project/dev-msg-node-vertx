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
	
	EventBus getEventBus() { return eb; }
	
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
	
	/** Install an addressable component.
	 * @param component The IComponent interface, the handler is called when the message is to be deliver.
	 * @return this
	 */
	public PipeRegistry install(IComponent component) {
		eb.consumer(component.getName(), msg -> {
			component.handle((PipeContext)msg.body());
		});
		
		return this;
	}
	
	/** Adds a runtimeURL relation with a channel resource UID.
	 * @param runtimeURL The runtimeURL 
	 * @param resourceUID The textUID address registered in the vertx EventBus.
	 * @return this
	 */
	public PipeRegistry bind(String runtimeURL, String resourceUID) {
		runtimeSpace.put(runtimeURL, resourceUID);
		return this;
	}
	
	/** Removes runtimeURL relation from the "whatever" resource channel that is registered.
	 * @param runtimeURL The runtimeURL 
	 * @return this
	 */
	public PipeRegistry unbind(String runtimeURL) {
		runtimeSpace.remove(runtimeURL);
		return this;
	}

	/** Creates a link between an URL (Hyperty, Resource, ...) and the runtimeURL. 
	 * @param url Any unique identifiable resource URL
	 * @param runtimeURL The runtimeURL 
	 * @return <strong>true</strong> if the allocation is successful, <strong>false</strong> if the URL already exist.
	 */
	public boolean allocate(String url, String runtimeURL) {
		if(urlSpace.containsKey(url))
			return false;

		urlSpace.put(url, runtimeURL);
		return true;
	}
	
	/** Removes the link between an URL (Hyperty, Resource, ...) and the runtimeURL.
	 * @param url Any unique identifiable resource URL
	 */
	public void deallocate(String url) {
		urlSpace.remove(url);
	}
	
	/** Try to resolve any URL given  to a channel UID.
	 * @param url Any URL bound or allocated (RuntimeURL, HypertyURL, ResourceURL, ...)
	 * @return textUID registered in the vertx EventBus.
	 */
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
