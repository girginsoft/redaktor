/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.girginsoft.sociolog.service;

import com.girginsoft.sociolog.utils.NGramAnalyzer;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;

/**
 *
 * @author girginsoft
 */
public class LanguageDetectorService {
        private static LanguageDetectorService instance  = null;
        private  String englishSource = "corpus/collect_en.txt";
        private  String turkishSource = "corpus/collect.txt";
        private EnumMap<LanguageModel, HashMap<String, Integer>> ngrams = new EnumMap<LanguageModel, HashMap<String, Integer>>(LanguageModel.class);
        private   NGramAnalyzer nga = new NGramAnalyzer(2);
        private int vocabularyCount = 0;
        public enum LanguageModel {
            TR, EN;
        };
         private LanguageDetectorService () {
            //this.englishSource = this.getClass().getClassLoader().getResource("corpus/collect_en.txt").toExternalForm();
            //this.turkishSource = this.getClass().getClassLoader().getResource("corpus/collect.txt").toExternalForm();
        }
        
        public static LanguageDetectorService getInstance() {
            if (instance == null) {
                instance  = new LanguageDetectorService();
            }
            return instance;
        }
        protected String readFromFile(String path) throws FileNotFoundException, IOException {
        String filePath = path;
        //File fileDir = new File(filePath);

        BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(path), "UTF8"));
        String text = "";
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            text = sb.toString();
        } finally {
            br.close();
        }
        return text;
    }
    public void learn() throws FileNotFoundException, IOException {
        String turkish = readFromFile(turkishSource);
        String english = readFromFile(englishSource);
        ngrams.put(LanguageModel.TR, nga.getCharBasedNGramFreq(turkish));
        vocabularyCount += ngrams.get(LanguageModel.TR).size();
        ngrams.put(LanguageModel.EN, nga.getCharBasedNGramFreq(english));
        vocabularyCount += ngrams.get(LanguageModel.EN).size();
    }
    
    public LanguageModel predict(String text) {
        ArrayList<String> tokens = nga.getCharBasedNGram(text, true);
        LanguageModel predicted = LanguageModel.TR;
        double probability = 0.0;
        for (LanguageModel model : ngrams.keySet()) {
            HashMap<String, Integer> modelGrams = ngrams.get(model);
            double localProp = 1;
            for (String token : tokens) {
                int occurence = 1;
                if (modelGrams.get(token) != null) {
                    occurence = modelGrams.get(token);
                }
                localProp  *= (double)occurence / (double)vocabularyCount;
            }
            if (localProp > probability) {
                probability = localProp;
                predicted = model;
            }
        }
        return predicted;
    } 
}
