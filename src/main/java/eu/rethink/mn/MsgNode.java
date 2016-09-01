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

package eu.rethink.mn;

import static java.lang.System.out;

import eu.rethink.mn.component.GlobalRegistryConnector;
import eu.rethink.mn.component.HypertyAllocationManager;
import eu.rethink.mn.component.ObjectAllocationManager;
import eu.rethink.mn.component.RegistryConnector;
import eu.rethink.mn.component.SessionManager;
import eu.rethink.mn.component.SubscriptionManager;
import eu.rethink.mn.pipeline.PipeRegistry;
import eu.rethink.mn.pipeline.Pipeline;
import eu.rethink.mn.pipeline.handlers.PoliciesPipeHandler;
import eu.rethink.mn.pipeline.handlers.TransitionPipeHandler;
import eu.rethink.mn.pipeline.handlers.ValidatorPipeHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

/**
 * @author micaelpedrosa@gmail.com
 * Main class to start the msg-node server
 */
public class MsgNode extends AbstractVerticle {

	public static void main(String[] args) {
		final NodeConfig config = readConfig();
		try {
			final ClusterManager mgr = new HazelcastClusterManager();
			final MsgNode msgNode = new MsgNode(mgr, config);

			final VertxOptions options = new VertxOptions().setClusterManager(mgr);
			Vertx.clusteredVertx(options, res -> {
				if (res.succeeded()) {
					Vertx vertx = res.result();
					vertx.deployVerticle(msgNode);

                    DeploymentOptions verticleOptions = new DeploymentOptions().setWorker(true);
                    vertx.deployVerticle("js:./src/js/connector/RegistryConnectorVerticle.js", verticleOptions);
                    vertx.deployVerticle("js:./src/js/connector/GlobalRegistryConnectorVerticle.js", verticleOptions);
										vertx.deployVerticle("js:./src/js/connector/PoliciesConnectorVerticle.js", verticleOptions);
				} else {
					System.exit(-1);
				}
			});

		} catch (Exception e) {
			System.out.println("Problem in config setup.");
			System.exit(-1);
		}
	}

	private final ClusterManager mgr;
	private final NodeConfig config;

	public static NodeConfig readConfig() {
		NodeConfig config = null;

		String selection = System.getenv("MSG_NODE_CONFIG");
		if (selection == null) {
			System.out.println("[Config] No enviroment variable MSG_NODE_CONFIG, default to node.config.json -> dev");
			selection = "dev";
		}

		if (!selection.equals("env")) {
			//load from config file
			config = NodeConfig.readFromFile("node.config.json", selection);
		} else {
			//load from environment variables
			config = NodeConfig.readFromEnvironment();
		}

		return config;
	}

	public MsgNode(ClusterManager mgr, NodeConfig config) {
		this.mgr = mgr;
		this.config = config;
	}

	@Override
	public void start() throws Exception {
		final PipeRegistry register = new PipeRegistry(vertx, mgr, config.getDomain());
		register.installComponent(new SubscriptionManager(register));
		register.installComponent(new SessionManager(register));
		register.installComponent(new HypertyAllocationManager(register));
		register.installComponent(new ObjectAllocationManager(register));

		final RegistryConnector rc = new RegistryConnector(register);
		register.installComponent(rc);

		final GlobalRegistryConnector grc = new GlobalRegistryConnector(register);
		register.installComponent(grc);

		final Pipeline pipeline = new Pipeline(register)
			.addHandler(new ValidatorPipeHandler()) 	//validation of mandatory fields
			.addHandler(new TransitionPipeHandler()) 	//inter-domain allocator and routing
			.addHandler(new PoliciesPipeHandler())
			.failHandler(error -> {
				out.println("PIPELINE-FAIL: " + error);
			});


		//HTTPS security configurations
		final JksOptions jksOptions = new JksOptions()
			.setPath("server-keystore.jks")
			.setPassword("rethink2015");


		final HttpServerOptions httpOptions = new HttpServerOptions()
			.setTcpKeepAlive(true)
			.setSsl(true)
			.setKeyStoreOptions(jksOptions);


		final HttpServer server = vertx.createHttpServer(httpOptions);
		server.requestHandler(req -> {
			//just a land page to test connection
			System.out.println("HTTP-PING");
			req.response().putHeader("content-type", "text/html").end("<html><body><h1>Hello</h1></body></html>");
		});


		WebSocketServer.init(server, pipeline);
		server.listen(config.getPort());
		System.out.println("[Message-Node] Running with config: " + config);
	}
}
