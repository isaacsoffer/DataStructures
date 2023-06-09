package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import edu.yu.cs.com1320.project.stage5.impl.*;
import edu.yu.cs.com1320.project.stage5.*;
import java.net.URI;
import java.net.URISyntaxException;

public class BTreeImplTest{
    @Test 
    void testGetAndPut1(){
        BTree<Integer, String> tree = new BTreeImpl();

        //one by one - get is called on most recent
        tree.put(10, "ten");
        assertEquals("ten", tree.get(10));
        tree.put(100, "hundred");
        assertEquals("hundred", tree.get(100));
        tree.put(20, "twenty");
        assertEquals("twenty", tree.get(20));

        //get isn't most recent noq, but still in tree
        assertEquals("ten", tree.get(10));
        //test in chunks
        tree.put(0, "zero");
        tree.put(1, "one");
        tree.put(2, "two");
        tree.put(3, "three");

        assertEquals("zero", tree.get(0));
        assertEquals("one", tree.get(1));
        assertEquals("two", tree.get(2));
        assertEquals("three", tree.get(3));
    }

    @Test 
    void testGetInValid(){
        BTree<Integer, String> tree = new BTreeImpl();
        tree.put(0, "zero");
        tree.put(1, "one");
        tree.put(2, "two");
        tree.put(3, "three");

        assertNull(tree.get(10));
        assertNull(tree.get(20));
        assertNull(tree.get(30));
        assertNull(tree.get(40));
    } 

    @Test 
    void testMoveToDiskTextDocs() throws Exception{
        BTree<URI, Document> tree = new BTreeImpl();
        tree.setPersistenceManager(new DocumentPersistenceManager());
        
        String text = "aiod dpos aidk iwdsis";
        URI uri = new URI("https://mail.google.com/mail/u/BTreeImplTest/ShouldSerialize/ThenDelete");
        Document doc1 = new DocumentImpl(uri,text, null);
        
        String second = "The school is off tomorrow";
        URI uri2 = new URI("https://stackoverflow.com/questions/22011200/creating-hashmap-from-a-json-string");
        Document doc2 = new DocumentImpl(uri2, second, null);

        tree.put(uri, doc1);
        tree.put(uri2, doc2);
        System.out.println("Document uri before serialization = " + tree.get(uri).getKey().toString());
        System.out.println("Document text before serialization = " + tree.get(uri).getDocumentTxt());

        assertTrue(doc1.equals(tree.get(uri)));
        assertTrue(doc2.equals(tree.get(uri2)));
    
        //not sure how to test this, because a get will deserialize
        //at least make sure a get still deserializes 
        // test for String docs
        tree.moveToDisk(uri);
        tree.moveToDisk(uri2);
        //Document afterSerialize = tree.get(uri);
        // System.out.println("Document uri after serialization = " + afterSerialize.getKey().toString());
        // System.out.println("Document text after serialization = " + afterSerialize.getDocumentTxt());
        assertTrue(doc1.equals(tree.get(uri)));
        assertTrue(doc2.equals(tree.get(uri2)));
    }

    @Test
    void testMoveToDiskBytes() throws Exception{
        BTree<URI, Document> tree = new BTreeImpl();
        tree.setPersistenceManager(new DocumentPersistenceManager());

        String third = "I love Hashem, and I love his Torah and mitzvot";
        URI uri3 = new URI("https://stackoverflow.com/questions/22011200/creating-hashmap-from-a-json-string");
        Document doc3 = new DocumentImpl(uri3, third.getBytes());
        
        String fourth = "it is lovely day outside tommy";
        URI uri4 = new URI("https://mail.google.com/mail/u/0");
        Document doc4 = new DocumentImpl(uri4 ,fourth.getBytes());

        tree.put(uri3, doc3);
        tree.put(uri4, doc4);

        assertTrue(doc3.equals(tree.get(uri3)));
        assertTrue(doc4.equals(tree.get(uri4)));

        tree.moveToDisk(uri3);
        tree.moveToDisk(uri4);

        assertTrue(doc3.equals(tree.get(uri3)));
        assertTrue(doc4.equals(tree.get(uri4)));
    }

    //copied and adapted from my hashTable tests
    @Test
    void testTable(){
       BTree<Integer, String> table = new BTreeImpl<Integer, String>();
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
        BTree<Integer, String> table = new BTreeImpl<Integer, String>();
        String str = table.put(1, "one");
        str = table.put(6, "six"); //should also hash to same as 1
        assertTrue(table.get(6).equals("six"));
        assertEquals("one", table.get(1));
    }

}