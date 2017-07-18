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

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.spi.cluster.ClusterManager;

import java.util.HashMap;
import java.util.Map;

import eu.rethink.mn.IComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author micaelpedrosa@gmail.com
 * Needed information for the pipeline.
 * Registered components, sessions, and cluster aware data.
 */
public class PipeRegistry {
	static final Logger logger = LoggerFactory.getLogger("BROKER");

	final EventBus eb;
	final ClusterManager mgr;

	final String domain;
	final Map<String, IComponent> components; 				//<ComponentName, IComponent>
	final Map<String, PipeSession> sessions;				//<RuntimeURL, PipeSession>

	//cluster maps...
	final Map<String, String> urlSpace; 					//<URL, RuntimeURL>

	public EventBus getEventBus() { return eb; }
	public String getDomain() { return domain; }

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
				logger.info("encodeToWire: " + msg);
				buffer.appendString(msg);
			}

			@Override
			public PipeContext decodeFromWire(int pos, Buffer buffer) {
				final String msg = buffer.getString(0, buffer.length() -1 );
				logger.info("decodeFromWire: " + msg);
				return null; //not needed in this architecture
			}
		});

		this.components = new HashMap<String, IComponent>();
		this.sessions = new HashMap<String, PipeSession>();

		this.urlSpace = mgr.getSyncMap("urlSpace");
	}


	/** Install an addressable component.
	 * @param component The IComponent interface, the handler is called when the message is to be deliver.
	 * @return himself for fluent API
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

	/** Creates a session in this resource context with runtimeSessionURL identification
	 * @param runtimeSessionURL The runtimeURL
	 * @return himself for fluent API
	 */
	public PipeSession createSession(String runtimeSessionURL) {
		final PipeSession session = new PipeSession(this, runtimeSessionURL);
		sessions.put(runtimeSessionURL, session);

		return session;
	}

	/** Get a session data from a runtimeSessionURL identification
	 * @param runtimeSessionURL Same address used on createSession
	 * @return himself for fluent API
	 */
	public PipeSession getSession(String runtimeSessionURL) {
		return sessions.get(runtimeSessionURL);
	}

	public PipeSession getSessionByRuntime(String runtimeURL) {

		PipeSession session = null;

		for (Map.Entry<String, PipeSession> e : sessions.entrySet()) {
			if (e.getKey().startsWith(runtimeURL)) {
				session = sessions.get(e.getKey());
			}
		}

		return session;

	}
}
