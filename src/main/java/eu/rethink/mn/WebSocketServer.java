package eu.rethink.mn;

import static java.lang.System.out;
import eu.rethink.mn.pipeline.Pipeline;
import eu.rethink.mn.pipeline.PipeResource;
import eu.rethink.mn.pipeline.PipeMessage;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;

public class WebSocketServer {
	
	public static void init(HttpServer server, Pipeline pipeline) {
		server.websocketHandler(ws -> {
			if(!ws.uri().equals("/ws")) {
				ws.reject();
			}
			
			out.println("RESOURCE-OPEN");
			final PipeResource resource = pipeline.createResource(ws.textHandlerID(),
				close -> {
					ws.close();
				},
				reply -> {
					ws.writeFinalTextFrame(reply);
				}
			);
			
			ws.frameHandler(frame -> {
				final JsonObject msg = new JsonObject(frame.textData());
				resource.processMessage(new PipeMessage(msg));
			});
						
			ws.closeHandler(handler -> {
				out.println("RESOURCE-CLOSE");
			});
		});
	}
}
