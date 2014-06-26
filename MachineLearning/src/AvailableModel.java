public class AvailableModel {

	String name;
	String path;
	int weightage;
	
	public AvailableModel(String name, String path, int weightage) {
		this.name = name;
		this.path = path;
		this.weightage = weightage;
	}
	
	public static final AvailableModel NaiveBayes = new AvailableModel("NaiveBayes", "train_NB500.model", 2);
	public static final AvailableModel J48 = new AvailableModel("J48", "train_DT500.model", 1);
	public static final AvailableModel SMO1 = new AvailableModel("SMO1", "train_SMO1500.model", 2);
	public static final AvailableModel SMO2 = new AvailableModel("SMO2", "train_SMO2500.model", 1);
	public static final AvailableModel KNN = new AvailableModel("KNN", "train_1NN500.model", 1);

}
