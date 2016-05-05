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

import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NodeConfig {
	String selected = null;
	String domain = null;
	int port = 0;
	
	String registryUrl = null;
	String globalRegistryUrl = null;
	
	public String getSelected() { return selected; }
	public void setSelected(String selected) { this.selected = selected; }

	public String getDomain() { return domain; }
	public void setDomain(String domain) { this.domain = domain; }
	
	public int getPort() { return port; }
	public void setPort(int port) { this.port = port; }
	
	public void setRegistryUrl(String registryUrl) { this.registryUrl = registryUrl; }
	public String getRegistryUrl() { return registryUrl; }
	
	public String getGlobalRegistryUrl() { return globalRegistryUrl; }
	public void setGlobalRegistryUrl(String globalRegistryUrl) { this.globalRegistryUrl = globalRegistryUrl; }
	
	public static NodeConfig readFromFile(String path, String selection) {
		final ObjectMapper objectMapper = new ObjectMapper();
		final NodeConfig config = new NodeConfig();

		try {
			config.setSelected(selection);
			
			final File file = new File(path);
		    final JsonNode node = objectMapper.readValue(file, JsonNode.class);
		    
		    System.out.println("[Config] File Found");
		    
		    final JsonNode selectedNode =  node.get(selection);
		    if (selectedNode == null) {
		    	System.out.println("[Config] No " + selection + " field found!");
		    	System.exit(-1);
		    }
		    
		    final JsonNode domainNode = selectedNode.get("domain");
		    if (domainNode == null) {
		    	System.out.println("[Config] No " + selection + ".domain field found!");
		    	System.exit(-1);
		    }
		    config.setDomain(domainNode.asText());
		    
		    final JsonNode portNode = selectedNode.get("port");
		    if (portNode == null) {
		    	System.out.println("[Config] No " + selection + ".port field found!");
		    	System.exit(-1);
		    }
		    config.setPort(portNode.asInt());
		    
		    final JsonNode registryNode = selectedNode.get("registry");
		    if (registryNode == null) {
		    	System.out.println("[Config] No " + selection + ".registry field found!");
		    	System.exit(-1);
		    }
		    
		    final JsonNode registryNodeUrl = registryNode.get("url");
		    if (registryNodeUrl == null) {
		    	System.out.println("[Config] No " + selection + ".registry.url field found!");
		    	System.exit(-1);
		    }
		    config.setRegistryUrl(registryNodeUrl.asText());
		    
		    final JsonNode globalregistryNode = selectedNode.get("globalregistry");
		    if (globalregistryNode == null) {
		    	System.out.println("[Config] No " + selection + ".globalregistry field found!");
		    	System.exit(-1);
		    }
		    
		    final JsonNode globalregistryNodeUrl = globalregistryNode.get("url");
		    if (globalregistryNodeUrl == null) {
		    	System.out.println("[Config] No " + selection + ".globalregistry.url field found!");
		    	System.exit(-1);
		    }
		    config.setGlobalRegistryUrl(globalregistryNodeUrl.asText());

		} catch (Exception e) {
		    e.printStackTrace();
		    System.exit(-1);
		}
		
		return config;
	}
	
	public static NodeConfig readFromEnvironment() {
		final NodeConfig config = new NodeConfig();
		
		try {
			config.setSelected("env");
			
			config.setDomain(System.getenv("NODE_DOMAIN"));
		    if (config.getDomain() == null) {
		    	System.out.println("[Config] NODE_DOMAIN variable not found!");
		    	System.exit(-1);
		    }
			
		    final String nodePort = System.getenv("NODE_PORT");
		    if (nodePort == null) {
		    	System.out.println("[Config] NODE_PORT variable not found!");
		    	System.exit(-1);
		    }
		    config.setPort(Integer.parseInt(nodePort));

		    config.setRegistryUrl(System.getenv("NODE_REGISTRY_URL"));
		    if (config.getRegistryUrl() == null) {
		    	System.out.println("[Config] NODE_REGISTRY_URL variable not found!");
		    	System.exit(-1);
		    }
		    
		    config.setGlobalRegistryUrl(System.getenv("NODE_GLOBAL_REGISTRY_URL"));
		    if (config.getGlobalRegistryUrl() == null) {
		    	System.out.println("[Config] NODE_GLOBAL_REGISTRY_URL variable not found!");
		    	System.exit(-1);
		    }
		    
		} catch (Exception e) {
		    e.printStackTrace();
		    System.exit(-1);
		}
		    
		return config;
	}
	
	@Override
	public String toString() {
		return "{ selected: " + selected + ", domain: " + domain + ", port: " + port  + ", registry.url: " + registryUrl + ", globalregistry.url: " + globalRegistryUrl + "}";
	}
}
