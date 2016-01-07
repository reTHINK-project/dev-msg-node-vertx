package eu.rethink.mn;

import static java.lang.System.out;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.rethink.mn.component.HypertyAllocationManager;
import eu.rethink.mn.component.ObjectAllocationManager;
import eu.rethink.mn.component.RegistryConnector;
import eu.rethink.mn.component.SessionManager;
import eu.rethink.mn.pipeline.PipeRegistry;
import eu.rethink.mn.pipeline.Pipeline;
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

public class MsgNode extends AbstractVerticle {

	public static void main(String[] args) {
		if (args.length == 1) {
			
			//load node.config.json
			final NodeConfig config = readConfig("node.config.json");
			System.out.println("[Config] File Found");
			System.out.println(config);
			
			try {
				final int port = Integer.parseInt(args[0]);

				final ClusterManager mgr = new HazelcastClusterManager();
				final MsgNode msgNode = new MsgNode(mgr, config.getDomain(), port);

				final VertxOptions options = new VertxOptions().setClusterManager(mgr);
				Vertx.clusteredVertx(options, res -> {
					if (res.succeeded()) {
						Vertx vertx = res.result();
						vertx.deployVerticle(msgNode);

                        DeploymentOptions verticleOptions = new DeploymentOptions().setWorker(true);
                        vertx.deployVerticle("js:./src/js/connector/RegistryConnectorVerticle.js", verticleOptions);
					} else {
						System.exit(-1);
					}
				});

			} catch (Exception e) {
				System.out.println("usage: <port>");
				System.exit(-1);
			}
		} else {
			System.out.println("usage: <port>");
		}
	}

	private final ClusterManager mgr;
	private final String domain;
	private final int port;

	public static NodeConfig readConfig(String filePath) {
		final ObjectMapper objectMapper = new ObjectMapper();
		final NodeConfig config = new NodeConfig();
		
		try {
			String configSelect = System.getenv("MSG_NODE_CONFIG");
			if (configSelect == null) {
				System.out.println("[Config] No enviroment variable MSG_NODE_CONFIG, default to dev");
				configSelect = "dev";
			}
			
			config.setSelected(configSelect);
			
			final File file = new File(filePath);
		    final JsonNode node = objectMapper.readValue(file, JsonNode.class);
		    
		    final JsonNode selectedNode =  node.get(configSelect);
		    if (selectedNode == null) {
		    	System.out.println("[Config] No " + configSelect + " field found!");
		    	System.exit(-1);
		    }
		    
		    final JsonNode domainNode = selectedNode.get("domain");
		    if (domainNode == null) {
		    	System.out.println("[Config] No " + configSelect + ".domain field found!");
		    	System.exit(-1);
		    }
		    
		    config.setDomain(domainNode.asText());
		    
		    final JsonNode registryNode = selectedNode.get("registry");
		    if (registryNode == null) {
		    	System.out.println("[Config] No " + configSelect + ".registry field found!");
		    	System.exit(-1);
		    }
		    
		} catch (IOException e) {
		    e.printStackTrace();
		    System.exit(-1);
		}
		
		return config;
	}
	
	public MsgNode(ClusterManager mgr, String domain,int port) {
		this.mgr = mgr;
		this.domain = domain;
		this.port = port;
	}

	@Override
	public void start() throws Exception {
		final PipeRegistry register = new PipeRegistry(vertx, mgr, domain);

		final SessionManager sm = new SessionManager(register);
		register.installComponent(sm);

		final HypertyAllocationManager alm = new HypertyAllocationManager(register);
		register.installComponent(alm);

		final ObjectAllocationManager olm = new ObjectAllocationManager(register);
		register.installComponent(olm);

		final RegistryConnector rc = new RegistryConnector(register);
		register.installComponent(rc);

		final Pipeline pipeline = new Pipeline(register)
			.addHandler(new ValidatorPipeHandler())
			.addHandler(new TransitionPipeHandler())
			.failHandler(error -> {
				out.println("PIPELINE-FAIL: " + error);
			});


		final JksOptions jksOptions = new JksOptions()
			.setPath("server-keystore.jks")
			.setPassword("rethink2015");


		final HttpServerOptions httpOptions = new HttpServerOptions()
			.setTcpKeepAlive(true)
			.setSsl(true)
			.setKeyStoreOptions(jksOptions);


		final HttpServer server = vertx.createHttpServer(httpOptions);
		server.requestHandler(req -> {
			System.out.println("HTTP-PING");
			req.response().putHeader("content-type", "text/html").end("<html><body><h1>Hello</h1></body></html>");
		});


		WebSocketServer.init(server, pipeline);
		server.listen(port);
		System.out.println("[Message-Node] Running on wss://msg-node." + domain + ":" + port);
	}
}
