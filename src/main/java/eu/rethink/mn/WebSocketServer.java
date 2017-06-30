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
import eu.rethink.mn.pipeline.Pipeline;
import eu.rethink.mn.pipeline.PipeResource;
import eu.rethink.mn.pipeline.message.PipeMessage;
import io.vertx.core.http.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author micaelpedrosa@gmail.com
 * Initialize the link beetween HttpServer and Pipeline. Used to create resources for every WS connection.
 */
public class WebSocketServer {
	static final Logger logger = LoggerFactory.getLogger("BROKER");
	public static void init(HttpServer server, Pipeline pipeline) {
		server.websocketHandler(ws -> {
			if(!ws.uri().equals("/ws")) {
				ws.reject();
				logger.info("RESOURCE-OPEN-REJECTED");
				return;
			}

			final StringBuilder sb = new StringBuilder();

			logger.info("RESOURCE-OPEN");
			final PipeResource resource = pipeline.createResource(
				ws.textHandlerID(),
				close -> ws.close(),
				reply -> ws.writeFinalTextFrame(reply)
			);

			ws.frameHandler(frame -> {
				sb.append(frame.textData());

				if (frame.isFinal()) {
					resource.processMessage(new PipeMessage(sb.toString()));
					sb.setLength(0);
				}
			});

			ws.closeHandler(handler -> {
				logger.info("RESOURCE-CLOSE");
			});
		});
	}
}
