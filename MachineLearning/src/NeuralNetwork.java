
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NeuralNetwork {

	private static final String LOG_FILE = "NeuralNetwork_Log.txt";
	private BufferedWriter bw = null;
	private List<PerceptronLayer> unitLayers;
	private static final int MAX_CONVERGENCE_LIMIT = 1000;

	public NeuralNetwork() {
		unitLayers = new ArrayList<PerceptronLayer>();
	}
	
	public void createNetworkStructure() {
		unitLayers.add(new PerceptronLayer(0, LayerType.HIDDEN, 3, 8));
		unitLayers.add(new PerceptronLayer(1, LayerType.OUTPUT, 8, 3));
		
		for(PerceptronLayer l: unitLayers) {
			for(int i = 0; i < l.getSize(); i++) {
				l.getUnits().add(new PerceptronUnit(i, (l.getType()==LayerType.HIDDEN?"h":"o")+i, (l.getType()==LayerType.HIDDEN?UnitType.HIDDEN:UnitType.OUTPUT)));
			}
			l.initializeUnitWeights();
		}
	}
	
	public void train() throws Exception {
		for(int i = 0; i < NeuralNetwork.MAX_CONVERGENCE_LIMIT; i++) {
			log("Iteration: "+i,true);
			for(int j = 0; j < 8; j++) {
				List<Double> input = getBinaryInput(Math.pow(2,j));
				List<Double> output = getBinaryInput(Math.pow(2,j));
	
				log("INPUT: ",false);
				for(int z = 0; z < input.size(); z++) {
					log(input.get(z)+",",false);
				}
				log("",true);
				log("OUTPUT: ",false);
				for(int z = 0; z < input.size(); z++) {
					log(output.get(z)+",",false);
				}
				log("",true);
				
				for(PerceptronLayer l: unitLayers) {
					int cnt=0;
					for(PerceptronUnit u: l.getUnits()) {
						u.setFeatures(input);
						u.computeNetd();
						u.computeOd();
						if(l.getType() == LayerType.OUTPUT) {
							u.setTd(output.get(cnt++));
						}
					}
					input = new ArrayList<Double>();
					for(PerceptronUnit u: l.getUnits()) {
						input.add(u.getOd());
					}
				}
				
				//evaluate output and perform back propagation of delta
				Collections.reverse(unitLayers);
				for(PerceptronLayer l: unitLayers) {
					for(PerceptronUnit u: l.getUnits()) {
						double diff = 0;
						if(l.getType()==LayerType.HIDDEN) {
							PerceptronLayer l2 = unitLayers.get((unitLayers.size()-1 - l.getId() - 1));
							for(PerceptronUnit u2: l2.getUnits()) {
								diff = diff + u2.getWeights().get(u.getId())*u2.getDelta();
							}
						}
						u.computeDelta(diff);
					}
				}
				Collections.reverse(unitLayers);
				
				for(PerceptronLayer l: unitLayers) {
					for(PerceptronUnit u: l.getUnits()) {
						u.updateWeightsBasedOnFeedBack();
					}
				}
				
				test();
			}
		}
	}
	
	public void test() throws Exception {
		for(PerceptronUnit u: unitLayers.get(unitLayers.size()-1).getUnits()) {
			log(u.getOd()+",",false);
		}
		log("",true);
	}

	public static void main(String[] args) throws Exception {
		double startTime = System.currentTimeMillis();
		NeuralNetwork network = new NeuralNetwork();
		network.initializeLogger(false);
		network.createNetworkStructure();
		network.train();
//		network.test();
		network.log("SpamFilter: Total execution time->"+((double)(System.currentTimeMillis()-startTime)/1000)+" secs.",true);
		network.flushLogger();
		network.closeLogger();
	}

	private List<Double> getBinaryInput(double val) {
		List<Double> l = new ArrayList<Double>();
		while(val >= 1) {
			l.add((val%2)+0.0);
			val=val/2;
		}
		while(l.size() < 8) {
			l.add(0.0);
		}
		Collections.reverse(l);
		return l;
	}
	
	private void initializeLogger(boolean append) throws Exception {
		bw = new BufferedWriter(new FileWriter(NeuralNetwork.LOG_FILE, append));
	}
	
	private void log(String str, boolean newLine) throws Exception {
		System.out.print(str);
		bw.write(str);
		if(newLine) {
			System.out.println();
			bw.newLine();
		}
	}
	
	private void flushLogger() throws Exception {
		bw.flush();
	}
	
	private void closeLogger() throws Exception {
		bw.flush();
		bw.close();
	}
}
