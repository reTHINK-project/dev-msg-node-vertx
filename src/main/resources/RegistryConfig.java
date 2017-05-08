
public class RegistryConfig {

	String registryUrl = null;
	boolean registrySSL = false;
	int registryRetries = 0;
	
	public boolean getRegistrySSL(){ return registrySSL; }
	public void setRegistrySSL(boolean registrySSL) { this.registrySSL = registrySSL;}
	
	public int getRegistryRetries() { return registryRetries; }
	public void setRegistryRetries(int registryRetries) { this.registryRetries = registryRetries; }
	
	public void setRegistryUrl(String registryUrl) { this.registryUrl = registryUrl; }
	public String getRegistryUrl() { return registryUrl; }
}
