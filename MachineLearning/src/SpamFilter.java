/**
 * @author Siva Karthik Gade
 *
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

enum Class {
	SPAM,
	HAM
}

enum Classifier {
	NAIVE_BAYES,
	LOGISTIC_REGRESSION
}

public class SpamFilter {
	
	private static final String LOG_FILE = "SpamFilter_Log.txt";
	private static final String STOP_WORDS_FILE = "Stop_Words.txt";
	private BufferedWriter bw = null;
	private String trainingFolder = null;
	private String testFolder = null;
	private Classifier classifier = null;
	private Set<String> vocabulary = new HashSet<String>();
	private List<String> vocab = new ArrayList<String>();
	private Set<String> stopWords = new HashSet<String>();
	private Map<Class, Integer> docCntPerClass = new HashMap<Class, Integer>();
	private Map<Class, Double> prior = new HashMap<Class, Double>();
	private Map<Class, Map<String, Integer>> tokenCounts = new HashMap<Class, Map<String, Integer>>();
	private Map<Class, Map<String, Double>> tokenCondProb = new HashMap<Class, Map<String, Double>>();
	private Map<Class, List<String>> classTokens = new HashMap<Class, List<String>>();
	private boolean ignoreStopWords = false;
	private String regex = "[^a-z\'\\:\\/\\@\\+]";
	private Map<Integer,Map<Integer,Integer>> data = null;
	private double eeta = 0.1;
	private double lameda = 0.001;
	private double maxConvergenceLimit = 100;
	private double[] w = null;
	
	public SpamFilter() throws Exception {
		bw = new BufferedWriter(new FileWriter(LOG_FILE));
		//log("In the constructor of SpamFilter");
	}
	
	public SpamFilter(String trainingFolder, String testFolder, int maxConvergenceLimit, double eeta, double lameda) throws Exception {
		this.trainingFolder = trainingFolder;
		this.testFolder = testFolder;
		this.maxConvergenceLimit = maxConvergenceLimit;
		this.eeta = eeta;
		this.lameda = lameda;
	}
	
	public double getEeta() {
		return eeta;
	}

	public void setEeta(double eeta) {
		this.eeta = eeta;
	}

	public boolean isIgnoreStopWords() {
		return this.ignoreStopWords;
	}

	public void setIgnoreStopWords(boolean ignoreStopWords) {
		this.ignoreStopWords = ignoreStopWords;
	}

	public double getMaxConvergenceLimit() {
		return maxConvergenceLimit;
	}

	public void setMaxConvergenceLimit(double maxConvergenceLimit) {
		this.maxConvergenceLimit = maxConvergenceLimit;
	}

	public double getLameda() {
		return lameda;
	}

	public void setLameda(double lameda) {
		this.lameda = lameda;
	}

	public Map<Integer, Map<Integer, Integer>> getData() {
		return data;
	}

	public void setData(Map<Integer, Map<Integer,Integer>> data) {
		this.data = data;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public Set<String> getStopWords() {
		return stopWords;
	}

	public void setStopWords(Set<String> stopWords) {
		this.stopWords = stopWords;
	}

	public Map<Class, List<String>> getClassTokens() {
		return classTokens;
	}

	public void setClassTokens(Map<Class, List<String>> classTokens) {
		this.classTokens = classTokens;
	}

	public Map<Class, Double> getPrior() {
		return prior;
	}

	public void setPrior(Map<Class, Double> prior) {
		this.prior = prior;
	}

	public Map<Class, Integer> getDocCntPerClass() {
		return docCntPerClass;
	}

	public void setDocCntPerClass(Map<Class, Integer> docCntPerClass) {
		this.docCntPerClass = docCntPerClass;
	}

	public Set<String> getVocabulary() {
		return vocabulary;
	}

	public void setVocabulary(Set<String> vocabulary) {
		this.vocabulary = vocabulary;
	}

	public Map<Class, Map<String, Double>> getTokenCondProb() {
		return tokenCondProb;
	}

	public void setTokenCondProb(Map<Class, Map<String, Double>> tokenCondProb) {
		this.tokenCondProb = tokenCondProb;
	}

	public Map<Class, Map<String, Integer>> getTokenCounts() {
		return tokenCounts;
	}

	public void setTokenCounts(Map<Class, Map<String, Integer>> tokenCounts) {
		this.tokenCounts = tokenCounts;
	}

	public Classifier getClassifier() {
		return classifier;
	}

	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}

	public String getTrainingFolder() {
		return trainingFolder;
	}

	public void setTrainingFolder(String trainingFolder) {
		this.trainingFolder = trainingFolder;
	}

	public String getTestFolder() {
		return testFolder;
	}

	public void setTestFolder(String testFolder) {
		this.testFolder = testFolder;
	}
	
	public void loadStopWords() throws Exception {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(SpamFilter.STOP_WORDS_FILE));
		} catch(FileNotFoundException e) {
			throw new Exception("Please copy Stop_Words.txt file into the same folder as SpamFilter.java");
		}
		String str = null;
		while((str=br.readLine())!=null) {
			getStopWords().add(str.toLowerCase());
		}
		br.close();
		//log("StopWord count: "+getStopWords().size());
	}
	
	public void trainMultiNomial() throws Exception {
		if(Classifier.NAIVE_BAYES == getClassifier()) {
			File folder = null;
			
			folder = new File(getTrainingFolder()+File.separator+Class.SPAM);
			getDocCntPerClass().put(Class.SPAM, folder.listFiles().length);
			
			folder = new File(getTrainingFolder()+File.separator+Class.HAM);
			getDocCntPerClass().put(Class.HAM, folder.listFiles().length);

			getPrior().put(Class.SPAM, (double)getDocCntPerClass().get(Class.SPAM)/(getDocCntPerClass().get(Class.SPAM) + getDocCntPerClass().get(Class.HAM)));
			getPrior().put(Class.HAM, (double)getDocCntPerClass().get(Class.HAM)/(getDocCntPerClass().get(Class.SPAM) + getDocCntPerClass().get(Class.HAM)));

			trainMultiNomialNB(null);
		} else if(Classifier.LOGISTIC_REGRESSION == getClassifier()) {
			trainMultiNomialLR();
		}
	}
	
	private void trainMultiNomialNB(Class currClass) throws Exception {
		
		if(currClass == null) {

			for(Class c: Class.values()) {
				getTokenCounts().put(c, new HashMap<String, Integer>());
				getTokenCondProb().put(c, new HashMap<String, Double>());
			}
			
			for(Class c: Class.values()) {
				trainMultiNomialNB(c);
			}
			
			for(Class c: Class.values()) {
				int totalTokenCnt = getClassTokens().get(c).size() + getVocabulary().size();
				for(String word: getVocabulary()) {
					getTokenCondProb().get(c).put(word, (double)getTokenCounts().get(c).get(word)/totalTokenCnt);
				}
			}
			
			return;
		}
		
		String filePath = null;
		File folder = null;
		File[] listOfFiles = null;
		String fileText = null, str = null, token = null;
		BufferedReader br = null;
		Collection<String> tokens = null;
		Iterator<String> itr = null;
		
		folder = new File(getTrainingFolder()+File.separator+currClass.name());
		listOfFiles = folder.listFiles();
		getClassTokens().put(currClass, new ArrayList<String>());
		
		for(int i = 0; i < listOfFiles.length; i++) {
			if(listOfFiles[i].isFile()) {
				fileText = "";
				filePath = listOfFiles[i].getAbsolutePath();
				br = new BufferedReader(new FileReader(filePath));
				while((str = br.readLine()) != null) {
					fileText = fileText + " " + str;
				}
				br.close();
				fileText = fileText.toLowerCase();
				//fileText = fileText.replaceAll(getRegex(), " ");
				tokens = new ArrayList<String>(Arrays.asList(fileText.toLowerCase().split("[\\s]+")));

				itr = tokens.iterator();
				while(itr.hasNext()) {
					token = itr.next();
					if(isIgnoreStopWords() && getStopWords().contains(token)) {
						itr.remove();
						continue;
					}
					if(getTokenCounts().get(currClass).get(token) == null)
						getTokenCounts().get(currClass).put(token,2);
					else
						getTokenCounts().get(currClass).put(token,getTokenCounts().get(currClass).get(token)+1);
					for(Class c: Class.values()) {
						if(c == currClass)
							continue;
						if(getTokenCounts().get(c).get(token) == null)
							getTokenCounts().get(c).put(token,1);
					}
				}
				
				getClassTokens().get(currClass).addAll(tokens);
				getVocabulary().addAll(tokens);
				vocab = new ArrayList<String>(getVocabulary());
			}
		}
	}

	private void trainMultiNomialLR() throws Exception {
		int docCntPerCls = 0;
		
		//Load and doc counts per class vocabulary
		for(Class c: Class.values()) {

			String filePath = null;
			File folder = null;
			File[] listOfFiles = null;
			String fileText = null, str = null, token = null;
			BufferedReader br = null;
			Collection<String> tokens = null;
			Iterator<String> itr = null;
			
			folder = new File(getTrainingFolder()+File.separator+c.name());
			listOfFiles = folder.listFiles();
			
			for(int i = 0; i < listOfFiles.length; i++) {
				if(listOfFiles[i].isFile()) {
					docCntPerCls++;
					fileText = "";
					filePath = listOfFiles[i].getAbsolutePath();
					br = new BufferedReader(new FileReader(filePath));
					while((str = br.readLine()) != null) {
						fileText = fileText + " " + str;
					}
					br.close();
					fileText = fileText.toLowerCase();
					//fileText = fileText.replaceAll(getRegex(), " ");
					tokens = new ArrayList<String>(Arrays.asList(fileText.toLowerCase().split("[\\s]+")));

					if(isIgnoreStopWords()) {
						itr = tokens.iterator();
						while(itr.hasNext()) {
							token = itr.next();
							if(getStopWords().contains(token)) {
								itr.remove();
							}
						}
					}
					getVocabulary().addAll(tokens);
					vocab = new ArrayList<String>(getVocabulary());
				}
			}
			getDocCntPerClass().put(c, docCntPerCls);
			docCntPerCls = 0;
		}
		//log("LR vocab size:"+vocab.size());
		//Populate data array
		int totalDocumentCnt = 0;
		for(Class c: Class.values()) {
			totalDocumentCnt += getDocCntPerClass().get(c);
		}
		//log("LR: Dictionary population completed. size:"+vocab.size());
		//log("LR: Total docs in training:"+totalDocumentCnt);

		data = new HashMap<Integer, Map<Integer, Integer>>();//new int[totalDocumentCnt][vocab.size()+2];

		for(int i = 0; i < totalDocumentCnt; i++) {
			data.put(i, new HashMap<Integer, Integer>());
		}
		
		int docCntr = 0;
		
		for(Class c: Class.values()) {

			File folder = new File(getTrainingFolder()+File.separator+c.name());
			File[] listOfFiles = folder.listFiles();
			
			for(int i = 0; i < listOfFiles.length; i++) {
				if(listOfFiles[i].isFile()) {
					data.get(docCntr).put(0, 1);
					BufferedReader br = new BufferedReader(new FileReader(listOfFiles[i].getAbsolutePath()));
					String str = null;
					String fileText = "", token = "";
					Set<String> distinctTokens = new HashSet<String>();
					List<String> tokens = new ArrayList<String>();
					Iterator<String> itr = null;
					while((str=br.readLine())!=null) {
						fileText = fileText + " " + str;
					}
					br.close();
					fileText = fileText.toLowerCase();
					//fileText = fileText.replaceAll(getRegex(), " ");
					tokens = new ArrayList<String>(Arrays.asList(fileText.toLowerCase().split("[\\s]+")));
					if(isIgnoreStopWords()) {
						itr = tokens.iterator();
						while(itr.hasNext()) {
							token = itr.next();
							if(getStopWords().contains(token)) {
								itr.remove();
							}
						}
					}
					distinctTokens.addAll(tokens);
					itr = distinctTokens.iterator();
					while(itr.hasNext()) {
						token = itr.next();
						int pos = vocab.indexOf(token);
						//int cnt = Collections.frequency(tokens, token);
						//data.get(docCntr).put(pos+1, cnt);
						data.get(docCntr).put(pos+1, 1);
					}
					data.get(docCntr).put(1+vocab.size(), (c == Class.SPAM ? 1 : 0));
					docCntr++;
				}
			}
		}
		if(docCntr != totalDocumentCnt) {
			throw new Exception("All samples are not processed. cnt1:"+totalDocumentCnt+", cnt2:"+docCntr+".");
		}
		
		//Initialize pr, w and dw arrays
		double[] pr, dw;
		pr = new double[totalDocumentCnt];
		for(int i = 0; i < totalDocumentCnt; i++) {
			pr[i] = 0;
		}
		
		w = new double[vocab.size()+1];
		for(int i = 0; i < vocab.size()+1; i++) {
			w[i] = (double)1/vocab.size();
		}
		
		for(int i = 0; i < getMaxConvergenceLimit(); i++) {
			for(int j = 0; j < totalDocumentCnt; j++) {
				double l = 0;
				for(int k: data.get(j).keySet()) {
					if(k == vocab.size()+1) {
						continue;
					}
					l = l + w[k]*data.get(j).get(k);
				}
				l = 1*l;
				l = 1 + Math.exp(l);
				l = (double)1/(double)l;
				pr[j] = l;
			}
			
			dw = new double[vocab.size()+1];
			for(int j = 0; j < vocab.size()+1; j++) {
				dw[j] = 0;
			}
			
			for(int k = 0; k < totalDocumentCnt; k++) {
				for(int j: data.get(k).keySet()) {
					if(j == vocab.size()+1) {
						continue;
					}
					dw[j] = dw[j] + data.get(k).get(j)*(data.get(k).get(vocab.size()+1)-pr[k]);
				}
			}
			
			for(int j = 0; j < vocab.size()+1; j++) {
				w[j] = w[j] + getEeta()*(dw[j] - getLameda()*w[j]);
			}
			
		}
	}

	
	public void testMultiNomial() throws Exception {
		if(Classifier.NAIVE_BAYES == getClassifier()) {
			int correctCnt = testMultiNomialNB(null);
			int totalTestDocCnt = 0;
			for(Class c: Class.values())
				totalTestDocCnt+=(new File(getTestFolder()+File.separator+c)).listFiles().length;
			//log("Total correctly classified documents: "+correctCnt+"/"+totalTestDocCnt+". Accuracy: "+((double)correctCnt/totalTestDocCnt));
			//log("Size of vocab: "+vocab.size());
			log("Accuracy of naive bayes classifier ("+(isIgnoreStopWords()?"excluding":"including")+" stopwords) is: "+((double)correctCnt*100/totalTestDocCnt));
		} else if(Classifier.LOGISTIC_REGRESSION == getClassifier())
			testMultiNomialLR();
	}
	
	private int testMultiNomialNB(String folderPath) throws Exception {
		int correctCnt = 0, totalCnt = 0;
		Class expectedClass = null;
		
		if(folderPath == null || folderPath.length() == 0) {
			for(Class c: Class.values())
				correctCnt+=testMultiNomialNB(getTestFolder()+File.separator+c);
			return correctCnt;
		}
		
		if(folderPath.substring(folderPath.lastIndexOf(File.separator)+1).equalsIgnoreCase(Class.SPAM.name()))
			expectedClass = Class.SPAM;
		else if(folderPath.substring(folderPath.lastIndexOf(File.separator)+1).equalsIgnoreCase(Class.HAM.name()))
			expectedClass = Class.HAM;
		
		File folder = new File(folderPath);
		File[] listOfFiles = folder.listFiles();
		String filePath = null;
		String fileText = null, str = null;
		BufferedReader br = null;
		List<String> tokensInDoc = null;
		double probability = 0, maxProbability = 0;
		Class actualClass = null;
		List<String> tokens = null;
		Iterator<String> itr = null;
	
		for(int i = 0; i < listOfFiles.length; i++) {
			if(listOfFiles[i].isFile()) {
				fileText = "";
				filePath = listOfFiles[i].getAbsolutePath();
				br = new BufferedReader(new FileReader(filePath));
				while((str = br.readLine()) != null) {
					fileText = fileText + " " + str;
				}
				br.close();
				tokensInDoc = new ArrayList<String>();
				fileText = fileText.toLowerCase();
				//fileText = fileText.replaceAll(getRegex(), " ");
				tokens = new ArrayList<String>(Arrays.asList(fileText.toLowerCase().split("[\\s]+")));
				if(isIgnoreStopWords()) {
					itr = tokens.iterator();
					while(itr.hasNext()) {
						str = itr.next();
						if(getStopWords().contains(str)) {
							itr.remove();
							continue;
						}
					}
				}
				tokensInDoc.addAll(tokens);
				
				actualClass = null;
				maxProbability = 0;
				for(Class c: Class.values()) {
					probability = Math.log10(getPrior().get(c))/Math.log10(2);
					for(String token: tokensInDoc) {
						probability = probability + (getTokenCondProb().get(c).get(token)==null ? 0 : Math.log10(getTokenCondProb().get(c).get(token))/Math.log10(2));
					}
					if(actualClass == null || (probability > maxProbability)) {
						maxProbability = probability;
						actualClass = c;
					}
				}
				
				if(actualClass == expectedClass)
					correctCnt++;
				totalCnt++;
			}
		}

		//log("correct count in class "+ expectedClass.name() + ": "+correctCnt+"/"+totalCnt+". Accuracy: "+((double)correctCnt/totalCnt));
		return correctCnt;
	}

	private void testMultiNomialLR() throws Exception {
		int correctCntSpam = 0, correctCntHam = 0;
		int totalCnt = 0;
		int[] testRecData = null;

		for(Class c: Class.values()) {
			File folder = new File(getTestFolder()+File.separator+c.name());
			File[] listOfFiles = folder.listFiles();
			
			for(int i = 0; i < listOfFiles.length; i++) {
				if(listOfFiles[i].isFile()) {
					testRecData = new int[vocab.size()+1];
					for(int j = 0; j < testRecData.length; j++) {
						testRecData[j] = 0;
					}
					testRecData[0] = 1;
					BufferedReader br = new BufferedReader(new FileReader(listOfFiles[i].getAbsolutePath()));
					String str = null;
					String fileText = "", token = "";
					Set<String> distinctTokens = new HashSet<String>();
					List<String> tokens = new ArrayList<String>();
					Iterator<String> itr = null;
					while((str=br.readLine())!=null) {
						fileText = fileText + " " + str;
					}
					br.close();
					fileText = fileText.toLowerCase();
					//fileText = fileText.replaceAll(getRegex(), " ");
					tokens = new ArrayList<String>(Arrays.asList(fileText.toLowerCase().split("[\\s]+")));
					if(isIgnoreStopWords()) {
						itr = tokens.iterator();
						while(itr.hasNext()) {
							token = itr.next();
							if(getStopWords().contains(token)) {
								itr.remove();
							}
						}
					}
					distinctTokens.addAll(tokens);
					itr = distinctTokens.iterator();
					while(itr.hasNext()) {
						token = itr.next();
						int pos = vocab.indexOf(token);
						//int cnt = Collections.frequency(tokens, token);
						//testRecData[pos+1] = cnt;
						testRecData[pos+1] = 1;
					}
					
					//evaluate
					double l = 0;
					for(int j = 0; j < vocab.size()+1; j++) {
						l = l + w[j]*testRecData[j];
					}
					//log("Test sample: "+totalCnt+"; LR Score="+l);
					if((l > 0) && (c == Class.HAM)) {
						correctCntSpam++;
					}
					if((l <= 0) && (c == Class.SPAM)) {
						correctCntHam++;
					}
					totalCnt++;
				}
			}
		}
		//log("LR Stats: Correct count: ("+correctCntSpam+","+correctCntHam+"). Total count: "+(correctCntSpam+correctCntHam)+"/"+totalCnt+". Accuracy: "+((double)(correctCntHam+correctCntSpam)/totalCnt));
		log("Accuracy of logistic regression classifier ("+(isIgnoreStopWords()?"excluding":"including")+" stopwords) is: "+((double)(correctCntSpam+correctCntHam)*100/totalCnt));
	}

	
	public void applyMultiNomial() throws Exception {
		if(Classifier.NAIVE_BAYES == getClassifier())
			applyMultiNomialNB();
		else if(Classifier.LOGISTIC_REGRESSION == getClassifier())
			applyMultiNomialLR();
	}
	
	private void applyMultiNomialNB() throws Exception {
		
	}

	private void applyMultiNomialLR() throws Exception {
		
	}


	public static void main(String[] args) throws Exception {

		double startTime = System.currentTimeMillis();
		String trainingFolder = args[0];
		String testFolder = args[1];
		int convergenceHardLimit = Integer.parseInt(args[2]);
		double eeta = Double.parseDouble(args[3]);
		double lameda = Double.parseDouble(args[4]);

		SpamFilter filter = null;
		
		try {
		filter = new SpamFilter(trainingFolder, testFolder, convergenceHardLimit, eeta, lameda);
		filter.initializeLogger(false);
		filter.loadStopWords();
		filter.setClassifier(Classifier.NAIVE_BAYES);
		filter.setIgnoreStopWords(false);
		filter.trainMultiNomial();
		filter.testMultiNomial();
		} catch(Exception e) {
			throw e;
		} finally {
			filter.flushLogger();
			filter.closeLogger();
		}

		try {
		filter = new SpamFilter(trainingFolder, testFolder, convergenceHardLimit, eeta, lameda);
		filter.initializeLogger(true);
		filter.loadStopWords();
		filter.setClassifier(Classifier.NAIVE_BAYES);
		filter.setIgnoreStopWords(true);
		filter.trainMultiNomial();
		filter.testMultiNomial();
		} catch(Exception e) {
			throw e;
		} finally {
			filter.flushLogger();
			filter.closeLogger();
		}
		
		try {
		filter = new SpamFilter(trainingFolder, testFolder, convergenceHardLimit, eeta, lameda);
		filter.initializeLogger(true);
		filter.loadStopWords();
		filter.setClassifier(Classifier.LOGISTIC_REGRESSION);
		filter.setIgnoreStopWords(false);
		filter.trainMultiNomial();
		filter.testMultiNomial();
		} catch(Exception e) {
			throw e;
		} finally {
			filter.flushLogger();
			filter.closeLogger();
		}

		try {
		filter = new SpamFilter(trainingFolder, testFolder, convergenceHardLimit, eeta, lameda);
		filter.initializeLogger(true);
		filter.loadStopWords();
		filter.setClassifier(Classifier.LOGISTIC_REGRESSION);
		filter.setIgnoreStopWords(true);
		filter.trainMultiNomial();
		filter.testMultiNomial();

		filter.log("SpamFilter: Total execution time->"+((double)(System.currentTimeMillis()-startTime)/1000)+" secs.");
		} catch(Exception e) {
			throw e;
		} finally {
			filter.flushLogger();
			filter.closeLogger();
		}

	}
	
	private void log(String str) throws Exception {
		System.out.println(str);
		bw.write(str);
		bw.newLine();
	}
	
	private void initializeLogger(boolean append) throws Exception {
		bw = new BufferedWriter(new FileWriter(SpamFilter.LOG_FILE, append));
	}
	
	private void flushLogger() throws Exception {
		bw.flush();
	}
	
	private void closeLogger() throws Exception {
		bw.flush();
		bw.close();
	}

}
