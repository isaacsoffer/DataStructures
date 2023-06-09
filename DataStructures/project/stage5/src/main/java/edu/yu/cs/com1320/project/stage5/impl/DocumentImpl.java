package edu.yu.cs.com1320.project.stage5.impl;

import java.net.URI;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collections;

import edu.yu.cs.com1320.project.stage5.*;
import java.lang.System;
import java.lang.Math;

public class DocumentImpl implements Document/* extends Comparable<Document>*/ {
    private long lastTimeUsed;
    private String documentTxt;
    private byte[] documentBinaryData;
    private URI key;
    private HashMap<String, Integer> map;
    private Set<String> wordsPresent;
    private Map<String, Integer> stage5Map;

    public DocumentImpl(URI uri, String text, Map<String, Integer> wordCountMap){
        if(uri == null || text == null || text.isEmpty()){
            throw new IllegalArgumentException("Please pass in a valid parameters");
        }
        this.key = uri;
        this.documentTxt = text;
        this.documentBinaryData = null;
        this.map = new HashMap<>();
        wordsPresent = new HashSet<>();
        this.lastTimeUsed = 0;
        if(wordCountMap == null){
            addWordsToMap(text);
            this.stage5Map = this.map;
        } else{
            this.stage5Map = wordCountMap;
        }
    }

    public DocumentImpl(URI uri, byte[] binaryData){
        if(uri == null){
            throw new IllegalArgumentException("Please pass in a valid URI");
        }
        if(binaryData == null || binaryData.length == 0){
            throw new IllegalArgumentException("Please pass in a valid array Document.constructor 2 ");
        }
        this.key = uri;
        this.documentBinaryData = fixByteArray(binaryData);
        this.documentTxt = null;
        this.map = new HashMap<>();
        wordsPresent = new HashSet<>();
        this.lastTimeUsed = 0;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + (documentTxt != null ? documentTxt.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(documentBinaryData);
        return Math.abs(result);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(!(obj instanceof DocumentImpl)){
            return false;
        }
        if(this.hashCode() == obj.hashCode()){
            return true;
        }
        return false;
    }

    /**
     * @return content of text document
     */
    public String getDocumentTxt(){
        return this.documentTxt;
    }

    public byte[] getDocumentBinaryData(){
        return this.documentBinaryData;
    }

/**
 * @return URI which uniquely identifies this document
 */
    public URI getKey(){
        return this.key;
    }

/**
 * how many times does the given word appear in the document?
 * @param word
 * @return the number of times the given words appears in the document. If it's a binary document, return 0.
 */
    public int wordCount(String word){
        if(word == null){
            throw new IllegalArgumentException("Please pass in a valid word");
        }
        if(map.containsKey(word)){
            return map.get(word);
        }
        return 0;
    }

/**
 * @return all the words that appear in the document
 */
    public Set<String> getWords() {
        return Collections.unmodifiableSet(this.wordsPresent);
    }



    /**
     * return the last time this document was used, via put/get or via a search result
     * (for stage 4 of project)
     */
    public long getLastUseTime(){
        return this.lastTimeUsed;
    }

    public void setLastUseTime(long timeInNanoseconds){
        this.lastTimeUsed = timeInNanoseconds;
    }

    @Override
    public int compareTo(Document doc2){
        if(this.lastTimeUsed > doc2.getLastUseTime()){
            return 1;
        }
        if(this.lastTimeUsed < doc2.getLastUseTime()){
            return -1;
        }
        return 0;
    }
    /**
     * @return a copy of the word to count map so it can be serialized
     */
    public Map<String,Integer> getWordMap(){
        return new HashMap(this.stage5Map);
    }

    /**
     * This must set the word to count map during deserialization
     * @param wordMap
     */
    public void setWordMap(Map<String,Integer> wordMap){
        this.stage5Map = wordMap;
    }

    //added method
    private void addWordsToMap(String data) {
        String[] dataArray = data.split(" ");
        for(int i = 0; i < dataArray.length; i++){
            //get only the characters of each word
            String temp = dataArray[i]/*.replaceAll("[^a-zA-Z0-9]", "")*/;
            wordsPresent.add(temp);
            if(map.containsKey(temp)) {
                map.put(temp, map.get(temp) + 1);
            } else {
                map.put(temp, 1);
            }
        }
    }

    private byte[] fixByteArray(byte[] byteArray) {
        String str = new String(byteArray);
        return str.getBytes();
    }



}
