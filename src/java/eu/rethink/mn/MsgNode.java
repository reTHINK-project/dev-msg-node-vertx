package eu.rethink.mn;

import static java.lang.System.out;
import eu.rethink.mn.pipeline.PipeMessage;
import eu.rethink.mn.pipeline.PipeRegistry;
import eu.rethink.mn.pipeline.Pipeline;
import eu.rethink.mn.pipeline.handlers.ValidatorPipeHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;

public class MsgNode extends AbstractVerticle {
	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new MsgNode());
	}
	
	@Override
	public void start() throws Exception {
		final PipeRegistry register = new PipeRegistry(vertx);
		
		register.install("mn:/session", ctx -> {
			final PipeMessage msg = ctx.getMessage();
			System.out.println(msg);

			if(msg.getType().equals("open")) {
				register.bind(msg.getFrom(), ctx.getResourceUid());
				ctx.replyOK("mn:/session");
			}
			
			if(msg.getType().equals("close")) {
				register.unbind(msg.getFrom());
				ctx.disconnect();
			}
		});
		
		register.install("mn:/register", ctx -> {
			final PipeMessage msg = ctx.getMessage();
			System.out.println(msg);
			
			final String url = msg.getBody().getString("url");
			if(url != null) {
				ctx.fail("mn:/register", "No url present in body!");
				return;
			}
			
			if(msg.getType().equals("add")) {
				register.bind(url, ctx.getResourceUid());
				ctx.replyOK("mn:/register");
			}
			
			if(msg.getType().equals("remove")) {
				register.unbind(url);
				ctx.replyOK("mn:/register");
			}
		});

		final Pipeline pipeline = new Pipeline(register)
			.addHandler(new ValidatorPipeHandler())
			.failHandler(error -> {
				out.println("PIPELINE-FAIL: " + error);
			});
		
		final HttpServerOptions httpOptions = new HttpServerOptions();
		httpOptions.setTcpKeepAlive(true);
		
		final HttpServer server = vertx.createHttpServer(httpOptions);
		WebSocketServer.init(server, pipeline);
		server.listen(9090);
		System.out.println("Message Node -> port(9090)");
	}
}
