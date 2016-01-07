package eu.rethink.mn;

public class NodeConfig {
	String selected;
	String domain;
	
	public String getSelected() { return selected; }
	public void setSelected(String selected) { this.selected = selected; }

	public String getDomain() { return domain; }
	public void setDomain(String domain) { this.domain = domain; }
	
	@Override
	public String toString() {
		return "[Config] { selected: " + selected + ", domain: " + domain + "}";
	}
}
