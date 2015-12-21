package eu.rethink.mn.pipeline;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;

public class PipeSession {
	final PipeRegistry registry;
	final String runtimeSessionURL;
	
	final Map<String, MessageConsumer<Object>> consumers = new HashMap<>();
	final Set<String> urls = new HashSet<>();
	
	public String getRuntimeSessionURL() { return runtimeSessionURL; }
	
	PipeSession(PipeRegistry registry, String runtimeSessionURL) {
		this.registry = registry;
		this.runtimeSessionURL = runtimeSessionURL;
	}
		
	public void addListener(String address, Handler<Message<Object>> handler) {
		final MessageConsumer<Object> value = registry.getEventBus().consumer(address, handler);
		consumers.put(address, value);
	}
	
	public void removeListener(String address) {
		final MessageConsumer<Object> value = consumers.remove(address);
		if (value != null) {
			value.unregister();
		}
	}
	
	public boolean allocate(String url) {
		if(registry.urlSpace.containsKey(url))
			return false;

		addURL(url);
		return true;
	}
	
	public void deallocate(String url) {
		removeURL(url);
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
		addURL(runtimeSessionURL);

		addListener(runtimeSessionURL, msg -> {
			registry.getEventBus().send(resourceUID, msg.body());
		});
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
