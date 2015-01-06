/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.girginsoft.sociolog.service;

import com.girginsoft.sociolog.utils.JaroWinklerDistance;
import com.girginsoft.sociolog.utils.NGramAnalyzer;
import org.apache.commons.codec.binary.Base64;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import net.zemberek.erisim.Zemberek;
import net.zemberek.tr.yapi.TurkiyeTurkcesi;

/**
 *
 * @author girginsoft
 */
public final class SpellingCorrectionServiceTr extends SpellingCorrectorAbstract{

    private static SpellingCorrectionServiceTr instance = null;
    private String path = "corpus/collect.txt";
    private final static HashMap<Character, Character> turkishAsciiChars = new HashMap<Character, Character>();
    private  Zemberek zemberek = new Zemberek(new TurkiyeTurkcesi());

    private SpellingCorrectionServiceTr() {
        //this.path = this.getClass().getClassLoader().getResource("corpus/collect.txt").toExternalForm();
        turkishAsciiChars.put('ç', 'c');
        turkishAsciiChars.put('ğ', 'g');
        turkishAsciiChars.put('ı', 'i');
        turkishAsciiChars.put('ö', 'o');
        turkishAsciiChars.put('ş', 's');
        turkishAsciiChars.put('ü', 'u');
    }

    public static SpellingCorrectionServiceTr getInstance() {
        if (instance == null) {
            instance = new SpellingCorrectionServiceTr();
        }
        return instance;
    }

    public String predict(String example, HashMap<String, ArrayList<String>> allSuggestions) {
        String prediction = example;
        System.out.println(example + " " + System.currentTimeMillis());
        List<String> words = Arrays.asList(this.split(example));
        //Zemberek zemberek = new Zemberek(new TurkiyeTurkcesi());
        //Collections.reverse(words);
        int index = 0;
        for (String element : words) {
            ArrayList<String> suggestions = new ArrayList<String>();
            //double probability = 0.0009;
            double probability = 0.0009;
            String current = this.clean(element);
            if (current.equals("") ||
                (Character.isUpperCase(current.charAt(0)) && index > 0)) {
                continue;
            }
            current = current.toLowerCase(new Locale("tr", "TR"));
            if (current.matches("\\d+")) {
                continue;
            }
          // if (zemberek.kelimeDenetle(current) ) { 
             //  byte[] subKey = Base64.encodeBase64(current.getBytes());
               //wordMap.put(new String(subKey), null);
            //    continue;
            //}
            int j = 0;
            ArrayList<String> predictions = new ArrayList<String>();
            for (String key : wordMap.keySet()) {
                String target = key;
                //target = zemberek.asciiyeDonustur(target);
                int diff = distance(current, target);
                if (diff > current.length() * 0.51) {
                    continue;
                }

                int freq = this.getFreq().get(key);
                double k = (diff + 1) * (1000 / (diff + 1));
                double likelihood = ((double) freq / k);
                prev_item_check:
                {
                    if ((index - 1) > 0 && diff > 0) {
                        String prev = words.get(index - 1);
                      ArrayList<String> targetPrevItems = wordMap.get(target);
                        if (targetPrevItems == null) {
                            break prev_item_check;
                        }
                        HashSet hs = new HashSet();
                        hs.addAll(targetPrevItems);
                        ArrayList<String> uniqueTargetPrevItems = new ArrayList<String>();
                        uniqueTargetPrevItems.addAll(hs);
                        boolean found  = false;
                        for (String targetPrevItem : uniqueTargetPrevItems) {
                            int localDistance = distance(prev, targetPrevItem);
                            int localFrequency = 0;
                            if (localDistance > current.length() * 0.405) {
                                continue;
                            }
                            for (String prevItem : targetPrevItems) {
                                if (prevItem.equals(targetPrevItem)) {
                                    localFrequency++;
                                }
                            }
                            double localK = (localDistance + 1) * (1000 * (localDistance + 1)) ;
                            double localLikelihood = ((double) localFrequency / localK);
                            likelihood *= localLikelihood;
                            if (0.0000009 < likelihood) {
                                if (!predictions.contains(target)) {
                                    predictions.add(target);
                                    found = true;
                                }
                            }
                        }
                        if (!found) {
                            break prev_item_check;
                        }
                        continue;
                    }
                }
                if (probability < likelihood) {
                    predictions.add(target);
                }
            }
            int maxDistance = 10000000;
            String replace = current;
            boolean cleanFound = false;
            for (int i = predictions.size() - 1; i >= 0; i--) {
                int counter = 0;
                for (String curPrediction : checkForAdditional(predictions.get(i))) {
                    //String curPrediction = predictions.get(i); 
                   int dist = distance(current, curPrediction);
                   
                    if (maxDistance >= dist) {
                        if (maxDistance > dist) {
                            suggestions.clear();
                        } 
                        if (!suggestions.contains(curPrediction)) {
                            suggestions.add(curPrediction);
                        }
                        if (maxDistance == dist) {
                            JaroWinklerDistance jw = new JaroWinklerDistance();
                            double curdist = jw.compare(current, curPrediction);
                            double prevdist = jw.compare(current, replace);
                            if (Math.abs(curdist - prevdist) < 0.2) {
                                if (this.getFreq().get(curPrediction) != null && this.getFreq().get(replace) !=null ) {
                                   curdist *= this.getFreq().get(curPrediction);
                                    prevdist *=  this.getFreq().get(replace) ;
                                }
                            }
                            if (curdist < prevdist)  {
                                continue;
                            }
                            if ( this.getFreq().get(curPrediction) == null) {
                                continue;
                            }
                        }
                        maxDistance = dist;
                        replace = curPrediction;
                    }
                    counter++;
                }
            }
            allSuggestions.put(current, suggestions);
            double rate = 0.25;
            if (current.length() <= 6) {
                rate = 0.6;
            }  
            if (maxDistance < current.length() * rate) {
                element = element.replaceAll("([^a-zıöğşçüA-ZİŞÇÖĞÜ0-9'])", "");
                prediction = prediction.replaceAll("\\b" + Pattern.quote(element)+ "\\b", replace);
            }
            index++;

        }
        System.out.println(prediction + " " + System.currentTimeMillis());
        return prediction;
    }
    private ArrayList<String> checkForAdditional(String word) {
        ArrayList<String> words = new ArrayList<String>();
        words.add(word);
        String[] additions = new  String[]{
                        "le", "la", "ler", "lar",
                        "mu", "mı", "mi", "mü", 
                        "im", "ım", "um", "üm", 
                        "yim", "yım", "yum", "yüm", 
                        "imiz", "ımız", "ümüz", "umuz", 
                        "imin", "ımın", "ümün", "umun", 
                        "iniz", "ınız", "ünüz", "unuz", 
                        "nız", "niz", "nuz", "nüz",
                        "ız", "iz", "uz", "üz",
                        "siniz", "sınız", "sünüz", "sunuz", 
                        "sini", "sını", "sünü", "sunu", 
                        "ten", "den", "dan", "tan", "de", "te", "ta", "da",
                        "nin", "nun", "nün", "nın",
                        "imi", "ımı", "ümü", "umu",
                        "ini", "ını", "ünü", "unu",
                        "sini", "sını", "sünü", "sunu",
                        "un", "ün", "ın", "in", "sın", "sin", "sun", "sün",
                        "min", "mın",
                        "i", "ı","u", "ü",
                        "e", "a",
                        "m", 
                        "sın", "sin", "sün", "sun",
                        "sını", "sini", "sünü", "sunu",
                        "li", "lı",
                        "ken", "ki", "daş",
                        "ıp", "yıp", "ip", "yip",
                        "mış",  "miş", "muş", "müş",
                        "ymış",  "ymiş", "ymuş", "ymüş",
                        "mak", "mek", "yor", "ken",
                        "dır", "dir", "dur", "dür"
                
        };
        for (String addition : additions) {
            if (!word.endsWith(addition) && zemberek.kelimeDenetle(word + addition)) {
                words.add( word + addition);
            } else if (word.endsWith(addition) ) {
                String extracted = word.substring(0, word.lastIndexOf(addition));
                if (zemberek.kelimeDenetle(extracted)) {
                    words.addAll(checkForAdditional(extracted));
                }
                //System.out.println(extracted);
                
            }
        }
        return words;
        
    }

    private String checkForAsciiTurkishChars(String current, String target) {
        int distance = distance(current, target);
        for (Character key : turkishAsciiChars.keySet()) {
            for (int i = 0; i < current.length(); i++) {
                if (key == current.charAt(i)) {
                    char[] currentChars = current.toCharArray();
                    currentChars[i] = turkishAsciiChars.get(key);
                    String temp = String.valueOf(currentChars);
                    if (current.equals(temp)) {
                        break;
                    }
                    int tempDistance = distance(temp, target);
                    //System.out.println(temp + "====>" + target + " ====>" + current + " distance = " + distance + " =>" + tempDistance);
                    if (distance <= tempDistance) {
                        continue;
                    }
                    current = temp;
                }
            }
        }
        return current;
    }

    @Override
    protected String getPath() {
        return this.path;
    }
}
