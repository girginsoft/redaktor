/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.girginsoft.sociolog.utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author girginsoft
 */
public class SentencesProcessor {
    private static SentencesProcessor ourInstance = new SentencesProcessor();
   
    public static SentencesProcessor getInstance() {
        return ourInstance;
    }

    private SentencesProcessor() {
    }

    public ArrayList<String> process(String text) {
        Pattern pattern = Pattern.compile("(([a-zıçöğüş\\)\\]\\(\\[\\:\\s])([\\!\\?\\.]))(.?[\\s?A-ZİĞÜÇŞÖa-z0-9\\n\\\"])");
        Matcher matcher = pattern.matcher(text);
        ArrayList<String> sentences = new ArrayList<String>();
        int start = 0;
        int end = 0;
        while (matcher.find()) {
            end = matcher.end(3);
            sentences.add(text.substring(start, end).trim());
            start = matcher.end(3);
        }
        if (!sentences.contains(text.substring(start, text.length()))) {
            sentences.add(text.substring(start, text.length()));
        }
        return sentences;
    }

    public ArrayList<String> process(String text, String seek) {
        ArrayList<String> sentences = process(text);
        ArrayList<String> highlighted = new ArrayList<String>();
        for(String sen : sentences) {
            if (sen.contains(seek)) {
                System.out.println(sen);
                highlighted.add(sen);
            }
        }
        return highlighted;
    }
}