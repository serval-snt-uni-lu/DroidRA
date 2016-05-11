package lu.uni.snt.reflection12;

public class ConcreteClass extends BaseClass {

	private String imei = "";
	
	public void setImei(String imei) {
		this.imei = imei;
	}
	
	public String getImei() {
		return this.imei;
	}
	
	public ConcreteClass(String imei)
	{
		//this.imei = imei;
	}
}
