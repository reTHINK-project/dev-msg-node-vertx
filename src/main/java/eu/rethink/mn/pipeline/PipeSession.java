package eu.rethink.mn.pipeline;

import static java.lang.System.out;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.vertx.core.eventbus.MessageConsumer;

public class PipeSession {
	final PipeRegistry registry;
	final String runtimeSessionURL;
	String resourceUID = null;
	
	final Map<String, MessageConsumer<Object>> consumers = new HashMap<>();
	final Set<String> urls = new HashSet<>(); //unique URL's registered in registry.urlSpace
	
	public String getRuntimeSessionURL() { return runtimeSessionURL; }
	
	PipeSession(PipeRegistry registry, String runtimeSessionURL) {
		this.registry = registry;
		this.runtimeSessionURL = runtimeSessionURL;
	}
	
	public boolean allocate(String url) {
		if(registry.urlSpace.containsKey(url))
			return false;

		addURL(url);
		out.println("ALLOCATE(" + runtimeSessionURL + "): " + url);
		
		return true;
	}
	
	public void deallocate(String url) {
		removeURL(url);
	}

	public boolean addListener(String address) {
		out.println("ADD-LISTENER(" + runtimeSessionURL + "): " + address);
		
		if(consumers.containsKey(address)) {
			return false;
		}
		
		final MessageConsumer<Object> value = registry.getEventBus().consumer(address, msg -> {
			registry.getEventBus().send(resourceUID, msg.body());
		});
		
		consumers.put(address, value);
		return true;
	}
	
	public void removeListener(String address) {
		out.println("REMOVE-LISTENER(" + runtimeSessionURL + "): " + address);
		
		final MessageConsumer<Object> value = consumers.remove(address);
		if (value != null) {
			value.unregister();
		}
	}
	
	void close() {
		registry.sessions.remove(runtimeSessionURL);

		for (String url: urls) {
			registry.urlSpace.remove(url);
		}
		
		for (MessageConsumer<Object> value: consumers.values()) {
			value.unregister();
		}
		
		// consumers.clear(); or urls.clear(); no need to do this, session will be discarded
	}
	
	void bindToResourceUID(String resourceUID) {
		this.resourceUID = resourceUID;
		addURL(runtimeSessionURL);
		addListener(runtimeSessionURL);
	}
	
	void addURL(String url) {
		registry.urlSpace.put(url, runtimeSessionURL);
		urls.add(url);
	}
	
	void removeURL(String url) {
		urls.remove(url);
		registry.urlSpace.remove(url);
	}
}
