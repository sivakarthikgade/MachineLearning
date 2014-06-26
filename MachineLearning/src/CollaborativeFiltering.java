
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollaborativeFiltering {

	private static final String LOG_FILE = "CollaborativeFiltering_Log.txt";
	private BufferedWriter bw = null;
	public String trainFile;
	public String testFile;
	public Map<Double,Map<Double,Double>> v, vt, tv;
	public Map<Double,Double> vAvg;
	
	public CollaborativeFiltering(String trainFile, String testFile) {
		this.trainFile = trainFile;
		this.testFile = testFile;
		v = new HashMap<Double,Map<Double,Double>>();
		vt = new HashMap<Double,Map<Double,Double>>();
		tv = new HashMap<Double,Map<Double,Double>>();
		vAvg = new HashMap<Double,Double>();
	}
	
	public void execute() throws Exception {
		String str = null;
		String[] tokens;

		BufferedReader br = new BufferedReader(new FileReader(trainFile));
		while((str = br.readLine()) != null) {
			tokens = str.split("[,\\s]+");
			if(!v.containsKey(Double.parseDouble(tokens[1]))) {
				v.put(Double.parseDouble(tokens[1]), new HashMap<Double,Double>());
			}
			v.get(Double.parseDouble(tokens[1])).put(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[2]));
		}
		br.close();
		log("users size in train set: "+v.keySet().size());
		
		for(double user: v.keySet()) {
			Map<Double,Double> rateList = v.get(user);
			double sum = 0, cnt = 0, avg = 0;
			cnt = rateList.size();
			for(double movie: rateList.keySet()) {
				sum = sum + rateList.get(movie);
			}
			avg = sum/cnt;
			vAvg.put(user,avg);
		}
		log("vAvg size: "+vAvg.size());
		
		v = null;
		br = new BufferedReader(new FileReader(trainFile));
		while((str = br.readLine()) != null) {
			tokens = str.split("[,\\s]+");
			if(!vt.containsKey(Double.parseDouble(tokens[0]))) {
				vt.put(Double.parseDouble(tokens[0]), new HashMap<Double,Double>());
			}
			vt.get(Double.parseDouble(tokens[0])).put(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]));
		}
		br.close();
		log("movies size in train set: "+vt.keySet().size());

		br = new BufferedReader(new FileReader(testFile));
		while((str = br.readLine()) != null) {
			tokens = str.split("[,\\s]+");
			if(!tv.containsKey(Double.parseDouble(tokens[1]))) {
				tv.put(Double.parseDouble(tokens[1]), new HashMap<Double,Double>());
			}
			tv.get(Double.parseDouble(tokens[1])).put(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[2]));
		}
		br.close();
		log("users size in test set: "+tv.keySet().size());
		
		Thread workers[] = new Thread[8];
		CFWorker.vt = this.vt;
		CFWorker.tv = this.tv;
		CFWorker.vAvg = this.vAvg;
		List<Double> temp = new ArrayList<Double>(tv.keySet());
		for(int i = 0; i < 8; i++) {
			workers[i] = new CFWorker();
			((CFWorker)workers[i]).id = i+1;
			((CFWorker)workers[i]).testInputKeySet = temp.subList(i*temp.size()/8, (i+1)*temp.size()/8);
			workers[i].start();
		}

		for(int i=0; i<workers.length; i++){
			try {	
				workers[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		log("Error computation on test set completed!!");
		log("TAE: "+CFWorker.TAE+", TSE: "+CFWorker.TSE+", Processed Test Samples Cnt: "+CFWorker.procTestSamplesCnt);
		log("MAE: "+(CFWorker.TAE/CFWorker.procTestSamplesCnt));
		log("RMSE: "+(Math.sqrt(CFWorker.TSE/CFWorker.procTestSamplesCnt)));
	}
	
	public static void main(String[] args) throws Exception {
		double startTime = System.currentTimeMillis();

		String trainFile = args[0];
		String testFile = args[1];

		CollaborativeFiltering filter = new CollaborativeFiltering(trainFile, testFile);
		filter.initializeLogger(false);
		try {
			filter.execute();
			filter.log("CollaborativeFiltering: Total execution time->"+((double)(System.currentTimeMillis()-startTime)/1000)+" secs.");
		} catch(Exception e) {
			throw e;
		} finally {
			filter.flushLogger();
			filter.closeLogger();
		}
	}

	private void initializeLogger(boolean append) throws Exception {
		bw = new BufferedWriter(new FileWriter(CollaborativeFiltering.LOG_FILE, append));
	}
	
	private void log(String str) throws Exception {
		System.out.println(str);
		bw.write(str);
		bw.newLine();
	}
	
	private void flushLogger() throws Exception {
		bw.flush();
	}
	
	private void closeLogger() throws Exception {
		bw.flush();
		bw.close();
	}
}
