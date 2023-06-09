package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.MinHeap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.Comparable;
import java.util.NoSuchElementException;


public class MinHeapImplTest{

    private class StringTime implements Comparable<StringTime> {
        private String myString;
        private long lastUseTime;
        private StringTime(String s, long l){
            this.myString = s;
            this.lastUseTime = l;
        }
        @Override
        public int compareTo(StringTime str2){
            if(this.lastUseTime > str2.lastUseTime){
                return 1;
            } else if(this.lastUseTime < str2.lastUseTime){
                return -1;
            }
            return 0;
        }
    }



    @Test
    void testGetExpectedIndex(){
        MinHeapImpl<Integer> heap = new MinHeapImpl<>();
        heap.insert(10);
        heap.insert(6);
        heap.insert(100);
        heap.insert(61);
        heap.insert(1);
        heap.insert(18);
        heap.insert(26);
        heap.insert(29);
        assertEquals(1, heap.getArrayIndex(1));
        assertEquals(2, heap.getArrayIndex(6));
        assertEquals(5, heap.getArrayIndex(10));
        assertEquals(3, heap.getArrayIndex(18));
        assertEquals(7, heap.getArrayIndex(26));
        assertEquals(6, heap.getArrayIndex(100));
        assertEquals(8, heap.getArrayIndex(61));
        assertEquals(4, heap.getArrayIndex(29));
        try{
            heap.getArrayIndex(10000000);
            assertTrue(false, "expected exception");
        } catch (NoSuchElementException e){
            assertTrue(true, "exception was thrown");
        }
   }

   @Test
    void testReheapify(){
        MinHeapImpl<StringTime>  heap = new MinHeapImpl<>();
        StringTime s1 = new StringTime("I love Hashem", System.nanoTime());
        StringTime s2 = new StringTime("I love Hashem more", System.nanoTime());
        heap.insert(s1);
        heap.insert(s2);
        StringTime s3 = new StringTime("I love Hashem I love Hashem", System.nanoTime());
        StringTime s4 = new StringTime("I love Hashem more everyday", System.nanoTime());
        heap.insert(s3);
        heap.insert(s4);
       //test for moving up heap
       assertEquals(1, heap.getArrayIndex(s1));
       assertEquals(2, heap.getArrayIndex(s2));
       assertEquals(3, heap.getArrayIndex(s3));
       assertEquals(4, heap.getArrayIndex(s4));
       s1.lastUseTime = System.nanoTime();
       heap.reHeapify(s1);
       assertEquals(4, heap.getArrayIndex(s1));
       assertEquals(1, heap.getArrayIndex(s2));
       // assertEquals(2, heap.getArrayIndex(s3)); - based on heap simulator - shouldnt be there anyway
       assertEquals(2, heap.getArrayIndex(s4));
       assertEquals(3, heap.getArrayIndex(s3));
       s2.lastUseTime = System.nanoTime();
       heap.reHeapify(s2);
       assertEquals(4, heap.getArrayIndex(s1));
       assertEquals(3, heap.getArrayIndex(s2));
       assertEquals(1, heap.getArrayIndex(s3));
       assertEquals(2, heap.getArrayIndex(s4));
       //test for moving down
       s2.lastUseTime = 0;
       heap.reHeapify(s2);
       assertEquals(4, heap.getArrayIndex(s1));
       assertEquals(1, heap.getArrayIndex(s2));
       assertEquals(3, heap.getArrayIndex(s3));
       assertEquals(2, heap.getArrayIndex(s4));
       s1.lastUseTime = 0;
       heap.reHeapify(s1);
       //assertEquals(1, heap.getArrayIndex(s1)); // fails bc s2 also 0;
       //assertEquals(2, heap.getArrayIndex(s2));
       assertEquals(3, heap.getArrayIndex(s3));
       assertEquals(4, heap.getArrayIndex(s4));
       s2.lastUseTime = 1;
       heap.reHeapify(s2);
       assertEquals(1, heap.getArrayIndex(s1));
       assertEquals(2, heap.getArrayIndex(s2));
       assertEquals(3, heap.getArrayIndex(s3));
       assertEquals(4, heap.getArrayIndex(s4));

       //test for saying still
       s4.lastUseTime = System.nanoTime();
       heap.reHeapify(s4);
       assertEquals(1, heap.getArrayIndex(s1));
       assertEquals(2, heap.getArrayIndex(s2));
       assertEquals(3, heap.getArrayIndex(s3));
       assertEquals(4, heap.getArrayIndex(s4));
       heap.reHeapify(s1);
       assertEquals(1, heap.getArrayIndex(s1));
       assertEquals(2, heap.getArrayIndex(s2));
       assertEquals(3, heap.getArrayIndex(s3));
       assertEquals(4, heap.getArrayIndex(s4));
       s3.lastUseTime = System.nanoTime();
       s4.lastUseTime = System.nanoTime();
       heap.reHeapify(s3);
       heap.reHeapify(s4);
       assertEquals(1, heap.getArrayIndex(s1));
       assertEquals(2, heap.getArrayIndex(s2));
       assertEquals(3, heap.getArrayIndex(s3));
       assertEquals(4, heap.getArrayIndex(s4));
   }

        @Test
        public void testReHeapify() {
            MinHeap<Integer> heap = new MinHeapImpl<>();
            heap.insert(5);
            heap.insert(3);
            heap.insert(7);
            heap.reHeapify(3);
            assertEquals((Integer) 3, heap.remove());
            assertEquals((Integer) 5, heap.remove());
            assertEquals((Integer) 7, heap.remove());
        }

        public void testReHeapifyWithInvalidElement() {
            MinHeap<Integer> heap = new MinHeapImpl<>();
            heap.insert(5);
            heap.insert(3);
            heap.insert(7);
            heap.reHeapify(10);
        }

        @Test
        public void testReHeapifyWithDuplicateElements() {
            MinHeap<Integer> heap = new MinHeapImpl<>();
            heap.insert(5);
            heap.insert(3);
            heap.insert(7);
            heap.insert(3);
            heap.reHeapify(3);
            assertEquals((Integer) 3, heap.remove());
            assertEquals((Integer) 3, heap.remove());
            assertEquals((Integer) 5, heap.remove());
            assertEquals((Integer) 7, heap.remove());
        }

        @Test
        public void testReHeapifyWithSingleElement() {
            MinHeap<Integer> heap = new MinHeapImpl<>();
            heap.insert(5);
            heap.reHeapify(5);
            assertEquals((Integer) 5, heap.remove());
        }


    }

