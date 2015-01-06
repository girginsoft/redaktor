/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.girginsoft.sociolog.service;

import com.girginsoft.sociolog.utils.JaroWinklerDistance;
import com.girginsoft.sociolog.utils.NGramAnalyzer;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import net.zemberek.erisim.Zemberek;
import net.zemberek.tr.yapi.TurkiyeTurkcesi;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author girginsoft
 */
public abstract class SpellingCorrectorAbstract {
    protected String path = "/Users/girginsoft/Documents/collect.txt";
    protected Map<String, ArrayList<String>> wordMap = null;
    private HashMap<String, Integer> frequency = null;
    private  Zemberek zemberek = new Zemberek(new TurkiyeTurkcesi());

    protected String readFromFile() throws FileNotFoundException, IOException {
        String filePath = getPath();
        //File fileDir = new File(filePath);

        BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(filePath), "UTF8"));
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

    protected String[] split(String text) {
        String[] words = text.split("\\ |\\.|\\,|\\?|\\!");
        return words;
    }

    protected String clean(String word) {
        word = word.trim();
        word = word.replaceAll("[^\\p{L}\\p{Nd}]+", "");
        return word;
    }

    public void learn() throws FileNotFoundException, IOException {
        String text = readFromFile().toLowerCase(new Locale("tr", "TR")).replaceAll("[^\\p{L}\\p{Nd}\\s]+", "");
        String[] words = this.split(text);
        NGramAnalyzer nga = new NGramAnalyzer(2);
        ArrayList<String> ngrams = new ArrayList<String>(); //nga.getNGramElements(text);
        List<String> tokens = Arrays.asList(words);
        ngrams.addAll(tokens);
        //ngrams.addAll(Collections.);
        this.wordMap = new HashMap<String, ArrayList<String>>();
        this.frequency = new HashMap<String, Integer>();
        int index = 0;
        for (String word : ngrams) {
            //word = this.clean(word);
            if (word.equals("")) {
                index++;
                continue;
            }
            ArrayList<String> vocab = wordMap.get(word);
            if (vocab == null) {
                vocab = new ArrayList<String>();
                if (index > 0) {
                    String prev = ngrams.get(index - 1);
                    if (prev.equals("")) {
                        index++;
                        continue;
                    }
                    vocab.add(prev);
                }
                frequency.put(word, 1);
            } else {
                if (index != 0) {
                    String previous = ngrams.get(index - 1);
                    if (previous.equals("")) {
                        index++;
                        continue;
                    }
                    vocab.add(previous);
                }
                frequency.put(word, frequency.get(word) + 1);
            }
            wordMap.put(word, vocab);
            index++;
        }
        System.out.println(this.getClass().getName() + " num of token  " + tokens.size());
        System.out.println(this.getClass().getName() + " num of unique  " + frequency.size());

    }

    public int distance(String a, String b) {
        a = a.toLowerCase(new Locale("tr", "TR"));
        b = b.toLowerCase(new Locale("tr", "TR"));
        a = zemberek.asciiyeDonustur(a);
        b = zemberek.asciiyeDonustur(b);
        // i == 0
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++) {
            costs[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    public abstract String predict(String example, HashMap<String, ArrayList<String>> allSuggestions);
    abstract protected String getPath();
    
    protected Map<String, ArrayList<String>> getWordMap () {
        return this.wordMap;
    }
    
    protected HashMap<String, Integer> getFreq() {
        return this.frequency;
    }
}
