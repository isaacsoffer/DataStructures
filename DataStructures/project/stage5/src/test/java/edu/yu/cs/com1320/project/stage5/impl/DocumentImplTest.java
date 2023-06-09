package edu.yu.cs.com1320.project.stage5.impl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.beans.Transient;
import java.net.URI;
import java.net.URISyntaxException;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import java.util.Map;

public class DocumentImplTest {
    //all same tests as stage1 - same code also
    @Test
    void equalsAndHashCodeCorrespond() throws URISyntaxException {
        String txt = "Loren Impsum ahduiw ahduif ahcyid zuijedb ijh ghs a o sodoieu";
        URI uri = new URI("www.google.com");
        DocumentImpl doc = new DocumentImpl(uri, txt, null);
        String txt2 = "Loren Impsum ahduiw ahduif ahcyid zuijedb ijh ghs a o sodoieu";
        DocumentImpl doc2 = new DocumentImpl(uri, txt2, null);
        assertTrue(doc.equals(doc2), "expected .equals to return true");
        assertEquals(doc.hashCode(), doc2.hashCode(), ".equals was true, so expected Hashes to be the equal");
        //Non equal doc
        String txt3 = "New random text";
        URI uri3 = new URI("www.yahoo.com");
        DocumentImpl doc3 = new DocumentImpl(uri3, txt3, null);
        assertTrue(!doc.equals(doc3), "expected .equals to fail for non equal docs");
        assertNotEquals(doc.hashCode(), doc3.hashCode(), ".equals was false, so expected Hashes to be not equal");
    }

    @Test
    void testConstructors(){
        String txt = "Random text";
        URI uri = null;
        try {
            uri = new URI("www.google.com");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        String emptyString = "";
        URI emptyUri = URI.create("");
        DocumentImpl doc = new DocumentImpl(uri, txt, null);
        assertNotNull(doc, "expected doc object to be created");
        //all these should throw exceptions
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            DocumentImpl doc2 = new DocumentImpl(null, txt, null);
        }, "IllegalArgumentException was expected");
        URI finalUri = uri;
        IllegalArgumentException thrown1 = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            DocumentImpl doc3 = new DocumentImpl(finalUri, (byte[]) null);
        }, "IllegalArgumentException was expected");
        IllegalArgumentException thrown2 = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            DocumentImpl doc35 = new DocumentImpl( null, (String) null, null);
        }, "IllegalArgumentException was expected");
        URI finalUri1 = uri;
        IllegalArgumentException thrown4 = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            DocumentImpl doc5 = new DocumentImpl(finalUri1, emptyString, null);
        }, "IllegalArgumentException was expected");
        IllegalArgumentException thrown5 = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            DocumentImpl doc6 = new DocumentImpl(null, emptyString, null);
        }, "IllegalArgumentException was expected");
    }

    //stage 3 tests
    @Test
    void testWordCount() throws URISyntaxException {
        String txt = "I Love Hashem i love HASHEM i LovE HaShEm i LOVE HAshEM the the the the";
        URI uri = new URI("www.google.com");
        DocumentImpl doc = new DocumentImpl(uri, txt, null);
        String txt2 = "Yeshiva University is a Yeshiva, it is, It iS";
        DocumentImpl doc2 = new DocumentImpl(uri, txt2, null);
        assertEquals(1, doc.wordCount("I"));
        assertEquals(3, doc.wordCount("i"));
        assertEquals(1, doc.wordCount("Love"));
        assertEquals(1, doc.wordCount("love"));
        assertEquals(1, doc.wordCount("LovE"));
        assertEquals(1, doc.wordCount("LOVE"));
        assertEquals(1, doc.wordCount("Hashem"));
        assertEquals(1, doc.wordCount("HASHEM"));
        assertEquals(1, doc.wordCount("HaShEm"));
        assertEquals(1, doc.wordCount("HAshEM"));
        assertEquals(4, doc.wordCount("the"));
        assertEquals(0, doc2.wordCount("Love"));
        assertEquals(1, doc2.wordCount("Yeshiva"));
        assertEquals(0, doc2.wordCount("yeshiva"));
        assertEquals(1, doc2.wordCount("iS"));
        assertEquals(1, doc2.wordCount("It"));
        assertEquals(1, doc2.wordCount("it"));
    }

    @Test
    void testGetWords() throws URISyntaxException {
        String txt = "I Love Hashem i love HASHEM i LovE HaShEm i LOVE HAshEM the the the the ,,, :L:";
        URI uri = new URI("www.google.com");
        DocumentImpl doc = new DocumentImpl(uri, txt, null);
        Set<String> set = doc.getWords();
        assertTrue(set.contains("I"));
        assertTrue(set.contains("Love"));
        assertTrue(set.contains("Hashem"));
        assertTrue(set.contains("i"));
        assertTrue(set.contains("love"));
        assertTrue(set.contains("HASHEM"));
        assertTrue(set.contains("LovE"));
        assertTrue(set.contains("HaShEm"));
        assertTrue(set.contains("LOVE"));
        assertTrue(set.contains("HAshEM"));
        assertTrue(set.contains("the"));
        assertFalse(set.contains(","));
        assertFalse(set.contains(":"));
    }


    //stage 4 tests
    @Test
    void testUseTime() throws URISyntaxException{
        long time = System.nanoTime();
        String txt = "I Love Hashem i love HASHEM i LovE HaShEm i LOVE HAshEM the the the the ,,, :L:";
        URI uri = new URI("www.google.com");
        DocumentImpl doc = new DocumentImpl(uri, txt, null);
        assertEquals(0, doc.getLastUseTime());
        doc.setLastUseTime(time);
        assertEquals(time, doc.getLastUseTime());
    }

    @Test
    void testCompare() throws URISyntaxException{
        String txt = "I Love Hashem i love HASHEM i LovE HaShEm i LOVE HAshEM the the the the ,,, :L:";
        URI uri = new URI("www.google.com");
        DocumentImpl doc1 = new DocumentImpl(uri, txt, null);
        String txt2 = "Loren Impsum ahduiw ahduif ahcyid zuijedb ijh ghs a o sodoieu";
        URI uri2 = new URI("www.google.com");
        DocumentImpl doc2 = new DocumentImpl(uri2, txt2, null);
        assertEquals(0, doc1.compareTo(doc2));
        doc1.setLastUseTime(System.nanoTime());
        assertEquals(1,  doc1.compareTo(doc2));
        doc2.setLastUseTime(System.nanoTime());
        assertEquals(-1,  doc1.compareTo(doc2));
    }

    //stage5 
    @Test
    void testWordMap() throws URISyntaxException{
        String txt = "I Love Hashem I Love Hashem I Love Hashem  I I Love you you you you you ";
        URI uri = new URI("www.google.com");
        DocumentImpl doc1 = new DocumentImpl(uri, txt, null);
        
        Map<String, Integer> map = doc1.getWordMap();
        //test if it constructs on its own when given null value
        assertEquals(5, map.get("I"));
        assertEquals(3, map.get("Hashem"));
        assertEquals(4, map.get("Love"));
        assertEquals(5, map.get("you"));
        
        String txt2 = "Loren Impsum ahduiw ahduif ahcyid zuijedb ijh ghs a o sodoieu";
        URI uri2 = new URI("www.google.com");
        //pass in map as the wordCount map, even though this map would be different if created on its own
        DocumentImpl doc2 = new DocumentImpl(uri2, txt2, map);

        Map<String, Integer> map2 = doc2.getWordMap();
        //test if it constructs on its own when given null value
        assertEquals(5, map2.get("I"));
        assertEquals(3, map2.get("Hashem"));
        assertEquals(4, map2.get("Love"));
        assertEquals(5, map2.get("you"));
    }

}
