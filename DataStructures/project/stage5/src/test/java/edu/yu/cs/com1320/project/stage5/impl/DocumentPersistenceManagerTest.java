//package edu.yu.cs.com1320.project.stage5.impl;
//for some reason it doesn't work
import edu.yu.cs.com1320.project.stage5.impl.*;
import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.stage5.*;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentPersistenceManager;
import  java.lang.reflect.Type;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.Set;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import static org.junit.jupiter.api.Assertions.*;
import java.security.SecureRandom;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.xml.bind.DatatypeConverter;
import com.google.gson.JsonSerializationContext;
import java.io.BufferedWriter;
import java.io.FileWriter;


    

public class DocumentPersistenceManagerTest {
    private File baseDir = new File(System.getProperty("user.dir"));
    private void writeStringToFile(String filePath, String content) throws IOException {
        // Create parent directories if they don't exist
        Path file = Path.of(filePath);
        Files.createDirectories(file.getParent());
        File towriteTo = new File(filePath + ".json");

        // Write the content to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(towriteTo))) {
            writer.write(content);
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
        }
    }
    private class Tester implements JsonSerializer<Document> {
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
            //System.out.println("filePath = " + filePath);
            Path directoryPath = Paths.get(filePathNoFileName);
            //System.out.println("directory path = " + directoryPath.toString());
            try {
                writeStringToFile(filePath, jsonObject.toString());
                // Path newDirectory = Files.createDirectories(directoryPath.getParent().resolve(filePathNoFileName));
                // System.out.println("newDirectory = " + newDirectory);                
                // System.out.println("trying create file for File name = " + fileName);
                // System.out.println("newFile = " + newFile);
                // Path newDirectory = Files.createDirectories(directoryPath);
                // File f = new File(newDirectory + fileName);
                // f.mkdirs();
                // FileWriter fileWriter = new FileWriter(f);
                // BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                // bufferedWriter.write(jsonObject.toString());
            } catch (IOException e) {
                try{
                    writeStringToFilePostException(filePath, jsonObject.toString());
                } catch(IOException e2){
                    return null;
                }
            }
            //System.out.println("about to return serialize inner = " + jsonObject.toString() );
            return gson.fromJson(jsonObject.toString(), JsonElement.class);

    


            // Path dir = Paths.get(doc.getKey().getHost(), doc.getKey().getPath());
            // Files.createDirectories(dir);
            // try {
            //     Files.writeString(dir, jsonObject.getAsString(), StandardCharsets.UTF_8); //stackExchnage said - with no option - it will create if no file, it will overwrite (truncate) other one if present
            // } catch (IOException e) {
            //     e.printStackTrace();
            // }
        }
    }

    private byte[] generateRandomByteArray(int length) {
        byte[] byteArray = new byte[length];
        SecureRandom random = new SecureRandom();
        random.nextBytes(byteArray);
        return byteArray;
    }
    @Test 
    void testDeAndSerializeValidInputNoDirGiven() throws IOException, URISyntaxException{
        PersistenceManager<URI, Document> pm = new DocumentPersistenceManager(); 
        String text = "aiod dpos aidk iwdsis";
        URI uri = new URI("https://mail.google.com/mail/u/0");
        Document doc1 = new DocumentImpl(uri, text, null);

        String third = "I love Hashem, and I love his Torah and mitzvot";
        URI uri3 = new URI("https://piazza.com/class/ld1z76nargg45e/post/368");
        Document doc3 = new DocumentImpl(uri3, third, null);

        byte[] bytes = generateRandomByteArray(10);        
        URI uri2 = new URI("https://docs.oracle.com/javase/8/docs/api/java/io/FileWriter.html");
        String s = new String(bytes);
        Document doc2 = new DocumentImpl(uri2, s.getBytes());

        pm.serialize(uri, doc1);
        pm.serialize(uri2, doc2);
        pm.serialize(uri3, doc3);

        Document serialized1 = pm.deserialize(uri);
        Document serialized2 = pm.deserialize(uri2);
        Document serialized3 = pm.deserialize(uri3);
        
        assertTrue(doc1.equals(serialized1), "expected deserialization to return an equivalent doc");
        assertTrue(doc2.equals(serialized2), "expected deserialization to return an equivalent doc");
        assertTrue(doc3.equals(serialized3), "expected deserialization to return an equivalent doc");

    }

    @Test 
    void testDeAndSerializeValidInput() throws IOException, URISyntaxException{
        PersistenceManager<URI, Document> pm = new DocumentPersistenceManager(new File(System.getProperty("user.dir"))); 
        String text = "aiod dpos aidk iwdsis";
        URI uri = new URI("https://mail.google.com/mail/u/0");
        Document doc1 = new DocumentImpl(uri, text, null);

        byte[] bytes = new byte[10];
        bytes[0] = 10;
        bytes[9] = 0;
        URI uri2 = new URI("https://docs.oracle.com/javase/8/docs/api/java/io/FileWriter.html");
        Document doc2 = new DocumentImpl(uri2, bytes);

        String third = "I love Hashem, and I love his Torah and mitzvot";
        URI uri3 = new URI("https://piazza.com/class/ld1z76nargg45e/post/368");
        Document doc3 = new DocumentImpl(uri3, third, null);

        pm.serialize(uri, doc1);
        pm.serialize(uri2, doc2);
        pm.serialize(uri3, doc3);

        Document serialized1 = pm.deserialize(uri);
        Document serialized2 = pm.deserialize(uri2);
        Document serialized3 = pm.deserialize(uri3);
        
        assertTrue(doc1.equals(serialized1), "expected deserialization to return an equivalent doc");
        assertTrue(doc2.equals(serialized2), "expected deserialization to return an equivalent doc");
        assertTrue(doc3.equals(serialized3), "expected deserialization to return an equivalent doc");
    }

    @Test 
    void testDelete() throws URISyntaxException, IOException {
        PersistenceManager<URI, Document> pm = new DocumentPersistenceManager(new File(System.getProperty("user.dir"))); 
        String text = "aiod dpos aidk iwdsis";
        URI uri = new URI("https://mail.google.com/mail/u/0");
        Document doc1 = new DocumentImpl(uri, text, null);

        byte[] bytes = new byte[10];
        bytes[0] = 10;
        bytes[9] = 0;
        URI uri2 = new URI("https://docs.oracle.com/javase/8/docs/api/java/io/FileWriter.html");
        Document doc2 = new DocumentImpl(uri2, bytes);

        String third = "I love Hashem, and I love his Torah and mitzvot";
        URI uri3 = new URI("https://piazza.com/class/ld1z76nargg45e/post/368");
        Document doc3 = new DocumentImpl(uri3, third, null);
        
        pm.serialize(uri, doc1);
        pm.serialize(uri2, doc2);

        boolean delete1 = pm.delete(uri);
        boolean delete2 = pm.delete(uri2);
        boolean delete3 = pm.delete(uri3);

        assertTrue(delete1, "ie a deletion occured");
        assertTrue(delete2, "ie a deletion occured");
        assertFalse(delete3, "no deletion could've occured, since this URI was never serialized");

        try{
            Document d = pm.deserialize(uri);
            assertTrue(false, "expected exception");
        } catch(IllegalArgumentException e) {
            assertTrue(true);
        }
        try{
            Document d1 = pm.deserialize(uri2);
            assertTrue(false, "expected exception");
        } catch(IllegalArgumentException e) {
            assertTrue(true);
        }try{
            Document d3 = pm.deserialize(uri3);
            assertTrue(false, "expected exception");
        } catch(IllegalArgumentException e) {
            assertTrue(true);
        }
    }


    @Test 
    void testDeleteAfterDeseialized() throws URISyntaxException, IOException {
        PersistenceManager<URI, Document> pm = new DocumentPersistenceManager(); 
        String text = "aiod dpos aidk iwdsis";
        URI uri = new URI("https://mail.google.com/mail/u/0");
        Document doc1 = new DocumentImpl(uri, text, null);

        byte[] bytes = new byte[10];
        bytes[0] = 10;
        bytes[9] = 0;
        URI uri2 = new URI("https://docs.oracle.com/javase/8/docs/api/java/io/FileWriter.html");
        Document doc2 = new DocumentImpl(uri2, bytes);

        String third = "I love Hashem, and I love his Torah and mitzvot";
        URI uri3 = new URI("https://piazza.com/class/ld1z76nargg45e/post/368");
        Document doc3 = new DocumentImpl(uri3, third, null);
        
        pm.serialize(uri, doc1);
        pm.serialize(uri2, doc2);
        pm.serialize(uri3, doc3);

        Document deserialzed1 = pm.deserialize(uri);
        Document deserialzed2 = pm.deserialize(uri2);

        boolean delete1 = pm.delete(uri);
        boolean delete2 = pm.delete(uri2);
        boolean delete3 = pm.delete(uri3);

        assertTrue(delete3, "ie a deletion occured");
        assertFalse(delete1, "no deletion, since the doc was deserialzed");
        assertFalse(delete2, "no deletion, since the doc was deserialzed");

    }


    @Test 
    void testDeAndSerializeAfterDeletes() throws IOException, URISyntaxException{
        PersistenceManager<URI, Document> pm = new DocumentPersistenceManager(); 
        String text = "aiod dpos aidk iwdsis";
        URI uri = new URI("https://mail.google.com/mail/u/0");
        Document doc1 = new DocumentImpl(uri, text, null);

        byte[] bytes = new byte[10];
        bytes[0] = 10;
        bytes[9] = 0;
        URI uri2 = new URI("https://docs.oracle.com/javase/8/docs/api/java/io/FileWriter.html");
        Document doc2 = new DocumentImpl(uri2, bytes);

        String third = "I love Hashem, and I love his Torah and mitzvot";
        URI uri3 = new URI("https://piazza.com/class/ld1z76nargg45e/post/368");
        Document doc3 = new DocumentImpl(uri3, third, null);

        pm.serialize(uri, doc1);
        pm.serialize(uri2, doc2);
        pm.serialize(uri3, doc3);

        boolean delete1 = pm.delete(uri);
        boolean delete2 = pm.delete(uri2);
        boolean delete3 = pm.delete(uri3);

        pm.serialize(uri, doc1);
        pm.serialize(uri2, doc2);
        pm.serialize(uri3, doc3);

        Document serialized1 = pm.deserialize(uri);
        Document serialized2 = pm.deserialize(uri2);
        Document serialized3 = pm.deserialize(uri3);
        
        assertTrue(doc1.equals(serialized1), "expected deserialization to return an equivalent doc");
        assertTrue(doc2.equals(serialized2), "expected deserialization to return an equivalent doc");
        assertTrue(doc3.equals(serialized3), "expected deserialization to return an equivalent doc");
    }

    //failed tests
    // Test method name: stage5TestSerializationPath
    // Test point value: 20
    // Test description: test that serialized documents get serialized to the expected path
    @Test 
    void stage5TestSerializationPath() throws IOException, URISyntaxException{
        PersistenceManager<URI, Document> pm = new DocumentPersistenceManager(); 

        String third = "I love Hashem, and I love his Torah and mitzvot";
        //never before use uri - check manually if writing worked, but also in code
        URI uri = new URI("https://www.marcobehler.com/guides/java-files");
        String path = "";
        if(uri.getHost() != null){
            path = uri.getHost();
        }
        path = path + uri.getPath(); // Get the path part of the URI
        Document doc3 = new DocumentImpl(uri, third, null);
        
        Tester iS = new Tester();
        JsonElement jE = iS.serialize(doc3, Document.class, new Bot());
        pm.serialize(uri, doc3);

        FileInputStream fis = new FileInputStream(path + ".json");
        String result = readFromInputStream(fis);
        System.out.println("read from folder = " + result);
        assertNotNull(result.toLowerCase());   
        assertTrue(jE.toString().equals(result.trim()));
    }


 

    @Test 
    void copyOfAboveTest() throws IOException, URISyntaxException{
        PersistenceManager<URI, Document> pm = new DocumentPersistenceManager(); 
        String text = "aiod dpos aidk iwdsis";
        URI uri = new URI("https://www.zichru.com/nazir-daf-5");
        Document doc1 = new DocumentImpl(uri, text.getBytes());
        Document doc2 = new DocumentImpl(uri, text.getBytes());

        pm.serialize(uri, doc1);
        Tester iS = new Tester();
        JsonElement jE = iS.serialize(doc2, Document.class, new Bot());

        String path = "";
        if(uri.getHost() != null){
            path = uri.getHost();
        }
        path = path + uri.getPath(); // Get the path part of the URI

        FileInputStream f = new FileInputStream(path + ".json");
        String result = readFromInputStream(f);
        System.out.println("read from folder2 = " + result.trim());
        System.out.println("JE string = " + jE.toString());

        assertTrue(jE.toString().equals(result.trim()));



    }

    @Test 
    void stage5TestSerializationPathTake2() throws IOException, URISyntaxException{
        PersistenceManager<URI, Document> pm = new DocumentPersistenceManager(); 

        byte[] bytes = new byte[10];
        bytes[0] = 10;
        bytes[9] = 0;
        URI uri = new URI("https://www.zichru.com/gittin-daf-5");
        Document doc2 = new DocumentImpl(uri, bytes);
        
        String path = "";
        if(uri.getHost() != null){
            path = uri.getHost();
        }
        path = path + uri.getPath(); // Get the path part of the URI
        String filePath = System.getProperty("user.dir").toString() + "\\" + path.replace("/", File.separator); // Convert path to file path        
        
        pm.serialize(uri, doc2);
        //check files manually that it works 
        assertTrue(false, "check files manually that the expected spot is there");   
    }

    @Test 
    void testDeserializeRemoveFromDirectorys() throws IOException, URISyntaxException{
        PersistenceManager<URI, Document> pm = new DocumentPersistenceManager(); 

        byte[] bytes = new byte[10];
        bytes[0] = 10;
        bytes[9] = 0;
        URI uri = new URI("https://www.zichru.com/sotah-daf-44");
        Document doc2 = new DocumentImpl(uri, bytes);
        
        String path = "";
        if(uri.getHost() != null){
            path = uri.getHost();
        }
        path = path + uri.getPath(); // Get the path part of the URI
        String filePath = System.getProperty("user.dir").toString() + "\\" + path.replace("/", File.separator); // Convert path to file path        
        File f = new File(filePath);

        pm.serialize(uri, doc2);
        assertTrue(f.exists());
        //check files manually that it works 

        pm.deserialize(uri);
        assertFalse(f.exists());
        //check manaully
    }

    @Test
    void testCreationOfFile() throws URISyntaxException, IOException {
        PersistenceManager<URI, Document> pm = new DocumentPersistenceManager(); 
        String text = "aiod dpos aidk iwdsis";
        URI uri = new URI("https://chat.openai.com/test5");
        Document doc1 = new DocumentImpl(uri, text, null);

        pm.serialize(uri, doc1);
        //check if its there

        String path = "";
        if(uri.getHost() != null){
            path = uri.getHost();
        }
        path = path + uri.getPath(); // Get the path part of the URI
        String filePath = System.getProperty("user.dir").toString() + "\\" + path.replace("/", File.separator); // Convert path to file path        
        System.out.println("trying to delete file path = " + filePath);
        deleteFile(filePath);
        //deleteDirectory(new File(filePath + ".json"));
        //essentially checking if my deleteFile works
    }
        

    private String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
        = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }


    private void deleteFile(String filePath) {
        File file = new File(filePath + ".json");

        if (file.exists()) {
            if (file.delete()) {
                System.out.println("File deleted successfully: " + filePath);
            } else {
                System.err.println("Failed to delete the file: " + filePath);
            }
        } else {
            System.err.println("File not found: " + filePath);
        }
    }

    private class Bot implements JsonSerializationContext {
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


    

    //private String readFile(String path){
    //     FileInputStream fis = new FileInputStream(path);
    //     return IOUtils.toString(fis, "UTF-8");
    // }


}
