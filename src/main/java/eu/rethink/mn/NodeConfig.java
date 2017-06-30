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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author micaelpedrosa@gmail.com
 * Main class representing the config model from node.config.json structure or environment variables.
 */
public class NodeConfig {
	static final Logger logger = LoggerFactory.getLogger("BROKER");
	String selected = null;
	String domain = null;
	int port = 0;

	String registryUrl = null;
	String globalRegistryUrl = null;

	int registryRetries = 0;
	boolean registrySSL = false;
	String registryTrustStore = null;
	String registryTrustStorePass = null;
	String keyStore= null;
	String keyStorePass = null;
	String keyPassPhrase = null;

	boolean registryOwnCertificates = false;

    public boolean isRegistryOwnCertificates() { return registryOwnCertificates; }
    public void setRegistryOwnCertificates(boolean registryOwnCertificates) { this.registryOwnCertificates = registryOwnCertificates; }

    public String getRegistryTrustStore() { return registryTrustStore; }
    public void setRegistryTrustStore(String registryTrustStore) { this.registryTrustStore = registryTrustStore; }

    public String getRegistryTrustStorePass() { return registryTrustStorePass; }
    public void setRegistryTrustStorePass(String registryTrustStorePass) { this.registryTrustStorePass = registryTrustStorePass; }

    public String getKeyStore() { return keyStore; }
    public void setKeyStore(String keyStore) { this.keyStore = keyStore; }

    public String getKeyStorePass() { return keyStorePass; }
    public void setKeyStorePass(String keyStorePass) { this.keyStorePass = keyStorePass; }

    public String getKeyPassPhrase() { return keyPassPhrase; }
    public void setKeyPassPhrase(String keyPassPhrase) { this.keyPassPhrase = keyPassPhrase; }

	public boolean getRegistrySSL(){ return registrySSL; }
	public void setRegistrySSL(boolean registrySSL) { this.registrySSL = registrySSL;}

	public int getRegistryRetries() { return registryRetries; }
	public void setRegistryRetries(int registryRetries) { this.registryRetries = registryRetries; }

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

	/** Read configuration from JSON file
	 * @param path Relative path to file
	 * @param selection Select the JSON subnode (dev, prod, ...). "env" selection reserved for environment variables.
	 * @return Data structure with configurations
	 */
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

	/** Read configuration from environment variables.
	 * Variable names: NODE_DOMAIN, NODE_PORT, NODE_REGISTRY_URL, NODE_GLOBAL_REGISTRY_URL
	 * @return Data structure with configurations
	 */
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

		    final String registrySsl = System.getenv("NODE_REGISTRY_SSL");
		    if (registrySsl == null) {
		    	System.out.println("[Config] NODE_REGISTRY_SSL variable not found!");
		    	System.exit(-1);
		    }
		    config.setRegistrySSL(Boolean.parseBoolean((registrySsl)));

		    final String registryRetries = System.getenv("NODE_REGISTRY_RETRIES");
		    if (registryRetries == null) {
		    	System.out.println("[Config] NODE_REGISTRY_RETRIES variable not found!");
		    	System.exit(-1);
		    }
		    config.setRegistryRetries(Integer.parseInt(registryRetries));

		    config.setGlobalRegistryUrl(System.getenv("NODE_GLOBAL_REGISTRY_URL"));
		    if (config.getGlobalRegistryUrl() == null) {
		    	System.out.println("[Config] NODE_GLOBAL_REGISTRY_URL variable not found!");
		    	System.exit(-1);
		    }

		    final String registryOwnCerts = System.getenv("NODE_REGISTRY_OWN_CERTIFICATES");
		    if (registryOwnCerts == null) {
		    	System.out.println("[Config] NODE_REGISTRY_OWN_CERTIFICATES variable not found!");
		    	System.exit(-1);
		    }
		    config.setRegistryOwnCertificates(Boolean.parseBoolean((registryOwnCerts)));


		    config.setRegistryTrustStore(System.getenv("NODE_REGISTRY_TRUST_STORE"));
		    if (config.getRegistryTrustStore() == null && config.getRegistrySSL() && config.isRegistryOwnCertificates()) {
		    	System.out.println("[Config] NODE_REGISTRY_TRUST_STORE variable not found!");
		    	System.exit(-1);
		    }

		    config.setRegistryTrustStorePass(System.getenv("NODE_REGISTRY_TRUST_STORE_PASS"));
		    if (config.getRegistryTrustStorePass() == null && config.getRegistrySSL() && config.isRegistryOwnCertificates()) {
		    	System.out.println("[Config] NODE_REGISTRY_TRUST_STORE_PASS variable not found!");
		    	System.exit(-1);
		    }

		    config.setKeyStore(System.getenv("NODE_REGISTRY_KEY_STORE"));
		    if (config.getKeyStore() == null && config.getRegistrySSL() && config.isRegistryOwnCertificates()) {
		    	System.out.println("[Config] NODE_REGISTRY_KEY_STORE variable not found!");
		    	System.exit(-1);
		    }

		    config.setKeyStorePass(System.getenv("NODE_REGISTRY_KEY_STORE_PASS"));
		    if (config.getKeyStorePass() == null && config.getRegistrySSL() && config.isRegistryOwnCertificates()) {
		    	System.out.println("[Config] NODE_REGISTRY_KEY_STORE_PASS variable not found!");
		    	System.exit(-1);
		    }
		    config.setKeyPassPhrase(System.getenv("NODE_REGISTRY_KEY_PASS_PHRASE"));
		    if (config.getKeyPassPhrase() == null && config.getRegistrySSL() && config.isRegistryOwnCertificates()) {
		    	System.out.println("[Config] NODE_REGISTRY_KEY_PASS_PHRASE variable not found!");
		    	System.exit(-1);
		    }

		} catch (Exception e) {
		    e.printStackTrace();
		    System.exit(-1);
		}
		logger.info("CONFIG:    " + config.toString() );
		return config;
	}
	@Override
	public String toString() {
		return "{ selected: " + selected + ", domain: " + domain + ", port: " + port  + ", registry.url: " + registryUrl + ", globalregistry.url: " + globalRegistryUrl + "}";
	}

}
