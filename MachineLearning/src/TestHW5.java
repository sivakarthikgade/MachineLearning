
import java.io.BufferedWriter;
import java.io.FileWriter;

public class TestHW5 {

	public String logFile = "EM.log";
	public BufferedWriter bw = null;
	
	public TestHW5() throws Exception {
		bw = new BufferedWriter(new FileWriter(logFile));
	}
	
	public static void main(String args[]) throws Exception {
		TestHW5 t = new TestHW5();
		
		t.log("Values with unknown variance");
		for(int i = 0; i < 10; i++) {
			EM e = new EM();
			e.run(args[0], -1);
		}

		t.log("Values with known variance");
		for(int i = 0; i < 10; i++) {
			EM e = new EM();
			e.run(args[0], 1);
		}
		t.bw.flush();
		t.bw.close();
	}
	
	public void log(String msg) throws Exception {
		bw.write(msg);
		bw.newLine();
	}

}
