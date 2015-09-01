package lu.uni.snt.reflection6;

public class ConcreteClass extends BaseClass {

	public String foo() {
		return imei;
	}
	
	public ConcreteClass(String imei)
	{
		this.imei = imei;
	}
}
