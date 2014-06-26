
public class EMCluster {
	
	public double alpha;
	public double mean;
	public double variance;
	public double[] p;
	public double[] w;

	public EMCluster() {
		p = new double[EM.N];
		w = new double[EM.N];
	}
	
	public EMCluster(double alpha, double mean, double variance) {
		p = new double[EM.N];
		w = new double[EM.N];

		this.alpha = alpha;
		this.mean = mean;
		this.variance = variance;
	}
}
