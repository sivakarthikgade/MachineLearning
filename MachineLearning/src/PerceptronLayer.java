
import java.util.ArrayList;
import java.util.List;

enum LayerType {
	HIDDEN,
	OUTPUT
}

public class PerceptronLayer {

	private int id;
	private LayerType type;
	private int size;
	private int paramSize;
	private List<PerceptronUnit> units;
	private static final double DEFAULT_WEIGHT = Math.pow(10, -3);
	
	public PerceptronLayer(int id, LayerType type, int size, int paramSize) {
		this.id = id;
		this.type = type;
		this.size = size;
		this.paramSize = paramSize;
		this.units = new ArrayList<PerceptronUnit>();
	}
	
	public void initializeUnitWeights() {
		for(PerceptronUnit unit: units) {
			for(int i = 0; i < paramSize; i++) {
				unit.getWeights().add(PerceptronLayer.DEFAULT_WEIGHT);
			}
		}
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<PerceptronUnit> getUnits() {
		return units;
	}

	public void setUnits(List<PerceptronUnit> units) {
		this.units = units;
	}

	public int getParamSize() {
		return paramSize;
	}

	public void setParamSize(int paramSize) {
		this.paramSize = paramSize;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public LayerType getType() {
		return type;
	}

	public void setType(LayerType type) {
		this.type = type;
	}

	public static void main(String[] args) {
	}

}
