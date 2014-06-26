
import java.io.File;

public class TestValidationSet
{
	public static void main(String args[]) {
		int cCnt, fCnt, eCnt;
		String actClass, predClass;
		String folderPath = args[0];
		File dir = new File(folderPath);
		File[] subDirs = dir.listFiles();
		for(File subDir: subDirs) {
			actClass = subDir.getName();
			cCnt = 0; fCnt = 0; eCnt = 0;
			File[] files = subDir.listFiles();
			for(File file: files) {
				MyFilteredClassifier classifier = new MyFilteredClassifier();
				classifier.load(file.getAbsolutePath());
				classifier.loadModel(args[1]);
				classifier.makeInstance();
				predClass = classifier.classify();
				if(predClass.equals("")) { eCnt++;}
				if(predClass.equals(actClass)) {
					cCnt++;
				} else {
					fCnt++;
				}
			}
			System.out.println("Class "+actClass+" results-> Correctly Classified: "+cCnt+"; Incorrectly Classified: "+fCnt+"; Exception: "+eCnt);
		}
	}
}