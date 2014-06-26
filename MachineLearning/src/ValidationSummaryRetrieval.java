
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationSummaryRetrieval
{
	public static List<String> classes = new ArrayList<String>();
	
	public static void main(String args[]) throws Exception {
		classes.add("alt.atheism");
		classes.add("comp.graphics");
		classes.add("comp.os.ms-windows.misc");
		classes.add("comp.sys.ibm.pc.hardware");
		classes.add("comp.sys.mac.hardware");
		classes.add("comp.windows.x");
		classes.add("misc.forsale");
		classes.add("rec.autos");
		classes.add("rec.motorcycles");
		classes.add("rec.sport.baseball");
		classes.add("rec.sport.hockey");
		classes.add("sci.crypt");
		classes.add("sci.electronics");
		classes.add("sci.med");
		classes.add("sci.space");
		classes.add("soc.religion.christian");
		classes.add("talk.politics.guns");
		classes.add("talk.politics.mideast");
		classes.add("talk.politics.misc");
		classes.add("talk.religion.misc");

		BufferedReader br = new BufferedReader(new FileReader(args[0]));
		BufferedWriter bw = new BufferedWriter(new FileWriter(args[1]));
		
		String str = null;
		int[][] predictionMatrix = new int[20][20];
		for(int i = 0; i < 20; i++) {
			for(int j = 0; j < 20; j++) {
				predictionMatrix[i][j] = 0;
			}
		}
		
		int cnt, x, cCnt;
		String cClass;
		Map<String, Integer> classCnt;
		
		for(int i = 1; i <= 20; i++) {
			cnt = 0;
			classCnt = new HashMap<String, Integer>();
			while(cnt < 100) {
				str = br.readLine();
				cnt++;
				String[] tokens = str.split(":");
				if(classCnt.containsKey(tokens[1].trim())) {
					classCnt.put(tokens[1].trim(), classCnt.get(tokens[1].trim())+1);
				} else {
					classCnt.put(tokens[1].trim(), 1);
				}
			}
			str = br.readLine();
			str = str.substring(str.indexOf(" ")+1);
			cClass = str.substring(0, str.indexOf(" "));
			x = classes.indexOf(cClass);
			cCnt = Integer.parseInt(str.substring(str.indexOf(": ")+2, str.indexOf(";")));
			for(String cls: classCnt.keySet()) {
				predictionMatrix[x][classes.indexOf(cls)] = classCnt.get(cls);
				if(cClass.equals(cls)) {
					if(classCnt.get(cls) != cCnt) {
						throw new Exception("Correct Counts not matching: "+classCnt.get(cls)+","+cCnt);
					}
				}
			}
		}
		
		int correctlyClassifiedCnt = 0, inCorrectlyClassifiedCnt = 0;
		String confusionMatrix = "";
		for(int i = 0; i < 20; i++) {
			for(int j = 0; j < 20; j++) {
				confusionMatrix = confusionMatrix + predictionMatrix[i][j] + "\t";
				if(i == j) {
					correctlyClassifiedCnt+=predictionMatrix[i][j];
				} else {
					inCorrectlyClassifiedCnt+=predictionMatrix[i][j];
				}
			}
			confusionMatrix = confusionMatrix + "\n";
		}
		
		bw.write("Correctly Classified Instances: " +((double)correctlyClassifiedCnt/(double)2000)+ "%");
		bw.newLine();
		bw.write("Incorrectly Classified Instances: " +((double)inCorrectlyClassifiedCnt/(double)2000)+ "%");
		bw.newLine();
		bw.write(confusionMatrix);
		bw.newLine();
		
		br.close();
		bw.flush();
		bw.close();
	}
	
	public static void log(String message) {
		System.out.println(message);
	}
}