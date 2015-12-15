package eu.rethink.mn.pipeline;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;

public class PipeSession {
	final EventBus eb;
	final String runtimeSessionURL;
	
	final Set<String> urls = new HashSet<String>();
	final Map<String, String> urlSpace; 
	
	MessageConsumer<Object> consumer = null;
	
	public PipeSession(EventBus eb, String runtimeSessionURL, Map<String, String> urlSpace) {
		this.eb = eb;
		this.runtimeSessionURL = runtimeSessionURL;
		this.urlSpace = urlSpace;
		
		urlSpace.put(runtimeSessionURL, runtimeSessionURL);
	}
	
	
	public void setListener(String resourceUID) {
		consumer = eb.consumer(runtimeSessionURL, msg -> {
			eb.send(resourceUID, msg.body());
		});
	}
	
	public void addURL(String url) {
		urlSpace.put(url, runtimeSessionURL);
		urls.add(url);
	}
	
	public void removeURL(String url) {
		urls.remove(url);
		urlSpace.remove(url);
	}
	
	public void close() {
		if(consumer != null) {
			consumer.unregister();
		}
		
		for (String url: urls) {
			urlSpace.remove(url);
		}
		
		//urls.clear(); no need to do this, session will be discarded
	}
}
