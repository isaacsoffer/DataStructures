package edu.yu.cs.com1320.project.impl;
import  edu.yu.cs.com1320.project.*;

import java.util.NoSuchElementException;

/**
 * Beginnings of a MinHeap, for Stage 4 of project. Does not include the additional data structure or logic needed to reheapify an element after its last use time changes.
 * @param <E>
 */
public class MinHeapImpl<E extends Comparable<E>>  extends MinHeap<E /*extends Comparable<E>*/>{
    public MinHeapImpl() {
        this.elements = (E[]) new Comparable[2]; //don't use index 0, so start at 1
    }

    @Override
    public void reHeapify(E element){
        if(element == null){
            throw new NoSuchElementException("please pass in a valid element");
        }
        //these methods themselves test if any change is needed, and work until it is fixed
        this.upHeap(getArrayIndex(element));
        this.downHeap(getArrayIndex(element));
    }
    @Override
    protected int getArrayIndex(E element){
        if(element == null){
            throw new NoSuchElementException("please pass in a valid element");
        }
        for(int i = 1; i < this.elements.length; i++){ //index 0 is empty
            if(this.elements[i] != null){
                if(this.elements[i].equals(element)){
                    return i;
                }
            }
        }
        //if reached here it is not in the array
        throw new NoSuchElementException("please pass in an element that is in the heap");

        //tried to do it more efficiently, didn't work
        /*int k = 1;
        E tester = this.elements[k];
        while(tester.compareTo(element) >= 1){
            //identify which of the 2 children are less
            int j = 2 * k;
            if (j < this.count && this.isGreater(j, j + 1)){
                j++;
            }
            k = j;
            tester = this.elements[k];
        }
        //now tester is either equal to or greater than element
        if(tester.compareTo(element) == 0){
            return k;
        } else if(tester.compareTo(element) < 0){ 
            //tester is greater than element, but the parent node is less (the while loop mustve run for it)
            //therefore, the node's sibling is the correct index
            double parent = k/2;
            int parentInt = k/2;
            if(parent - parentInt == 0){
                //ie no decimal, so the child was a left child, and sibling to return is right child
                if(elements[(2 * parentInt) + 1] != null){
                    return (2 * parentInt) + 1;
                }
            } else if(elements[(2 * parentInt)] != null){ //child was right, so return left child
                return 2* parentInt;
            }
        }
        throw new NoSuchElementException();*/
    }
    @Override
    protected void doubleArraySize(){
        E[] doubled = (E[]) new Comparable[2 * this.elements.length];
        for(int i = 0; i < this.elements.length; i++){
            doubled[i] = this.elements[i];
        }
        this.elements = doubled;
    }
}