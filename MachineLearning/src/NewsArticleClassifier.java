
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsArticleClassifier {

	private static final AvailableModel[] Models = {AvailableModel.NaiveBayes, AvailableModel.J48, AvailableModel.SMO1, AvailableModel.SMO2, AvailableModel.KNN};
	
	public NewsArticleClassifier() {
		// TODO Auto-generated constructor stub
	}
	
	public void classify(String inputFilePath) throws Exception {
		String prediction;
		Map<String, List<AvailableModel>> predictionList = new HashMap<String, List<AvailableModel>>();
		for(int i = 0; i < Models.length; i++) {
			MyFilteredClassifier classifier = new MyFilteredClassifier();
			classifier.load(inputFilePath);
			classifier.loadModel(Models[i].path);
			classifier.makeInstance();
			prediction = classifier.classify();
			if(!predictionList.keySet().contains(prediction)) {
				predictionList.put(prediction, new ArrayList<AvailableModel>());
			}
			predictionList.get(prediction).add(Models[i]);
		}
		
		//Output the majority votes class as the prediction.
		String selectedPrediction = null;
		int selectedPredictionVotes = 0, tiePredVotes = 0;
		boolean tie = false;
		for(String prd: predictionList.keySet()) {
			if(selectedPredictionVotes < getWeightedVote(predictionList.get(prd), false)) {
				selectedPrediction = prd;
				selectedPredictionVotes = getWeightedVote(predictionList.get(prd), false);
			} else if(selectedPredictionVotes == getWeightedVote(predictionList.get(prd), false)) {
				tie = true;
				tiePredVotes = selectedPredictionVotes;
			}
		}
		
		//Output the majority weighted votes class as the prediction.
		if(tie && (tiePredVotes == selectedPredictionVotes)) {
			selectedPrediction = null;
			selectedPredictionVotes = 0; tiePredVotes = 0;
			tie = false;
			for(String prd: predictionList.keySet()) {
				if(selectedPredictionVotes < getWeightedVote(predictionList.get(prd), true)) {
					selectedPrediction = prd;
					selectedPredictionVotes = getWeightedVote(predictionList.get(prd), true);
				} else if(selectedPredictionVotes == getWeightedVote(predictionList.get(prd), true)) {
					tie = true;
					tiePredVotes = selectedPredictionVotes;
				}
			}
		} else {
			log("Class: "+selectedPrediction);
		}
		
		//Output SMO1 prediction.
		if(tie && (tiePredVotes == selectedPredictionVotes)) {
			for(String prd: predictionList.keySet()) {
				if(predictionList.get(prd).contains(AvailableModel.SMO1)) {
					selectedPrediction = prd;
					log("Class: "+selectedPrediction);
					return;
				}
			}
			
		} else {
			log("Class: "+selectedPrediction);
		}
	}
	
	private int getWeightedVote(List<AvailableModel> models, boolean weighted) {
		if(!weighted) {
			return models.size();
		} else {
			int sum = 0;
			for(AvailableModel model: models) {
				sum = sum + model.weightage;
			}
			return sum;
		}
	}
	
	public static void main(String args[]) throws Exception {
		NewsArticleClassifier c = new NewsArticleClassifier();
		String testFile = args[0];
		c.classify(testFile);
	}
	
	private void log(String message) {
		System.out.println(message);
	}

}

// Accepts a news article as input. Uses the 5/6 models it has -> Classifies the article using each of them -> Employs a voting mechanism and outputs a prediction.
// Classifying - Can use the existing validation code.
// Prediction - Majority. If tie then weighted Majority. Weights - NB-2, DT-1, SVM1-2, SVM2-1, 1NN-1