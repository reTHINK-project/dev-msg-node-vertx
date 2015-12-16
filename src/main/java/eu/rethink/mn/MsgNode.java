package eu.rethink.mn;

import static java.lang.System.out;
import eu.rethink.mn.component.HypertyAllocationManager;
import eu.rethink.mn.component.ObjectAllocationManager;
import eu.rethink.mn.component.SessionManager;
import eu.rethink.mn.pipeline.PipeRegistry;
import eu.rethink.mn.pipeline.Pipeline;
import eu.rethink.mn.pipeline.handlers.TransitionPipeHandler;
import eu.rethink.mn.pipeline.handlers.ValidatorPipeHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class MsgNode extends AbstractVerticle {
	
	public static void main(String[] args) {
		if (args.length == 2) {
			try {
				final String domain = args[0];
				final int port = Integer.parseInt(args[1]);
				
				final ClusterManager mgr = new HazelcastClusterManager();
				final MsgNode msgNode = new MsgNode(mgr, domain, port);
				
				final VertxOptions options = new VertxOptions().setClusterManager(mgr);
				Vertx.clusteredVertx(options, res -> {
					if (res.succeeded()) {
						Vertx vertx = res.result();
						vertx.deployVerticle(msgNode);
					} else {
						System.exit(-1);
					}
				});
				
			} catch (Exception e) {
				System.out.println("usage: <domain> <port>");
				System.exit(-1);
			}
		} else {
			System.out.println("usage: <domain> <port>");
		}
	}
	
	private final ClusterManager mgr;
	private final String domain;
	private final int port;
	
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
		
		
		final Pipeline pipeline = new Pipeline(register)
			.addHandler(new ValidatorPipeHandler())
			.addHandler(new TransitionPipeHandler())
			.failHandler(error -> {
				out.println("PIPELINE-FAIL: " + error);
			});
		
		/*
		final JksOptions jksOptions = new JksOptions()
			.setPath("./rethink-keystore.jks")
			.setPassword("<password-of-your-keystore>");
		*/
		
		final HttpServerOptions httpOptions = new HttpServerOptions()
			.setTcpKeepAlive(true);
		/*	.setSsl(true)
			.setKeyStoreOptions(jksOptions);
		*/

		final HttpServer server = vertx.createHttpServer(httpOptions);
		WebSocketServer.init(server, pipeline);
		server.listen(port);
		System.out.println("Running ws://msg-node." + domain + ":" + port);
	}
}
