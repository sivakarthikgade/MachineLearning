
/**
 * A Java class that implements a simple text classifier, based on WEKA.
 * To be used with MyFilteredLearner.java.
 * WEKA is available at: http://www.cs.waikato.ac.nz/ml/weka/
 * Copyright (C) 2013 Jose Maria Gomez Hidalgo - http://www.esp.uem.es/jmgomez
 *
 * This program is free software: you can redistribute it and/or modify
 * it for any purpose.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
 
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;

import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * This class implements a simple text classifier in Java using WEKA.
 * It loads a file with the text to classify, and the model that has been
 * learnt with MyFilteredLearner.java.
 * @author Jose Maria Gomez Hidalgo - http://www.esp.uem.es/jmgomez
 * @see MyFilteredLearner
 */
 public class MyFilteredClassifier {

    /**
     * String that stores the text to classify
     */
    String text;
    /**
     * Object that stores the instance.
     */
    Instances instances;
    /**
     * Object that stores the classifier.
     */
    FilteredClassifier classifier;
                
    /**
     * This method loads the text to be classified.
     * @param fileName The name of the file that stores the text.
     */
    public void load(String fileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            text = "";
            while ((line = reader.readLine()) != null) {
            	text = text + " " + line;
            }
            //System.out.println("===== Loaded text data: " + fileName + " =====");
            reader.close();
            //System.out.println(text);
        } catch (IOException e) {
           System.out.println("Problem found when reading: " + fileName);
        }
    }
                        
   /**
    * This method loads the model to be used as classifier.
    * @param fileName The name of the file that stores the text.
    */
	public void loadModel(String fileName) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
            Object tmp = in.readObject();
            classifier = (FilteredClassifier) tmp;
            in.close();
             //System.out.println("===== Loaded model: " + fileName + " =====");
        } catch (Exception e) {
            // Given the cast, a ClassNotFoundException must be caught along with the IOException
            System.out.println("Problem found when reading: " + fileName);
        }
    }
        
    /**
     * This method creates the instance to be classified, from the text that has been read.
     */
    public void makeInstance() {
        // Create the attributes, class and text
        Attribute attribute1 = new Attribute("text",(FastVector) null);
        FastVector fvNominalVal = new FastVector(20);
		fvNominalVal.addElement("alt.atheism");
        fvNominalVal.addElement("comp.graphics");
        fvNominalVal.addElement("comp.os.ms-windows.misc");
        fvNominalVal.addElement("comp.sys.ibm.pc.hardware");
        fvNominalVal.addElement("comp.sys.mac.hardware");
        fvNominalVal.addElement("comp.windows.x");
        fvNominalVal.addElement("misc.forsale");
        fvNominalVal.addElement("rec.autos");
        fvNominalVal.addElement("rec.motorcycles");
        fvNominalVal.addElement("rec.sport.baseball");
        fvNominalVal.addElement("rec.sport.hockey");
        fvNominalVal.addElement("sci.crypt");
        fvNominalVal.addElement("sci.electronics");
        fvNominalVal.addElement("sci.med");
        fvNominalVal.addElement("sci.space");
        fvNominalVal.addElement("soc.religion.christian");
        fvNominalVal.addElement("talk.politics.guns");
        fvNominalVal.addElement("talk.politics.mideast");
        fvNominalVal.addElement("talk.politics.misc");
        fvNominalVal.addElement("talk.religion.misc");
        Attribute attribute2 = new Attribute("class", fvNominalVal);
        // Create list of instances with one element
        FastVector fvWekaAttributes = new FastVector(2);
        fvWekaAttributes.addElement(attribute1);
        fvWekaAttributes.addElement(attribute2);
        instances = new Instances("Test relation", fvWekaAttributes, 1);           
        // Set class index
        instances.setClassIndex(1);
        // Create and add the instance
        Instance instance = new Instance(2);
        instance.setValue(attribute1, text);
        // Another way to do it:
        // instance.setValue((Attribute)fvWekaAttributes.elementAt(1), text);
        instances.add(instance);
         //System.out.println("===== Instance created with reference dataset =====");
        //System.out.println(instances);
    }
        
	/**
	 * This method performs the classification of the instance.
	 * Output is done at the command-line.
	 */
	public String classify() {
        try {
            double pred = classifier.classifyInstance(instances.instance(0));
            //System.out.println("===== Classified instance =====");
            System.out.println("Class predicted: " + instances.classAttribute().value((int) pred));
			return instances.classAttribute().value((int) pred);
        }
        catch (Exception e) {
            System.out.println("Problem found when classifying the text"+e.toString());
        }
		return "";
	}
        
}  