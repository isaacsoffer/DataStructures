package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Set;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;



public class TrieImplTest {
    //mostly it is being tested memaila in DocumentStore
    private Comparator<Integer> lambdaCompare = (Integer a, Integer b) -> {
        if(a > b){
            return 1;
        } else if( a< b){
            return -1;
        }
        return 0;

    };

    private Comparator<String> lambdaCompareStr = (String a, String b) -> {
        return a.compareTo(b);
    };
    @Test
    void testPut(){
        TrieImpl<Integer> trie = new TrieImpl();
        trie.put("twentynine", 29);
        trie.put("twentyeight", 28);
        trie.put("twentyseven", 27);
        trie.put("twentysix", 26);
        trie.put("twentyfive", 25);
        trie.put("twentyfour", 24);
        trie.put("twentythree", 23);
        //sorta also tests getAllWithPrefixSorted
        assertTrue(trie.delete("twentynine", 29) != null);
        assertTrue(trie.delete("twentyeight", 28) != null);
        assertTrue(trie.delete("twentyseven", 27) != null);
        assertTrue(trie.delete("twentysix", 26) != null);
        assertTrue(trie.delete("twentyfive", 25) != null);
        assertTrue(trie.delete("twentyfour", 24) != null);
        assertTrue(trie.delete("twentythree", 23) != null);
        try {
            trie.put("", 1);
            assertTrue(false, "expected to throw exception");
        } catch (IllegalArgumentException e){
            assertTrue(true);
        }
    }

    @Test
    void testGetAllSorted(){
        TrieImpl<Integer> trie = new TrieImpl();
        trie.put("number", 1);
        trie.put("number", 2);
        trie.put("number", 3);
        trie.put("number", 4);
        trie.put("number", 5);
        trie.put("number", 6);
        trie.put("number", 7);
        trie.put("number", 8);
        trie.put("number", 9);
        trie.put("number", 10);
        trie.put("number", 11);
        trie.put("number", 12);
        trie.put("number", 13);
        trie.put("number", 14);
        trie.put("number", 15);
        trie.put("Number", 21);
        trie.put("nUmber", 22);
        trie.put("numbEr", 29);
        trie.put("notpart", 88);
        trie.put("stillno", 77);
        trie.put("almosthadit", 99);
        List<Integer> list = trie.getAllSorted("number", Comparator.naturalOrder());
        assertTrue(list.contains(15));
        assertTrue(list.get(0).equals(1), "failed because returned " + list.get(1));
        assertTrue(list.get(1).equals(2));
        assertTrue(list.get(2).equals(3));
        assertTrue(list.get(3).equals(4));
        assertTrue(list.get(4).equals(5));
        assertTrue(list.get(5).equals(6));
        assertTrue(list.get(6).equals(7));
        assertTrue(list.get(7).equals(8));
        assertTrue(list.get(8).equals(9));
        assertTrue(!list.contains(21));
        assertTrue(!list.contains(22));
        assertTrue(!list.contains(29));
        assertTrue(!list.contains(88));
        assertTrue(!list.contains(77));
        assertTrue(!list.contains(99));
    }

    @Test
    void testGetAllSorted2(){
        TrieImpl<Integer> trie = new TrieImpl();
        trie.put("number", 1);
        trie.put("number", 2);
        trie.put("number", 3);
        trie.put("number", 4);
        trie.put("number", 5);
        trie.put("number", 6);
        trie.put("number", 7);
        trie.put("number", 8);
        trie.put("number", 9);
        trie.put("number", 10);
        trie.put("number", 11);
        trie.put("number", 12);
        trie.put("number", 13);
        trie.put("number", 14);
        trie.put("number", 15);
        trie.put("Number", 21);
        trie.put("nUmber", 22);
        trie.put("numbEr", 29);
        trie.put("notpart", 88);
        trie.put("stillno", 77);
        trie.put("almosthadit", 99);
        Set<Integer> list = trie.deleteAll("number");
        assertTrue(list.contains(15));
        assertTrue(list.contains(1), "failed because returned ");
        assertTrue(list.contains(2));
        assertTrue(list.contains(3));
        assertTrue(list.contains(4));
        assertTrue(list.contains(5));
        assertTrue(list.contains(6));
        assertTrue(list.contains(7));
        assertTrue(list.contains(8));
        assertTrue(list.contains(9));
    }


    @Test
    void testGetAllWithPrefixSorted(){
        TrieImpl<Integer> trie = new TrieImpl();
        trie.put("twentynine", 29);
        trie.put("twentyeight", 28);
        trie.put("twentyseven", 27);
        trie.put("twentysix", 26);
        trie.put("twentyfive", 25);
        trie.put("twentyfour", 24);
        trie.put("twentythree", 23);
        trie.put("twentytwo", 22);
        trie.put("twentyone", 21);
        trie.put("twenty", 20);
        trie.put("nineteen", 19);
        trie.put("eighteen", 18);
        trie.put("seventeen", 17);
        trie.put("sixteen", 16);
        trie.put("fifteen", 15);
        trie.put("fourteen", 14);
        trie.put("thirteen", 13);
        trie.put("twelve", 12);
        trie.put("eleven", 11);
        trie.put("ten", 10);
        trie.put("nine", 9);
        trie.put("eight", 8);
        trie.put("seven", 7);
        trie.put("six", 6);
        trie.put("five", 5);
        trie.put("four", 4);
        trie.put("three", 3);
        trie.put("two", 2);
        trie.put("one", 1);
        List<Integer> list = trie.getAllWithPrefixSorted("twenty", lambdaCompare);
        for(int i = 0; i < list.size() && i < 10; i++) {
            assertTrue(list.get(i).equals(i + 20));
        }
        for(int i = 0; i < list.size() && i < 18; i++){
            assertTrue(!list.contains(i));
        }
    }

    @Test
    void testDeleteWPrefix(){
        TrieImpl<Integer> trie = new TrieImpl();
        trie.put("twentynine", 29);
        trie.put("twentyeight", 28);
        trie.put("twentyseven", 27);
        trie.put("twentysix", 26);
        trie.put("twentyfive", 25);
        trie.put("twentyfour", 24);
        trie.put("twentythree", 23);
        trie.put("twentytwo", 22);
        trie.put("twentyone", 21);
        trie.put("twenty", 20);
        trie.put("nineteen", 19);
        trie.put("eighteen", 18);
        trie.put("seventeen", 17);
        trie.put("sixteen", 16);
        trie.put("fifteen", 15);
        trie.put("fourteen", 14);
        trie.put("thirteen", 13);
        trie.put("twelve", 12);
        trie.put("eleven", 11);
        trie.put("ten", 10);
        trie.put("nine", 9);
        trie.put("eight", 8);
        trie.put("seven", 7);
        trie.put("six", 6);
        trie.put("five", 5);
        trie.put("four", 4);
        trie.put("three", 3);
        trie.put("two", 2);
        trie.put("one", 1);
        Set<Integer> set = trie.deleteAllWithPrefix("twenty");
        assertTrue(trie.getAllWithPrefixSorted("twenty", lambdaCompare).isEmpty());
        assertTrue(trie.getAllSorted("twentynine", lambdaCompare).isEmpty());
        assertTrue(trie.delete("twentynine", 29) == null);
        assertTrue(set.contains(29) , "it was removed from trie, but not returned from deletealLWprefix");
        assertTrue(set.contains(28));
        assertTrue(set.contains(27));
        assertTrue(set.contains(26));
        assertTrue(set.contains(25));
        assertTrue(set.contains(24));
        assertTrue(set.contains(23));
        assertTrue(set.contains(22));
        assertTrue(set.contains(21));
        assertTrue(set.contains(20));
        assertTrue(trie.getAllWithPrefixSorted("twenty", lambdaCompare).isEmpty());
    }

    void testDeleteWPrefixString() {
        TrieImpl<String> trie = new TrieImpl();
        trie.put("twentynine", "twentynine");
        trie.put("twentyeight", "twentyeight");
        trie.put("twentyseven", "twentyseven");
        trie.put("twentysix", "twentysix");
        trie.put("twentyfive", "twentyfive");
        trie.put("twentyfour", "twentyfour");
        trie.put("twentythree", "twentythree");
        trie.put("twentytwo", "twentytwo");
        trie.put("twentyone", "twentyone");
        trie.put("twenty", "twenty");
        trie.put("nineteen", "nineteen");
        trie.put("eighteen", "eighteen");
        trie.put("seventeen", "seventeen");
        Set<String> set = trie.deleteAllWithPrefix("twenty");
        assertTrue(trie.getAllWithPrefixSorted("twenty", lambdaCompareStr).isEmpty());
        assertTrue(trie.delete("twentynine", "twentynine") == null);
        assertTrue(trie.delete("twentyeight", "twentyeight") == null);
        assertTrue(trie.delete("twentyseven", "twentyseven") == null);
        assertTrue(trie.delete("twentythree", "twentythree") == null);
        assertTrue(trie.delete("twentyone", "twentyone") == null);
        assertTrue(trie.delete("twenty", "twenty") == null);

    }

    @Test
    void TestAboveAgain(){
        TrieImpl<Integer> trie = new TrieImpl();
        trie.put("number", 1);
        trie.put("number", 2);
        trie.put("number", 3);
        trie.put("number", 4);
        trie.put("number", 5);
        trie.put("number", 6);
        trie.put("number", 7);
        trie.put("number", 8);
        trie.put("number", 9);
        trie.put("number", 10);
        trie.put("number", 11);
        trie.put("number", 12);
        trie.put("number", 13);
        trie.put("number", 14);
        trie.put("number", 15);
        trie.put("Number", 21);
        trie.put("nUmber", 22);
        trie.put("numbEr", 29);
        trie.put("notpart", 88);
        trie.put("stillno", 77);
        trie.put("almosthadit", 99);
        Set<Integer> set = trie.deleteAllWithPrefix("numbe");
        assertTrue(trie.getAllWithPrefixSorted("numbe", lambdaCompare).isEmpty());
        assertTrue(trie.getAllSorted("number", lambdaCompare).isEmpty() );
        assertTrue(trie.delete("number", 1) == null);
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        assertTrue(set.contains(3));
        assertTrue(set.contains(4));
        assertTrue(set.contains(5));
        assertTrue(set.contains(6));
        assertTrue(set.contains(7));
        assertTrue(set.contains(8));
        assertTrue(set.contains(9));
        assertTrue(set.contains(10));
        assertTrue(set.contains(11));
        assertTrue(set.contains(12));
        assertTrue(set.contains(13));

    }

    @Test
    void testDeleteAll() {
        TrieImpl<Integer> trie = new TrieImpl();
        trie.put("number", 1);
        trie.put("number", 2);
        trie.put("number", 3);
        trie.put("number", 4);
        trie.put("number", 5);
        trie.put("number", 6);
        trie.put("number", 7);
        trie.put("number", 8);
        trie.put("number", 9);
        trie.put("number", 10);
        trie.put("number", 11);
        trie.put("number", 12);
        trie.put("number", 13);
        trie.put("number", 14);
        trie.put("number", 15);
        trie.put("Number", 21);
        trie.put("nUmber", 22);
        trie.put("numbEr", 29);
        trie.put("notpart", 88);
        trie.put("stillno", 77);
        trie.put("almosthadit", 99);
        Set<Integer> set = trie.deleteAll("number");
        List<Integer> list = trie.getAllSorted("number", lambdaCompare);
        for(int i = 14; i >= 1; i--) {
            assertTrue(set.contains(i) , "failed for i = " + i);
            assertTrue(!list.contains(i));
        }
    }

    @Test
    void testDelete(){
        TrieImpl<Integer> trie = new TrieImpl();
        trie.put("number", 1);
        trie.put("number", 2);
        trie.put("number", 3);
        trie.put("number", 4);
        trie.put("number", 5);
        assertTrue(!trie.getAllSorted("number", lambdaCompare).isEmpty());
        trie.delete("number", 1);
        trie.delete("number", 2);
        trie.delete("number", 3);
        trie.delete("number", 4);
        trie.delete("number", 5);
        assertTrue(trie.getAllSorted("number", lambdaCompare).isEmpty());
    }

    @Test
    void testDeleteAgain(){
        TrieImpl<Integer> trie = new TrieImpl();
        trie.put("number", 1);
        trie.put("number", 2);
        trie.put("number", 3);
        trie.put("number", 4);
        trie.put("number", 5);
        assertTrue(!trie.getAllSorted("number", lambdaCompare).isEmpty());
        trie.delete("number", 1);
        assertTrue(!trie.getAllSorted("number", lambdaCompare).contains(1));
        trie.delete("number", 3);
        assertTrue(!trie.getAllSorted("number", lambdaCompare).contains(3));
        trie.delete("number", 2);
        assertTrue(!trie.getAllSorted("number", lambdaCompare).contains(2));
        trie.delete("number", 4);
        assertTrue(!trie.getAllSorted("number", lambdaCompare).contains(4));
    }

       private Trie<String> trie;

        @BeforeEach
        public void setUp() {
            trie = new TrieImpl<>();
        }

        @Test
        public void testPutAndGetAllSorted() {
            trie.put("hello", "world");
            trie.put("hello", "there");
            trie.put("goodbye", "world");
            trie.put("goodbye", "everyone");

            List<String> expected = new ArrayList<>();
            expected.add("there");
            expected.add("world");
            Collections.sort(expected, Collections.reverseOrder());

            List<String> actual = trie.getAllSorted("hello", Comparator.reverseOrder());
            assertEquals(expected, actual);

            List<String> n = new ArrayList<>();
            n.add("everyone");
            n.add("world");
            Collections.sort(n, Collections.reverseOrder());
            actual = trie.getAllSorted("goodbye", Comparator.reverseOrder());
            assertEquals(n, actual);
        }

        @Test
        public void testGetAllWithPrefixSorted1() {
            trie.put("hello", "world");
            trie.put("help", "me");
            trie.put("goodbye", "world");
            trie.put("goodbye", "everyone");

            List<String> expected = new ArrayList<>();
            expected.add("me");
            expected.add("world");
            Collections.sort(expected, Collections.reverseOrder());

            List<String> actual = trie.getAllWithPrefixSorted("he", Comparator.reverseOrder());
            assertEquals(expected, actual);
        }

        @Test
        public void testDeleteAllWithPrefix() {
            trie.put("hello", "world");
            trie.put("help", "me");
            trie.put("goodbye", "world");
            trie.put("goodbye", "everyone");

            Set<String> deleted = trie.deleteAllWithPrefix("he");
            assertEquals(2, deleted.size());
            assertTrue(deleted.contains("world"));
            assertTrue(deleted.contains("me"));

            List<String> remaining = trie.getAllWithPrefixSorted("he", Comparator.naturalOrder());
            assertTrue(remaining.isEmpty(), "expected empty, but was " + remaining);
        }

        @Test
        public void testDeleteAll1() {
            trie.put("hello", "world");
            trie.put("hello", "there");
            trie.put("goodbye", "world");
            trie.put("goodbye", "everyone");

            Set<String> deleted = trie.deleteAll("hello");
            assertEquals(2, deleted.size());
            assertTrue(deleted.contains("world"));
            assertTrue(deleted.contains("there"));

            List<String> remaining = trie.getAllSorted("hello", Comparator.naturalOrder());
            assertTrue(remaining.isEmpty());
        }

        @Test
        public void testDelete1() {
            trie.put("hello", "world");
            trie.put("hello", "there");
            trie.put("goodbye", "world");
            trie.put("goodbye", "everyone");

            String deleted = trie.delete("hello", "world");
            assertEquals("world", deleted);

            List<String> remaining = trie.getAllSorted("hello", Comparator.naturalOrder());
            assertEquals(1, remaining.size());
            assertEquals("there", remaining.get(0));
        }
    }


