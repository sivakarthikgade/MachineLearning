
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class EM {

	public List<Double> inputVal;
	public static int N;
	public static final int K = 3;
	public static final int D = 1;
	public EMCluster[] cluster;
	
	public EM() {
		inputVal = new ArrayList<Double>();
		cluster = new EMCluster[K];
	}
	
	public void run(String inputFile, int constantVariance) throws Exception {

		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String str = null;
		while((str=br.readLine())!=null) {
			inputVal.add(Double.parseDouble(str));
		}
		br.close();
		
		N = inputVal.size();
		
		double inputValMean = 0, inputValVariance = 0;
		for(int i = 0; i < inputVal.size(); i++) {
			inputValMean+=inputVal.get(i);
		}
		inputValMean/=inputVal.size();
		
		for(int i = 0; i < inputVal.size(); i++) {
			inputValVariance+=Math.pow(inputVal.get(i)-inputValMean,2);
		}
		inputValVariance/=inputVal.size();
		
		for(int i = 0; i < K; i++) {
			if(constantVariance == -1) {
				int mf = (int) Math.floor(Math.random()*15);
				cluster[i] = new EMCluster(((double)1/K), inputVal.get((int)Math.floor(Math.random()*inputVal.size())), (mf+1)*inputValVariance);
			} else {
				cluster[i] = new EMCluster(((double)1/K), inputVal.get((int)Math.floor(Math.random()*inputVal.size())), constantVariance);
			}
		}
		
		log("Initialization parameters in this run");
		for(int i = 1; i <= K; i++) {
//			log("Cluster"+i+"-> alpha: "+cluster[i-1].alpha+"; mean: "+cluster[i-1].mean+"; variance: "+cluster[i-1].variance);
			log("("+i+", "+cluster[i-1].alpha+", "+cluster[i-1].mean+", "+cluster[i-1].variance+")");
		}
		
//		int cnt = 0;
		double oldLogLH = 0, newLogLH = 0;
		do {
			oldLogLH = newLogLH;
			execEStep();
			execMStep();
			newLogLH = computeLogLikelihood();
//    		cnt++;
//    		if(cnt%10 == 0) {
//    			log("# iterations done: "+cnt);
//    		}
		} while(oldLogLH != newLogLH);
		
		log("Final parameters in this run after stabilization");
		for(int i = 1; i <= K; i++) {
//			log("Cluster"+i+"-> alpha: "+cluster[i-1].alpha+"; mean: "+cluster[i-1].mean+"; variance: "+cluster[i-1].variance);
			log("("+i+", "+cluster[i-1].alpha+", "+cluster[i-1].mean+", "+cluster[i-1].variance+")");
		}
		log("Loglikelihood achieved: "+newLogLH);
	}
	
	public void execEStep() {
		computeP();
		computeW();
	}
	
	private void computeP() {
		for(int k = 0; k < K; k++) {
			for(int i = 0; i < N; i++) {
				cluster[k].p[i] = (1/Math.sqrt(Math.pow(2*Math.PI, D)*Math.abs(cluster[k].variance)))
								*Math.exp((-0.5*(inputVal.get(i) - cluster[k].mean)*(inputVal.get(i) - cluster[k].mean))/cluster[k].variance);
			}
		}
	}
	
	private void computeW() {
		for(int i = 0; i < N; i++) {
			double denominator = 0;
			for(int k = 0; k < K; k++) {
				denominator+=(cluster[k].p[i]*cluster[k].mean);
			}
			for(int k = 0; k < K; k++) {
				cluster[k].w[i] = cluster[k].p[i]*cluster[k].mean/denominator; 
			}
		}
	}
	
	public void execMStep() {
		for(int k = 0; k < K; k++) {
			double Nk = 0;
			double WkX = 0;
			for(int i = 0; i < N; i++) {
				Nk+=cluster[k].w[i];
				WkX+=(cluster[k].w[i]*inputVal.get(i));
			}
			cluster[k].alpha = Nk/N;
			cluster[k].mean = WkX/Nk;
			
			double WkXM = 0;
			for(int i = 0; i < N; i++) {
				WkXM+=(cluster[k].w[i]*(inputVal.get(i) - cluster[k].mean)*(inputVal.get(i) - cluster[k].mean));
			}
			cluster[k].variance = WkXM/Nk;
		}
	}
	
	public double computeLogLikelihood() {
		double logLH = 0, val = 0;
		for(int i = 0; i < N; i++) {
			val = 0;
			for(int k = 0; k < K; k++) {
				val+=(cluster[k].mean*cluster[k].p[i]);
			}
			logLH+=Math.log(val);
		}
		return logLH;
	}
	
	public static void main(String args[]) throws Exception {
		EM e = new EM();
		String inputFile = args[0];
		int constantVariance = Integer.parseInt(args[1]);
		e.run(inputFile, constantVariance);
	}
	
	private void log(String message) {
		System.out.println(message);
	}

}
