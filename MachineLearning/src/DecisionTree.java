
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

enum ENTROPY_CONSTANTS {
	INFORMATION_GAIN,
	VARIANCE_IMPURITY
};

public class DecisionTree {
	
	private int[][] trainingData, testData, validationData;
	private Map<Integer,String> decisionTree = new HashMap<Integer,String>();
	private Map<Integer,String> headerRow = new HashMap<Integer,String>();
	private Map<String,Integer> headerRowReverseMapping = new HashMap<String,Integer>();
	private String LOG_FILE_NAME = "DecisionTree_Log.txt";
	private BufferedWriter logWriter;
	
	public Map<Integer,String> getHeaderRow() {
		return this.headerRow;
	}
	
	public void setHeaderRow(Map<Integer,String> headerRow) {
		this.headerRow = headerRow;
	}
	
	public Map<String,Integer> getHeaderRowReverseMapping() {
		return this.headerRowReverseMapping;
	}
	
	public void setHeaderRowReverseMapping(Map<String,Integer> headerRowReverseMapping) {
		this.headerRowReverseMapping = headerRowReverseMapping;
	}
	
	public int[][] getTrainingData() {
		return this.trainingData;
	}
	
	public void setTrainingData(int[][] trainingData) {
		this.trainingData = trainingData;
	}
	
	public int[][] getTestData() {
		return this.testData;
	}
	
	public void setTestData(int[][] testData) {
		this.testData = testData;
	}
	
	public int[][] getValidationData() {
		return this.validationData;
	}
	
	public void setValidationData(int[][] validationData) {
		this.validationData = validationData;
	}
	
	public Map<Integer,String> getDecisionTree() {
		return this.decisionTree;
	}

	public void setDecisionTree(Map<Integer,String> decisionTree) {
		this.decisionTree = decisionTree;
	}

	public DecisionTree() throws IOException {
		initializeLogger();
		//log("In the constructor of DecisionTree");
	}

	private int[][] readInput(String fileName) throws IOException {
		int[][] input;
		//log("Begin DecisionTree.readInput()");

		int height = 0, width = 0;
		String record = null;
		
		//Initialize headerRow and trainingData. Populate headerRow
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		
		String header = br.readLine();
		while(br.readLine() != null)
			height++;
		
		br.close();
		
		StringTokenizer st = new StringTokenizer(header,",");
		int i = 0;
		String str = null;
		while(st.hasMoreTokens()) {
			str = st.nextToken();
			this.headerRow.put(i,str);
			this.headerRowReverseMapping.put(str, i++);
		}
		
		width = this.headerRow.size();
		
		input = new int[height][width];
		
		//Populate training data
		br = new BufferedReader(new FileReader(fileName));
		
		br.readLine();
		
		height = 0;
		while((record = br.readLine()) != null) {
			width = 0;
			st = new StringTokenizer(record,",");
			while(st.hasMoreTokens()) {
				input[height][width++] = Integer.parseInt(st.nextToken());
			}
			height++;
		}
		
		//log("End DecisionTree.readInput()");
		
		return input;
	}
	
	public void initializeAndPopulateInputDataHolders(Map<String,String> dataSetsPath) throws IOException {

		//log("Begin DecisionTree.initializeAndPopulateInputHolders()");

		setTrainingData(readInput(dataSetsPath.get("trainingDataPath")));
		setValidationData(readInput(dataSetsPath.get("validationDataPath")));
		setTestData(readInput(dataSetsPath.get("testDataPath")));
		
		//log("End DecisionTree.initializeAndPopulateInputHolders()");
	}

	private double getEntropy(int[][] data, ENTROPY_CONSTANTS entropyType) throws Exception {

		if(data.length == 0)
			return 0;
		
		int pos = 0, neg = 0;
		
		for(int i = 0; i < data.length; i++) {
			if(data[i][data[i].length-1] == 0) {
				neg++;
			} else {
				pos++;
			}
		}
		
		double entropy = 0;
		
		if(entropyType == ENTROPY_CONSTANTS.INFORMATION_GAIN) {
			entropy = (neg==0?0:-(((double)neg/data.length)*Math.log10((double)neg/data.length)/Math.log10(2))) + (pos==0?0:-(((double)pos/data.length)*Math.log10((double)pos/data.length)/Math.log10(2)));
		} else if (entropyType == ENTROPY_CONSTANTS.VARIANCE_IMPURITY){
			entropy = ((double)neg/data.length)*((double)pos/data.length);
		} else {
			throw new Exception("Unrecognized Input Entropy Type");
		}
		return entropy;
	}
	
	private double getEntropy(int negativeSamplesCnt, int positiveSamplesCnt, int totalSamplesCnt, ENTROPY_CONSTANTS entropyType) throws Exception {
		
		if(totalSamplesCnt == 0)
			return 0;
		
		double entropy = 0;
		
		if(entropyType == ENTROPY_CONSTANTS.INFORMATION_GAIN) {
			entropy = (negativeSamplesCnt==0?0:-(((double)negativeSamplesCnt/totalSamplesCnt)*Math.log10((double)negativeSamplesCnt/totalSamplesCnt)/Math.log10(2))) + (positiveSamplesCnt==0?0:-(((double)positiveSamplesCnt/totalSamplesCnt)*Math.log10((double)positiveSamplesCnt/totalSamplesCnt)/Math.log10(2)));
		} else if (entropyType == ENTROPY_CONSTANTS.VARIANCE_IMPURITY){
			entropy = ((double)negativeSamplesCnt/totalSamplesCnt)*((double)positiveSamplesCnt/totalSamplesCnt);
		} else {
			throw new Exception("Unrecognized Input Entropy Type");
		}
		
		return entropy;
	}
	
	public void createDecisionTree(int[][] data, Map<Integer,String> header, int nodePos, ENTROPY_CONSTANTS entropyType) throws Exception {

		//log("Begin DecisionTree.createDecisionTree()");
		if(nodePos == 1) {
			setDecisionTree(new HashMap<Integer,String>());
		}
		
		double rootEntropy = getEntropy(data, entropyType);
		//log("root entropy at nodePosition: "+nodePos+" is: "+rootEntropy);
		logArray(data);
		logMap(header);

		if(rootEntropy == 0) {
			getDecisionTree().put(nodePos, ""+data[0][data[0].length-1]);
		} else if(header.size() == 1) {
			int lneg = 0, lpos = 0;
			for(int i = 0; i < data.length; i++) {
				if(data[i][data[i].length-1] == 0) {
					lneg++;
				} else {
					lpos++;
				}
			}
			if(lneg > lpos) {
				getDecisionTree().put(nodePos,  "0");
			} else {
				getDecisionTree().put(nodePos, "1");
			}
		} else {
			double maxGain = -2;
			int maxGainAttribute = -1;
			int maxGainLeftNodeCnt = -1;
			int maxGainRightNodeCnt = -1;
			int maxGainMajorityClass = -1;
			
			for(int i = 0; i < data[0].length - 1; i++) {
				if(!header.containsKey(i))
					continue;
				int neg = 0, pos = 0, negNeg = 0, negPos = 0, posNeg = 0, posPos = 0;
				for(int j = 0; j < data.length; j++) {
					if(data[j][i] == 0) {
						neg++;
						if(data[j][data[j].length - 1] == 0) {
							negNeg++;
						} else {
							negPos++;
						}
					} else {
						pos++;
						if(data[j][data[j].length - 1] == 0) {
							posNeg++;
						} else {
							posPos++;
						}
					}
				}
				double negEntropy = 0, posEntropy = 0, entropy = 0, gain = 0;
				
				negEntropy = getEntropy(negNeg, negPos, neg, entropyType);
				posEntropy = getEntropy(posNeg, posPos, pos, entropyType);
				entropy = ((double)neg/(neg+pos))*negEntropy + ((double)pos/(neg+pos))*posEntropy;
				gain = rootEntropy - entropy;
				//log("entropy when split based on "+header.get(i)+" element: "+entropy);
				if (gain > maxGain) {
					maxGain = gain;
					maxGainAttribute = i;
					maxGainLeftNodeCnt = neg;
					maxGainRightNodeCnt = pos;
					maxGainMajorityClass = ((negNeg + posNeg) > (negPos + posPos)) ? 0 : 1;
				}
			}
			//log("maxGain: "+maxGain);
			//log("maxGainAttribute: "+header.get(maxGainAttribute));
			getDecisionTree().put(nodePos, header.get(maxGainAttribute));
			int[][] leftData = new int[maxGainLeftNodeCnt][data[0].length], rightData = new int[maxGainRightNodeCnt][data[0].length];
			Map<Integer,String> leftHeader = (Map<Integer, String>) ((HashMap<Integer, String>) header).clone();
			leftHeader.remove(maxGainAttribute);
			Map<Integer,String> rightHeader = (Map<Integer, String>) ((HashMap<Integer, String>) header).clone();
			rightHeader.remove(maxGainAttribute);
			int leftArrayCntr = 0, rightArrayCntr = 0;
			for(int i = 0; i < data.length; i++) {
				if(data[i][maxGainAttribute] == 0) {
					for(int j = 0; j < data[i].length; j++) {
						leftData[leftArrayCntr][j] = data[i][j];
					}
					leftArrayCntr++;
				} else {
					for(int j = 0; j < data[0].length; j++) {
						rightData[rightArrayCntr][j] = data[i][j];
					}
					rightArrayCntr++;
				}
			}
			if(maxGainLeftNodeCnt == 0) {
				getDecisionTree().put(nodePos*2, ""+maxGainMajorityClass);
			} else {
				createDecisionTree(leftData, leftHeader, nodePos*2, entropyType);
			}
			if(maxGainRightNodeCnt == 0) {
				getDecisionTree().put(nodePos*2 + 1, ""+maxGainMajorityClass);
			} else {
				createDecisionTree(rightData, rightHeader, nodePos*2 + 1, entropyType);
			}
		}
		//log("End DecisionTree.createDecisionTree()");
	}

	public Map<Integer,String> pruneDecisionTree(int l, int k) throws Exception {
		//log("Begin: DecisionTree.pruneDecisionTree");
		
		Map<Integer,String> decisionTreeBest = new HashMap<Integer,String>();
		decisionTreeBest.putAll(getDecisionTree());
		double bestTreeAccuracy = testDecisionTree(decisionTreeBest, getValidationData());
		//log("Default Tree Accuracy: "+bestTreeAccuracy);
		
		for(int i = 1; i <= l; i++) {
			Map<Integer,String> decisionTreeTemp = new HashMap<Integer,String>();
			decisionTreeTemp.putAll(getDecisionTree());
			
			List<Integer> nonLeafNodes = new ArrayList<Integer>();
			int nonLeafCnt = 0;
			Iterator<Integer> nodeItr = decisionTreeTemp.keySet().iterator();
			while(nodeItr.hasNext()) {
				Integer key = nodeItr.next();
				String val = decisionTreeTemp.get(key);
				if(!("0".equals(val) || "1".equals(val))) {
					nonLeafCnt++;
					nonLeafNodes.add(key);
				}
			}

			int m = 1 + (int)Math.round(Math.random()*(k-1));
			for(int j = 1; j <= m; j++) {
				int p = (int)Math.floor(Math.random()*nonLeafCnt);
				int subTreeChosen = nonLeafNodes.get(p);
				pruneNode(subTreeChosen, decisionTreeTemp);
				nonLeafNodes.remove(p);
				nonLeafCnt--;
			}
			
			double tempTreeAccuracy = testDecisionTree(decisionTreeTemp, getValidationData());
			if(tempTreeAccuracy > bestTreeAccuracy) {
				decisionTreeBest = decisionTreeTemp;
				bestTreeAccuracy = tempTreeAccuracy;
			}
		}
		
		//log("Best Tree Accuracy After Pruning: "+bestTreeAccuracy);
		//log("End: DecisionTree.pruneDecisionTree.");
		return decisionTreeBest;
	}
	
	private void pruneNode(int chosenNode, Map<Integer,String> tree) throws Exception {
		int i = chosenNode; 
		int isOdd = i%2;
		Map<Integer,Integer> rule = new HashMap<Integer,Integer>();
		while((i=i/2)>0) {
			rule.put(headerRowReverseMapping.get(tree.get(i)), isOdd);
			isOdd = i%2;
		}
		boolean isRecordMatching = true;
		int pCount = 0, nCount = 0;
		
		for(int j = 0; j < trainingData.length; j++) {
			isRecordMatching = true;
			for(int k = 0; k < trainingData[0].length-1 && rule.containsKey(k); k++) {
				if(trainingData[j][k] != rule.get(k)) {
					isRecordMatching = false;
					break;
				}
			}
			if(isRecordMatching) {
				if(trainingData[j][trainingData[0].length-1] == 0) {
					nCount++;
				} else {
					pCount++;
				}
			}
		}
		
		tree.put(chosenNode, ""+(nCount>pCount?0:1));
	}
	
	public double testDecisionTree(Map<Integer,String> decisionTree, int[][] testData) throws IOException {
		int correct = 0, wrong = 0;
		boolean isCorrect = false;
		
		for(int i = 0; i < testData.length; i++) {
			isCorrect = estimateAndCompareOutput(decisionTree, testData[i]);
			if(isCorrect)
				correct++;
			else
				wrong++;
		}
		
		//log("In testDecisionTree. Results: Correct="+correct+", Wrong="+wrong+".");
		return (double)correct/(correct+wrong);
	}
	
	private boolean estimateAndCompareOutput(Map<Integer,String> decisionTree, int[] testSample) throws IOException {
		int nodePos = 1;
		while(!decisionTree.get(nodePos).equals("0") && !decisionTree.get(nodePos).equals("1")) {
			nodePos = testSample[this.headerRowReverseMapping.get(decisionTree.get(nodePos))]==0 ? nodePos*2 : nodePos*2 + 1;
		}
		if(decisionTree.get(nodePos).equals(""+testSample[testSample.length-1]))
			return true;
		else {
			String str = "";
			for(int i = 0; i < testSample.length; i++) {
				str = str + testSample[i] + ",";
			}
			//log("Error in getting expected output sample: "+str);
			return false;
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		//input arguments
		int l = 0, k = 0;
		String trainingDataPath = "", validationDataPath = "", testDataPath = "";
		boolean print = false;
		
		try {
			l = Integer.parseInt(args[0]);
			k = Integer.parseInt(args[1]);
			trainingDataPath = args[2];
			validationDataPath = args[3];
			testDataPath = args[4];
			print = "true".equalsIgnoreCase(args[5]) || "yes".equalsIgnoreCase(args[5]) || "1".equalsIgnoreCase(args[5]);
		} catch(Exception e) {
			//throw new Exception("Error in processing input arguments to decision tree.", e);
			l = 100;
			k = 10;
			trainingDataPath = "training_set.csv";
			validationDataPath = "validation_set.csv";
			testDataPath = "test_set.csv";
			print = true;
		}

		DecisionTree tree = new DecisionTree();
		try {
			Map<String,String> dataSetsPath = new HashMap<String,String>();
			dataSetsPath.put("trainingDataPath", trainingDataPath);
			dataSetsPath.put("validationDataPath", validationDataPath);
			dataSetsPath.put("testDataPath", testDataPath);
			tree.initializeAndPopulateInputDataHolders(dataSetsPath);

			tree.createDecisionTree(tree.getTrainingData(), tree.getHeaderRow(), 1, ENTROPY_CONSTANTS.INFORMATION_GAIN);
			if(print) {
				tree.log("Decision Tree Before Pruning (Built Using Information Gain Heuristic):");
				tree.logDecisionTree(1, "", "");
			}
			double accuracyDTreeIGBeforePruning = tree.testDecisionTree(tree.getDecisionTree(), tree.getTestData());

			tree.setDecisionTree(tree.pruneDecisionTree(l, k));
			if(print) {
				tree.log("Decision Tree After Pruning (Built Using Information Gain Heuristic):");
				tree.logDecisionTree(1, "", "");
			}
			double accuracyDTreeIGAfterPruning = tree.testDecisionTree(tree.getDecisionTree(), tree.getTestData());
			

			tree.createDecisionTree(tree.getTrainingData(), tree.getHeaderRow(), 1, ENTROPY_CONSTANTS.VARIANCE_IMPURITY);
			if(print) {
				tree.log("Decision Tree Before Pruning (Built Using Variance Impurity Heuristic):");
				tree.logDecisionTree(1, "", "");
			}
			double accuracyDTreeVIBeforePruning = tree.testDecisionTree(tree.getDecisionTree(), tree.getTestData());

			tree.setDecisionTree(tree.pruneDecisionTree(l, k));
			if(print) {
				tree.log("Decision Tree After Pruning (Built Using Variance Impurity Heuristic):");
				tree.logDecisionTree(1, "", "");
			}
			double accuracyDTreeVIAfterPruning = tree.testDecisionTree(tree.getDecisionTree(), tree.getTestData());


			tree.log("Accuracy of decision tree(Built Using Information Gain Heuristic), before pruning: "+accuracyDTreeIGBeforePruning);
			tree.log("Accuracy decision tree(Built Using Information Gain Heuristic), after pruning: "+accuracyDTreeIGAfterPruning);
			
			tree.log("Accuracy of decision tree(Built Using Variance Impurity Heuristic), before pruning: "+accuracyDTreeVIBeforePruning);
			tree.log("Accuracy decision tree(Built Using Variance Impurity Heuristic), after pruning: "+accuracyDTreeVIAfterPruning);

		
		} catch(Exception e) {
			throw e;
		} finally {
			tree.closeLogger();
		}
		
	}

	private void logDecisionTree2(int nodePos, String pathTillParent) throws Exception {
		if(decisionTree.containsKey(nodePos)) {
			if(!decisionTree.containsKey(2*nodePos) && !decisionTree.containsKey(2*nodePos + 1)) {
				logWriter.write(pathTillParent+"::"+decisionTree.get(nodePos));
				logWriter.newLine();
			} else {
				logDecisionTree2(nodePos*2, pathTillParent+"||"+decisionTree.get(nodePos)+"=0");
				logDecisionTree2(nodePos*2 + 1, pathTillParent+"||"+decisionTree.get(nodePos)+"=1");
			}
		}
	}
	
	private void logDecisionTree(int nodePos, String indentation, String condition) throws IOException {
		if(getDecisionTree().containsKey(nodePos)) {
			if(!getDecisionTree().containsKey(2*nodePos) && !getDecisionTree().containsKey(2*nodePos + 1)) {
				System.out.println(indentation.substring(1)+condition+getDecisionTree().get(nodePos));
				logWriter.write(indentation.substring(1)+condition+getDecisionTree().get(nodePos));
				logWriter.newLine();
			} else {
				System.out.println((indentation.length()>0?indentation.substring(1):indentation)+condition);
				logWriter.write((indentation.length()>0?indentation.substring(1):indentation)+condition);
				logWriter.newLine();
				logDecisionTree(nodePos*2, indentation+"| ", getDecisionTree().get(nodePos)+" = 0 : ");
				logDecisionTree(nodePos*2 + 1, indentation+"| ", getDecisionTree().get(nodePos)+" = 1 : ");
			}
		}
	}

	private void initializeLogger() throws IOException {
		logWriter = new BufferedWriter(new FileWriter(LOG_FILE_NAME));
		logWriter.write("LOG STATEMENTS FOR DECISION TREE ALGORITHM");
		logWriter.newLine();
	}
	
	private void log(String str) throws IOException {
		System.out.println(str);
		logWriter.write(str);
		logWriter.newLine();
	}
	
	private void logArray(int[][] data) throws IOException {
		
		logWriter.write("dimensions of array: "+data.length+","+data[0].length+".");
		logWriter.newLine();

		for(int i = 0; i < data.length; i++) {
			for(int j = 0; j < data[0].length; j++) {
				logWriter.write(data[i][j]+" , ");
			}
			logWriter.newLine();
		}
		logWriter.flush();
		
	}
	
	private void logMap(Map<Integer,String> header) throws IOException {
		
		logWriter.write("size of map: "+header.size()+".");
		logWriter.newLine();

		Iterator<Integer> itr = header.keySet().iterator();
		while(itr.hasNext()) {
			Integer temp = itr.next();
			logWriter.write(temp.intValue()+": "+header.get(temp)+";");
		}
		logWriter.newLine();
		logWriter.flush();
	}
	
	private void closeLogger() throws IOException {
		logWriter.flush();
		logWriter.close();
	}
	
}
