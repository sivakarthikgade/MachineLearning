
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Cluster {
	
	public Color mean;
	public List<Integer> pixels;

	public Cluster() {
		// TODO Auto-generated constructor stub
	}

	public Cluster(Color mean) {
		this.mean = mean;
		this.pixels = new ArrayList<Integer>();
	}
	
	public void resetPixels() {
		this.pixels = new ArrayList<Integer>();
	}
}
