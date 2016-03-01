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

package eu.rethink.mn.pipeline.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import io.vertx.core.Handler;
import eu.rethink.mn.pipeline.PipeContext;
import eu.rethink.mn.pipeline.message.PipeMessage;

public class MsgOrderPipeHandler implements Handler<PipeContext> {
	final Map<String, SortedMap<Integer, PipeContext>> queues = new HashMap<>();
	final Map<String, Integer> lastMsg = new HashMap<>();
	
	@Override
	public void handle(PipeContext ctx) {
		//TODO: stall on message deliver-fail...
		final PipeMessage msg = ctx.getMessage();
		
		final int id = msg.getId();
		final String from = msg.getFrom();
		final String type = msg.getType();
		
		//reply's do not count for sequence...
		if(type.equals("reply")) {
			ctx.next();
			return;
		}
		
		SortedMap<Integer, PipeContext> queue = queues.get(from);
		if(queue == null) {
			queue = new TreeMap<>();
			queues.put(from, queue);
			
			lastMsg.put(from, 0);
		}
		
		queue.put(id, ctx);
		for(int key: queue.keySet()) {
			int last = lastMsg.get(from);
			if(key == last + 1) {
				final PipeContext nextCtx = queue.get(key);
				nextCtx.next();
				lastMsg.put(from, last + 1);
			}			
		}
	}
}
