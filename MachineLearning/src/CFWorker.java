
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CFWorker extends Thread {

	public int id;
	public static Map<Double,Map<Double,Double>> vt, tv;
	public static Map<Double,Double> vAvg;
	public Map<Double,Double> w;
	public List<Double> testInputKeySet;
	public static double TAE, TSE, procTestSamplesCnt;
	
	public CFWorker() {
		// TODO Auto-generated constructor stub
	}
	
	public void run() {
		System.out.println("Thread"+id+" execution started");
		double TAE = 0, TSE = 0, procTestSamplesCnt = 0, Paj = 0, k = 0, sum = 0, weight = 0; 
		for(double a: testInputKeySet) {
			if(vAvg.get(a) == null) {
				System.out.println("Thread"+id+": Test Sample Cnt: "+procTestSamplesCnt+". No training data available for user a:"+a);
				continue;
			}
			w = new HashMap<Double,Double>();
			for(double j: tv.get(a).keySet()) {
				procTestSamplesCnt++;
				Paj = vAvg.get(a);
				if(vt.get(j) != null) {
					k = 0; sum = 0;
					for(double i: vt.get(j).keySet()) {
						if(w.get(i) != null) {
							weight = w.get(i);
						} else {
							weight = computeWeight(a,i);
						}
						k = k + Math.abs(weight);
						sum = sum + weight*(vt.get(j).get(i) - vAvg.get(i));
					}
					if(k != 0) {
						Paj = Paj + (sum/k);
					}
				}
				TAE = TAE + Math.abs(Paj - tv.get(a).get(j));
				TSE = TSE + Math.pow(Math.abs(Paj - tv.get(a).get(j)), 2);
				System.out.println("Thread"+id+": Test Sample Cnt: "+procTestSamplesCnt+", MAE: "+(TAE/procTestSamplesCnt)+", RMSE: "+(Math.sqrt(TSE)/procTestSamplesCnt));
			}
		}
		finished(TAE, TSE, procTestSamplesCnt);
		System.out.println("Thread"+id+" execution finished");
	}

	private double computeWeight(double a, double i) {
		double d1 = 0, d2 = 0, n1 = 0, val = 0;
		
		for(double j: vt.keySet()) {
			if(vt.get(j).get(a) != null && vt.get(j).get(i) != null) {
				d1 = d1 + Math.pow(vt.get(j).get(a) - vAvg.get(a),2);
				d2 = d2 + Math.pow(vt.get(j).get(i) - vAvg.get(i),2);
				n1 = n1 + (vt.get(j).get(a) - vAvg.get(a))*(vt.get(j).get(i) - vAvg.get(i));
			}
		}
		if(n1 == 0 || d1 == 0 || d2 == 0) {
			val = 0;
		} else {
			val = n1/Math.sqrt(d1*d2);
		}
		
		w.put(i, val);
		
		return val;
	}
	
	private synchronized void finished(double TAE, double TSE, double procTestSamplesCnt) {
		CFWorker.TAE = CFWorker.TAE + TAE;
		CFWorker.TSE = CFWorker.TSE + TSE;
		CFWorker.procTestSamplesCnt = CFWorker.procTestSamplesCnt + procTestSamplesCnt;
	}
	
}
