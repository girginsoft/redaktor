/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.girginsoft.sociolog.utils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

public class NGramAnalyzer extends Analyzer {

    private int minGram;
    private int maxGram;

    public NGramAnalyzer(int minGram, int maxGram) {
        this.minGram = minGram;
        this.maxGram = maxGram;
    }

    public NGramAnalyzer(int ngram) {
        this.minGram = ngram;
        this.maxGram = ngram;
    }

    @Override
    protected TokenStreamComponents createComponents(String arg0, Reader reader) {

        Tokenizer source = new StandardTokenizer(Version.LUCENE_40, reader);

        TokenStream filter = new ShingleFilter(source, minGram, maxGram);
        filter = new LowerCaseFilter(Version.LUCENE_40, filter);
        filter = new StopFilter(Version.LUCENE_40, filter,
                StopAnalyzer.ENGLISH_STOP_WORDS_SET);

        return new TokenStreamComponents(source, filter);
    }

    public ArrayList<String> getNGramElements(String text) throws IOException {
        Reader reader = new StringReader(text);
        TokenStream tokenizer = this.tokenStream(null, reader);
        CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
        tokenizer.reset();
        ArrayList<String> ngrams =new  ArrayList<String>();
        while (tokenizer.incrementToken()) {
            String token = charTermAttribute.toString();
            String[] tokens = token.split(" ");
            for(int i = minGram; i <= maxGram; i ++) {
                  if (tokens.length == i) {
                      ngrams.add(token);
                  }
            }
        }
        tokenizer.close();
        return ngrams;
    }
     public ArrayList<String> getFullNGramElements(String text) {
       ArrayList<String> ngrams = new  ArrayList<String>();
         try {
            Reader reader = new StringReader(text);
            TokenStream tokenizer = this.tokenStream(null, reader);
            CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
            tokenizer.reset();
            while (tokenizer.incrementToken()) {
                String token = charTermAttribute.toString();
                 ngrams.add(token);
            }
            tokenizer.close();
        } catch (IOException ex) {
            Logger.getLogger(NGramAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
        return ngrams;
    }
     public ArrayList<String> getCharBasedNGram(String text, boolean unique) {
         ArrayList<String> ngrams = new  ArrayList<String>();
         try {
            Reader reader = new StringReader(text.toLowerCase(new Locale("tr", "TR")));
            TokenStream tokenizer = new StandardTokenizer(Version.LUCENE_CURRENT, reader);
            tokenizer = new NGramTokenFilter(tokenizer, minGram, maxGram);
            CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
            tokenizer.reset();
            while (tokenizer.incrementToken()) {
                String token = charTermAttribute.toString();
                //Do something
                if (unique) {
                    if (!ngrams.contains(token)) {
                        ngrams.add(token);
                    }
                } else {
                        ngrams.add(token);       
                }
            }
         } catch (IOException ex) {
             ex.printStackTrace();
         }
            return ngrams;
     }
     
       public HashMap<String, Integer> getCharBasedNGramFreq(String text) {
         HashMap<String, Integer> freqs = new HashMap<String, Integer>();
         try {
            Reader reader = new StringReader(text);
            TokenStream tokenizer = new StandardTokenizer(Version.LUCENE_CURRENT, reader);
            tokenizer = new NGramTokenFilter(tokenizer, minGram, maxGram);
            CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
            tokenizer.reset();
            while (tokenizer.incrementToken()) {
                String token = charTermAttribute.toString();
                //Do something
                int freq = 1;
                if (freqs.get(token) != null) {
                    freq = freqs.get(token);
                }
                freqs.put(token, freq + 1);       
                
            }
         } catch (IOException ex) {
             ex.printStackTrace();
         }
         return freqs;
     }
     
}
