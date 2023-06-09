package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class HashTableImplTest {
   //tests from last stage
   @Test
   void testHash(){
       HashTableImpl<Integer, String> table = new HashTableImpl<Integer, String>();
       String str = table.put(1, "one");
       assertNull(str, "return of the first time a key is added should be null");
       assertTrue(table.get(1).equals("one"), "get() should return the corresponding key of 'one'");
       str = table.put(1, "one another time");
       assertTrue(str.equals("one"), "put() should return the previous value for the given key");
       str = table.get(1);
       assertTrue(str.equals("one another time"), "get() should return the corresponding key of 'one another time'");
       str = table.put(1, null);
       assertNull(table.get(1), "Deleted the key from the table, expected get() to return null");
       str = table.put(2, "this");
       assertNull(str);
       str = table.get(2);
       assertEquals("this", str);
   }

    @Test
    void testAgain(){
        HashTableImpl<Integer, String> table = new HashTableImpl<Integer, String>();
        String str = table.put(1, "one");
        str = table.put(6, "six"); //should also hash to same as 1
        assertTrue(table.get(6).equals("six"));
    }
    @Test
    void thirdTest() throws URISyntaxException {
        HashTableImpl<URI, Document> table = new HashTableImpl<URI, Document>();
        URI uri = new URI("www.google.com");
        String txt = "test";
        Document d = table.put(uri, new DocumentImpl(uri, txt, null));
        assertEquals(txt, table.get(uri).getDocumentTxt());
        d = table.put(uri, new DocumentImpl(uri, "second doc", null));
        assertEquals("second doc", table.get(uri).getDocumentTxt());
        assertEquals(txt, d.getDocumentTxt());
    }
    @Test
    void testHashContains() throws URISyntaxException {
        HashTableImpl<String, Document> table = new HashTableImpl<String, Document>();
        //all same hashCode
        String key = "AaAa";
        String key2 = "BBBB";
        String key3 = "AaBB";
        String keyThatsNotInTableWithSameHash = "BBAa";
        String diffKey = "diffKey";
        String txt = "Loren Impsum ahduiw ahduif ahcyid zuijedb ijh ghs a o sodoieu";
        String txt2 = "Loren Iedb ijh ghs a o sodoieu";
        String txt3 = "Loren Iedb ijh ghsdvuhkijloku";
        URI uri = new URI("www.google.com");
        URI uri2 = new URI("www.yahoo.com");
        DocumentImpl doc = new DocumentImpl(uri, txt, null);
        //look in code - case where current is null
        assertFalse(table.containsKey(key));
        table.put(key2, doc);
        //where current is the key
        assertTrue(table.containsKey(key2));
        //where current is the right hash but not the right key - must loop
        DocumentImpl docdoc = new DocumentImpl(uri2, txt2, null);
        table.put(key3, docdoc);
        assertEquals(key2.hashCode(), key3.hashCode());
        assertTrue(table.containsKey(key3));
        assertFalse(table.containsKey(keyThatsNotInTableWithSameHash));
    }

    //new tests
    @Test
    void testForGrowth(){
       HashTableImpl<Integer, String> table = new HashTableImpl<Integer, String>();
       String str = "str";
       for(int i = 0; i <40; i++){
           str = str + i + " ";
           table.put(i, str);
           assertTrue(table.containsKey(0), "contains(0) failed for i = " + i);
       }
       //50 entries, so array length should be about 50/4 about 12.5
        String str1 = "str";
        for(int i = 0; i < 40; i++){
            //assertTrue(table.containsKey(i), "expected table to contain key " + i);
            str1 = str1 + i + " ";
            assertEquals(str1,table.get(i), "expected the return value to be correct for key " + i);
        }
    }

    @Test
    void testForGrowth2(){
       HashTableImpl<String, Integer> table = new HashTableImpl<String, Integer>();
        String str = "str ";
        for(int i = 0; i <32; i++){
            str = str + i + " ";
            table.put(str,i);
            if(i == 6){
                // at this point 1 and 6 should hash to the same spot
                testAgain();
            }
            assertTrue(table.containsKey("str 0 "), "contains('str') failed for i = " + i);
        }
        int i = table.get("str 0 1 2 3 4 5 6 7 ");
        int j = table.get("str 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 ");
        int k = table.get("str 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 ");
        //hash to same spot
        assertEquals(7, i);
        assertEquals(15, j);
        assertEquals(23, k);
    }

   @Test
    void testGrowth(){
       HashTableImpl<Integer, Integer> table = new HashTableImpl<Integer, Integer>();
        for(int i = 0; i < 40; i++){
            table.put(i, 2*i);
        }
       for(int i = 0; i < 40; i++){
           assertEquals(2*i, table.get(i));
       }
   }
}
