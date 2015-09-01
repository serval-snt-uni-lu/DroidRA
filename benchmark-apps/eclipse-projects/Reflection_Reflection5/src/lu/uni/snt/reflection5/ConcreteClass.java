package lu.uni.snt.reflection5;

public class ConcreteClass extends BaseClass {

	public String foo() {
		return imei;
	}
	
	public ConcreteClass(String imei)
	{
		this.imei = imei;
	}
}
