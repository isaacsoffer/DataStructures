package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.Stack;

public class StackImpl<T> implements Stack<T> {
	
	class ListEntry<T>{
        T item;
        ListEntry next;
        ListEntry(){
           item = null;
           next = null;
        }
        ListEntry(T addedItem){
        	this.item = addedItem;
          	next = null;
        }
    }

    private ListEntry head;
    private int size;
    
    public StackImpl(){
    	head = new ListEntry();
    	size = 0;
    }

/**
     * @param element object to add to the Stack
     */
    @Override
    public void push(T element){
    	if(element == null){
    		throw new IllegalArgumentException("please pass a valid element");
    	}
    	ListEntry enteredElement = new ListEntry(element);
    	/*if(head.next == null){ //DONT NEED IF ELSE - IF HEAD.NEXT IS NULL, FIRST LINE IN ELSE IS NULL WHETHER IF OR ELSE RUNS
    		head.next = enteredElement;
    	} else {
    		enteredElement.next = head.next
    		head.next = enteredElement; 
    	}*/
    	enteredElement.next = head.next; //if head.next is null, still fine
    	head.next = enteredElement;
    	size++;
    	return;
    }

    /**
     * removes and returns element at the top of the stack
     * @return element at the top of the stack, null if the stack is empty
     */
    @Override
    public T pop(){
    	if(head.next == null){ //need this - bc if not temp.item will throw nullpointer
    		return null;
    	}
    	ListEntry temp = head.next;
    	head.next = head.next.next;
    	size--;
    	return (T) temp.item;
    }

    /**
     *
     * @return the element at the top of the stack without removing it
     */
    @Override
    public T peek(){
        if(head.next == null){ //need this - bc if not temp.item will throw nullpointer
            return null;
        }
        return (T) head.next.item;
    }

    /**
     *
     * @return how many elements are currently in the stack
     */
    @Override
    public int size(){
    	return size;
    }
}
