/**
* Copyright 2016 PT Inovação e Sistemas SA
* Copyright 2016 INESC-ID
* Copyright 2016 QUOBIS NETWORKS SL
* Copyright 2016 FRAUNHOFER-GESELLSCHAFT ZUR FOERDERUNG DER ANGEWANDTEN FORSCHUNG E.V
* Copyright 2016 ORANGE SA
* Copyright 2016 Deutsche Telekom AG
* Copyright 2016 Apizee
* Copyright 2016 TECHNISCHE UNIVERSITAT BERLIN
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
**/

package eu.rethink.mn.pipeline;

import static java.lang.System.out;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.vertx.core.eventbus.MessageConsumer;

/**
 * @author micaelpedrosa@gmail.com
 * Session data and associated actions for a resource connection.
 * NOTE: there is room for improvements here. PipeResource and PipeSession can be simplified in one class.
 * But some care should be taken because of the cluster architecture. 
 */
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
	
	/** Allocate a new URL address for a Hyperty or Object.
	 * @param url HypertyURL or ObjectURL
	 * @return true if succeeded
	 */
	public boolean allocate(String url) {
		if(registry.urlSpace.containsKey(url))
			return false;

		addURL(url);

		out.println("ALLOCATE(" + runtimeSessionURL + "): " + url);
		return true;
	}
	
	/** DeAllocate a URL address for a Hyperty or Object.
	 * @param url HypertyURL or ObjectURL
	 */
	public void deallocate(String url) {
		out.println("DEALLOCATE(" + runtimeSessionURL + "): " + url);
		removeURL(url);
	}

	/** Attach an address listener to the current resource. 
	 * Any message to this address should reach the Resource client.
	 * @param address Any address
	 * @return true if succeeded
	 */
	public boolean addListener(String address) {
		if(consumers.containsKey(address)) {
			return false;
		}
		
		final MessageConsumer<Object> value = registry.getEventBus().consumer(address, msg -> {
			registry.getEventBus().send(resourceUID, msg.body());
		});
		
		consumers.put(address, value);
		
		out.println("ADD-LISTENER(" + runtimeSessionURL + "): " + address);
		return true;
	}
	
	/** Detach an address listener.
	 * @param address Any address
	 */
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
