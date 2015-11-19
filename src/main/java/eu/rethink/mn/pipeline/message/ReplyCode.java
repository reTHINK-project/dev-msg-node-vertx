package eu.rethink.mn.pipeline.message;

public enum ReplyCode {
	OK(200, "OK"), ERROR(500, "Internal Server Error"); 
	
	int code;
	String desc;
	
	public int getCode() { return code; }
	public String getDesc() { return desc; }
	
	ReplyCode(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}
}
