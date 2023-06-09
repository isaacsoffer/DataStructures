package edu.yu.cs.com1320.project.stage5.impl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.StringBuilder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import  java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.net.URISyntaxException;
import java.nio.file.Path; 
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.util.Arrays;
import com.google.gson.JsonObject;
import java.io.FileWriter;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedWriter;
import java.net.URLEncoder;
import java.lang.Integer;
import java.net.URL;

import javax.imageio.IIOException;
import javax.xml.bind.DatatypeConverter;
import java.nio.channels.*;

/**
 * created by the document store and given to the BTree via a call to BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {
    private File baseDir;
    private HashMap<URI, JsonElement> docsOnDisk;
    // TODO  
    //////////////////
    ///////////////
    ///////////////////////////
    /////////////// fix to private, alos BotS
    private class InnerSerialize implements JsonSerializer<Document> {
        @Override
        public JsonElement serialize(Document doc, Type type, JsonSerializationContext context){
            Gson gson = new Gson();
            JsonObject jsonObject = new JsonObject();
            URI uri = doc.getKey();
            jsonObject.addProperty("URI", gson.toJson(uri));
            if(doc.getDocumentTxt() != null){
                jsonObject.addProperty("DocumentText", gson.toJson(doc.getDocumentTxt()));
                jsonObject.addProperty("DocumentBinaryData", gson.toJson((byte[])null));
                jsonObject.addProperty("WordMap", gson.toJson(doc.getWordMap()));
            } else{
                jsonObject.addProperty("DocumentText", (String)null);
                String base64Encoded = DatatypeConverter.printBase64Binary(doc.getDocumentBinaryData());
                jsonObject.addProperty("DocumentBinaryData", base64Encoded);
            }
            String path = "";
            //write it to files
            if(uri.getHost() != null){
                path = uri.getHost();
            }
            path = path + uri.getPath(); // Get the path part of the URI
            String fileName = path.substring(path.lastIndexOf('/') + 1) + ".json"; // Extract the file name
            String filePath = baseDir.toString() + "\\" + path.replace("/", File.separator); // Convert path to file path
            String filePathNoFileName =  baseDir.toString() + "\\" + path.substring(0, path.lastIndexOf('/') + 1).replace("/", File.separator);
            Path directoryPath = Paths.get(filePathNoFileName);
            try {
                writeStringToFile(filePath, jsonObject.toString());
            } catch (IOException e) {
                try{
                    writeStringToFilePostException(filePath, jsonObject.toString());
                } catch(IOException e2){
                    return null;
                }
            }
            return gson.fromJson(jsonObject.toString(), JsonElement.class);
        }
    }

    private class InnerDeserialize implements JsonDeserializer<Document>  {
            @Override
            public Document deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
                Gson gson = new Gson();
                JsonObject jsonObject = json.getAsJsonObject();
                String uriString = gson.fromJson(jsonObject.get("URI"), String.class);
                Document doc;
                String docText = "";
                try{
                    //substring gets rid of quotes
                    URI uri = new URI(uriString.substring(1, uriString.length() - 1));
                    String path = "";
                    if(uri.getHost() != null){
                        path = uri.getHost();
                    }
                    Path dir = Paths.get(path + uri.getPath());
                    docText = gson.fromJson(jsonObject.get("DocumentText"), String.class);
                    if(docText != null){
                        String toUseinConstructor = docText.substring(1, docText.length() -1);
                        String[] temp = json.toString().split("\"WordMap\":\"");
                        String mapJson = temp[1].substring(0,temp[1].length() - 2);
                        Map<String, Integer> wordMap = jsonToMap(mapJson);
                        doc = new DocumentImpl(uri, toUseinConstructor, wordMap);
                    } else {
                        byte[] base64Decoded = DatatypeConverter.parseBase64Binary(jsonObject.get("DocumentBinaryData").getAsString());                        
                        doc = new DocumentImpl(uri, base64Decoded);
                    }
                    //remove file from drive, now that its in memory
                    deleteFile(uri);
                    return doc;
                } 
                catch (URISyntaxException e){
                //uri construction error
                    //e.printStackTrace();
                    return null;
                }  
                catch (IOException ioe){
                    //either readString or delete failed
                    //ioe.printStackTrace();
                    return null;
                } 
            }
        }

    private class BotS implements JsonSerializationContext {
        //this thing will never be used, just need to have to implement call two classes
        @Override
        public JsonElement serialize(Object src){
            return null;
        }
        @Override
        public JsonElement serialize(Object src, Type typeOfSrc){
            return null;
        }
    }

    private class BotD<Document> implements JsonDeserializationContext {
        //this thing will never be used, just need to have to implement call two classes
        @Override
        public Document deserialize(JsonElement e, Type typeOfSrc){
            return null;
        }
    }


    public DocumentPersistenceManager(){
        this.baseDir = new File(System.getProperty("user.dir"));
        docsOnDisk = new HashMap<>();
    }
    public DocumentPersistenceManager(File baseDir){
        if(baseDir != null){
            this.baseDir = baseDir;
        } else{
            this.baseDir = new File(System.getProperty("user.dir"));
        }
        docsOnDisk = new HashMap<>();
    }


    @Override
    public void serialize(URI uri, Document val) throws IOException {
        if(uri == null || val == null){
            throw new IllegalArgumentException();
        }
        InnerSerialize iS = new InnerSerialize();
        JsonElement toDisc = iS.serialize(val, Document.class, new BotS());
        if(toDisc == null){
            throw new IOException();
        }
        docsOnDisk.put(uri, toDisc);
    }

    @Override
    public Document deserialize(URI uri) throws IOException {
        if(uri == null){
            throw new IllegalArgumentException();
        }
        if(docsOnDisk.get(uri) == null){
            throw new IllegalArgumentException("No document on disk with given uri");
        }
        InnerDeserialize iD = new InnerDeserialize();
        Document doc;
        try{
            doc = iD.deserialize(docsOnDisk.get(uri), Document.class, new BotD<>());
        } catch(JsonParseException e){
            throw new RuntimeException();
            //e.printStackTrace();
        }
        docsOnDisk.remove(uri);
        return doc;
    }

    @Override
    public boolean delete(URI uri) throws IOException {
        if(uri == null){
            throw new IllegalArgumentException();
        }
        //if on disk
        JsonElement jE = docsOnDisk.remove(uri);
        //if not on disk its stored here
        //if null, no delete occured, therefore false
        return jE != null; 
    }

    

    private Map<String,Integer> jsonToMap(String json) throws JsonParseException {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        String[] array = json.split(",");
        String[] temp;
        for(int i = 0; i < array.length; i++){
            if(!array[i].contains(":")){
                //ie the word itself had a comma
                temp = new String[2];
                temp[0] = array[i];
                if(i+1 < array.length){
                    temp[1] = array[i+1];
                    i++;
                }
            } else{
                temp = array[i].split(":");
            } 
            String sub = temp[0].replaceAll("[^a-zA-Z0-9]", "");
            //couldn't just say temp[1], but temp.length - 1 is same bc length is always two
            map.put(sub, Integer.parseInt(temp[temp.length - 1].replaceAll("[^0-9]", "")));
        }
        return map;
    }

    private void writeStringToFile(String filePath, String content) throws IOException {
        // Create parent directories if they don't exist
        Path file = Path.of(filePath);
        Files.createDirectories(file.getParent());
        File towriteTo = new File(filePath + ".json");

        // Write the content to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(towriteTo))) {
            writer.write(content);
            writer.close();
        }
        //this works, but causes many more exceptions than I had before
    }

    private void writeStringToFilePostException(String filePath, String content) throws IOException {
        // Create parent directories if they don't exist
        Path file = Path.of(filePath);
        Files.createDirectories(file.getParent());

        // Write the content to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.toFile()))) {
            writer.write(content);
            writer.close();
        }
    }

    private void deleteFile(URI uri) throws IOException{
        String path = "";
        //write it to files            
        if(uri.getHost() != null){
            path = uri.getHost();
        }
        path = path + uri.getPath(); // Get the path part of the URI
        String filePath = baseDir.toString() + "\\" + path.replace("/", File.separator); // Convert path to file path
        String filePathNoFileName =  baseDir.toString() + "\\" + path.substring(0, path.lastIndexOf('/') + 1).replace("/", File.separator);
        deleteFile(filePath);
    }

    private void deleteFile(String filePath) {
        //not sure when this method will work vs when second will
        //when try only one without the other, I get errors
        File file = new File(filePath);

        if (file.exists()) {
            file.delete();
        } else {
            deleteFile2(filePath);
        }
    }

    private void deleteFile2(String filePath) {
        File file = new File(filePath + ".json");
        if (file.exists()) {
            file.delete();
        }
    }

}


