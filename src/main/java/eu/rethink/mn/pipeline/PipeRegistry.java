package eu.rethink.mn.pipeline;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.spi.cluster.ClusterManager;

import java.util.HashMap;
import java.util.Map;

import eu.rethink.mn.IComponent;

public class PipeRegistry {
	final EventBus eb;
	final ClusterManager mgr;
	
	final String domain;
	final Map<String, IComponent> components; 				//<ComponentName, IComponent>
	final Map<String, PipeSession> sessions;				//<RuntimeURL, PipeSession>
	
	
	//cluster maps...
	final Map<String, String> urlSpace; 					//<URL, RuntimeURL>
	
	public PipeRegistry(Vertx vertx, ClusterManager mgr, String domain) {
		this.domain = domain;
		this.mgr = mgr;
		
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
				final String msg = ctx.getMessage().toString();
				System.out.println("encodeToWire: " + msg);
				buffer.appendString(msg);
			}

			@Override
			public PipeContext decodeFromWire(int pos, Buffer buffer) {
				final String msg = buffer.getString(0, buffer.length() -1 );
				System.out.println("decodeFromWire: " + msg);
				return null; //not needed in this architecture
			}
		});
		
		this.components = new HashMap<String, IComponent>();
		this.sessions = new HashMap<String, PipeSession>();
		
		this.urlSpace = mgr.getSyncMap("urlSpace");
	}
	
	public EventBus getEventBus() { return eb; }
	public String getDomain() { return domain; }
	
	/** Install an addressable component.
	 * @param component The IComponent interface, the handler is called when the message is to be deliver.
	 * @return this
	 */
	public PipeRegistry installComponent(IComponent component) {
		components.put(component.getName(), component);
		return this;
	}
	
	/** Get a component interface for the name address registered.
	 * @param url for the component
	 * @return component registered for the URL
	 */
	public IComponent getComponent(String url) {
		return components.get(url);
	}
	
	/** Adds a runtimeURL relation with a channel resource UID.
	 * @param runtimeURL The runtimeURL 
	 * @param resourceUID The textUID address registered in the vertx EventBus.
	 * @return this
	 */
	public PipeRegistry bind(String runtimeSessionURL, String resourceUID) {
		final PipeSession session = new PipeSession(eb, runtimeSessionURL, urlSpace);
		session.setListener(resourceUID);
		
		sessions.put(runtimeSessionURL, session);
		
		return this;
	}
	
	/** Rebind other resource to the same (runtimeURL, runtimeToken)
	 * @param runtimeURL The runtimeURL 
	 * @param resourceUID The textUID address registered in the vertx EventBus.
	 * @return
	 */
	public PipeRegistry rebind(String runtimeSessionURL, String resourceUID) {
		sessions.get(runtimeSessionURL).setListener(resourceUID);
		
		return this;
	}
	
	/** Removes runtimeURL relation from the "whatever" resource channel that is registered.
	 * @param runtimeURL The runtimeURL 
	 * @return this
	 */
	public PipeRegistry unbind(String runtimeSessionURL) {
		final PipeSession session = sessions.remove(runtimeSessionURL);
		session.close();
		
		return this;
	}

	/** Creates a link between an URL (Hyperty, Resource, ...) and the runtimeURL. 
	 * @param url Any unique identifiable resource URL
	 * @param runtimeURL The runtimeURL 
	 * @return <strong>true</strong> if the allocation is successful, <strong>false</strong> if the URL already exist.
	 */
	public boolean allocate(String url, String runtimeSessionURL) {
		if(urlSpace.containsKey(url))
			return false;

		sessions.get(runtimeSessionURL).addURL(url);
		return true;
	}
	
	/** Removes the link between an URL (Hyperty, Resource, ...) and the runtimeURL.
	 * @param url Any unique identifiable resource URL
	 */
	public void deallocate(String url, String runtimeSessionURL) {
		sessions.get(runtimeSessionURL).removeURL(url);
	}
	
	/** Try to resolve any URL given to a RuntimeURL.
	 * @param url Any URL bound or allocated (RuntimeURL, HypertyURL, ResourceURL, ...)
	 * @return RuntimeURL registered in the vertx EventBus.
	 */
	public String resolve(String url) {
		return urlSpace.get(url);
	}
}
