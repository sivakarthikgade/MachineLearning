
import java.util.ArrayList;
import java.util.List;

enum UnitType {
	HIDDEN,
	OUTPUT
}
public class PerceptronUnit {

	private int id;
	private String name;
	private UnitType type;
	private List<Double> features;
	private List<Double> weights;
	private double netd;
	private double od;
	private double delta;
	private double td;
	private static double MIN_VALUE = Double.MIN_VALUE*Math.pow(2, 574);
	private static double EETA = 0.5;
	
	public PerceptronUnit() {
	}
	
	public PerceptronUnit(int id, String name, UnitType type) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.features = new ArrayList<Double>();
		this.weights = new ArrayList<Double>();
	}
	
	public double computeNetd() {
		if(features == null || weights == null || features.size() != weights.size()) {
			throw new IllegalArgumentException("Attributes of Unit "+name+" are not set properly. Please check.");
		}
		
		double netd = 0;
		for(int i = 0; i < features.size(); i++) {
			netd = netd + getValue(features.get(i)*weights.get(i));
		}
		setNetd(netd);
		return getNetd();
	}
	
	public double computeOd() {
		if(features == null || weights == null || features.size() != weights.size()) {
			throw new IllegalArgumentException("Attributes of Unit "+name+" are not set properly. Please check.");
		}
		
		double od = 0;
		od = getValue(Math.exp(-netd));
		od = 1 + od;
		od = getValue((double)1/od);
		setOd(od);
		return getOd();
	}
	
	public double computeDelta(double diff) {
		if(features == null || weights == null || features.size() != weights.size()) {
			throw new IllegalArgumentException("Attributes of Unit "+name+" are not set properly. Please check.");
		}
		
		double delta = 0;
		if(type == UnitType.OUTPUT) {
			delta = getOd()*(1-getOd())*(getTd()-getOd());
		} else {
			delta = getOd()*(1-getOd())*diff;
		}
		setDelta(delta);
		return getDelta();
	}
	
	public void updateWeightsBasedOnFeedBack() {
		for(int i = 0; i < weights.size(); i++) {
			weights.set(i, weights.get(i)+(EETA*delta*features.get(i)));
		}
	}
	
	private double getValue(double b) {
		if(Math.abs(b) < PerceptronUnit.MIN_VALUE)
			return PerceptronUnit.MIN_VALUE;
		else
			return b;
	}

	public double getTd() {
		return td;
	}

	public UnitType getType() {
		return type;
	}

	public void setType(UnitType type) {
		this.type = type;
	}

	public void setTd(double td) {
		this.td = td;
	}

	public double getDelta() {
		return delta;
	}

	public void setDelta(double delta) {
		this.delta = delta;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getOd() {
		return od;
	}

	public void setOd(double od) {
		this.od = od;
	}

	public double getNetd() {
		return netd;
	}

	public void setNetd(double netd) {
		this.netd = netd;
	}

	public List<Double> getWeights() {
		return weights;
	}

	public void setWeights(List<Double> weights) {
		this.weights = weights;
	}

	public List<Double> getFeatures() {
		return features;
	}

	public void setFeatures(List<Double> features) {
		this.features = features;
	}

	public static void main(String[] args) {
	}

}
