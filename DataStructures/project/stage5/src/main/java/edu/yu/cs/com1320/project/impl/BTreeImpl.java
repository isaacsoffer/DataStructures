package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.BTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import java.nio.file.Path; 
import java.util.NoSuchElementException;

public class BTreeImpl<Key extends Comparable<Key>, Value> implements BTree<Key, Value> {
    private static final int MAX = 6;
    private Node root; //root of the B-tree
    //private Node leftMostExternalNode;
    private int height; //height of the B-tree
    private int n; //number of key-value pairs in the B-tree
    private PersistenceManager<Key,Value> persistenceManager;

    private static final class Node{
        private int entryCount; // number of entries
        private Entry[] entries = new Entry[BTreeImpl.MAX]; // the array of children
        private Node next;
        private Node previous;
        // create a node with k entries
        private Node(int k)
        {
            this.entryCount = k;
        }
        private void setNext(Node next)
        {
            this.next = next;
        }
        private Node getNext()
        {
            return this.next;
        }
        private void setPrevious(Node previous)
        {
            this.previous = previous;
        }
    }

    //internal nodes: only use key and child
    //external nodes: only use key and value
    private static class Entry<Value> {
        private Comparable key;
        private Value val;
        private boolean isOnDisk;
        private Node child;
        private Entry(Comparable key, Value val, Node child) {
            this.key = key;
            this.val = val;
            this.child = child;
            this.isOnDisk = false;
        }
        private Value getValue(){
            return this.val;
        }
    }

    public BTreeImpl(){
        this.root = new Node(0);
        //add sentinal has to be done when initializing - gotta be a way how to do now, but idk
        //this.leftMostExternalNode = this.root;
    }

    // private Key getMinKey() {
    //     key.MIN_VALUE = *;
    //     if(Key.MIN_VALUE == null){
    //     try {
    //         Key minKey = (Key) Comparable.class.getDeclaredField("MIN_VALUE").get(null);
    //         return minKey;
    //     } catch (IllegalAccessException | NoSuchFieldException e) {
    //         throw new RuntimeException("Failed to get the minimum key value.", e);
    //     }
    // }
    // }


    public Value get(Key k){ //done
        if (k == null) {
            throw new IllegalArgumentException("argument to get() is null");
        }
        //returns a leaf node
        Entry entry = this.get(this.root, k, this.height);
        //I think you need to test for what type of value it is here
        //return (Value)entry.val;
        if(entry == null) {
            //System.out.println("Entry was not found for class/type " + k.getClass() + " = " + k.toString());
            return null; // didn't find the key
        }
        if(!entry.isOnDisk){ 
            //(Key auto exists- see 2 lines up)
            //value is either there or null
            //System.out.println("landed in the if that should only be if item is not on disk");
            return (Value) entry.getValue();
        } else{
            //ie it is written to disk
            try{  
                //System.out.println("try started, about to call deserailize in BTReeimpl");
                Value v = this.persistenceManager.deserialize(k);
                entry.isOnDisk = false;
                //System.out.println("v is = " + v.toString());
                return v;
            } catch(IOException e){
                //e.printStackTrace();
                return null; //piazza - if no exception declarted in method header, don't throw any others, just return null
            }
        }
    }

    private Entry get(Node currentNode, Key key, int height) {
        Entry[] entries = currentNode.entries;

        //current node is external (i.e. height == 0)
        if (height == 0)
        {
            for (int j = 0; j < currentNode.entryCount; j++)
            {
                if(isEqual(key, entries[j].key))
                {
                    //found desired key. Return its value
                    return entries[j];
                }
            }
            //didn't find the key
            return null;
        }

        //current node is internal (height > 0)
        else
        {
            for (int j = 0; j < currentNode.entryCount; j++)
            {
                //if (we are at the last key in this node OR the key we
                //are looking for is less than the next key, i.e. the
                //desired key must be in the subtree below the current entry),
                //then recurse into the current entry’s child
                if (j + 1 == currentNode.entryCount || less(key, entries[j + 1].key))
                {
                    return this.get(entries[j].child, key, height - 1);
                }
            }
            //didn't find the key
            return null;
        }
    }
    public Value put(Key k, Value v){
        Value valToDelete = null;
        if (k == null){
            throw new IllegalArgumentException("argument key to put() is null");
        }
        //if the key already exists in the b-tree, simply replace the value
        Entry alreadyThere = this.get(this.root, k, this.height);
        if(alreadyThere != null) {
            if(!alreadyThere.isOnDisk){
                valToDelete = (Value) alreadyThere.val;
            } else {
                try{ //inefficient, but how else can I get valToDelete and call delete - nvm this is good - it itself calls delete
                    valToDelete = (Value) this.persistenceManager.deserialize(k);
                } catch(IOException e) {
                    valToDelete = null; //effectively returning null
                }
            }
            //leave it as stored here unless moved to disk 
            alreadyThere.val = v;
            alreadyThere.isOnDisk = false;
        } //if not alreAady there, ValToelete is null
        Node newNode = this.put(this.root, k, v, this.height);
        this.n++;
        if (newNode != null) {
            //split the root:
            //Create a new node to be the root.
            //Set the old root to be new root's first entry.
            //Set the node returned from the call to put to be new root's second entry
            Node newRoot = new Node(2);
            newRoot.entries[0] = new Entry<Value>(this.root.entries[0].key, null, this.root);
            newRoot.entries[1] = new Entry<Value>(newNode.entries[0].key, null, newNode);
            this.root = newRoot;
            //a split at the root always increases the tree height by 1
            this.height++;
        }
        return valToDelete;
    }

    private Node put(Node currentNode, Key key, Value val, int height) {
        int j;
        Entry newEntry = new Entry<Value>(key, val, null);

        //external node
        if (height == 0) {
            //find index in currentNode’s entry[] to insert new entry
            //we look for key < entry.key since we want to leave j
            //pointing to the slot to insert the new entry, hence we want to find
            //the first entry in the current node that key is LESS THAN
            for (j = 0; j < currentNode.entryCount; j++) {
                if (less(key, currentNode.entries[j].key)) {
                    break;
                }
            }
        }

        // internal node
        else {
            //find index in node entry array to insert the new entry
            for (j = 0; j < currentNode.entryCount; j++) {
                //if (we are at the last key in this node OR the key we
                //are looking for is less than the next key, i.e. the
                //desired key must be added to the subtree below the current entry),
                //then do a recursive call to put on the current entry’s child
                if ((j + 1 == currentNode.entryCount) || less(key, currentNode.entries[j + 1].key)) {
                    //increment j (j++) after the call so that a new entry created by a split
                    //will be inserted in the next slot
                    Node newNode = this.put(currentNode.entries[j++].child, key, val, height - 1);
                    if (newNode == null) {
                        return null;
                    }
                    //if the call to put returned a node, it means I need to add a new entry to
                    //the current node
                    newEntry.key = newNode.entries[0].key;
                    newEntry.val = null;
                    newEntry.child = newNode;
                    break;
                }
            }
        }
        //shift entries over one place to make room for new entry
        for (int i = currentNode.entryCount; i > j; i--) {
            currentNode.entries[i] = currentNode.entries[i - 1];
        }
        //add new entry
        currentNode.entries[j] = newEntry;
        currentNode.entryCount++;
        if (currentNode.entryCount < BTreeImpl.MAX) {
            //no structural changes needed in the tree
            //so just return null
            return null;
        }
        else {
            //will have to create new entry in the parent due
            //to the split, so return the new node, which is
            //the node for which the new entry will be created
            return this.split(currentNode, height);
        }
    }


    public void moveToDisk(Key k) throws Exception {
        if(k == null){
            throw new IllegalArgumentException("Please pass in a valid key");
        }
        if(this.persistenceManager == null){
            throw new IllegalStateException("Move to disk can not be called until a persistance manager is set");
        }
        Entry entry = this.get(this.root, k, this.height);
        if(entry.isOnDisk){
            return; 
            //"the given key is already on disk
        }
        Value val = this.get(k);
        if(val == null){
            throw new NoSuchElementException("No value present for given key");
        }
        this.persistenceManager.serialize(k, val); //throws IOExceptiom
        //now, the key is serialized, ie its a null value, so to access the value it will go to disk
        put(k, null);
        entry.isOnDisk = true;
    }

    public void setPersistenceManager(PersistenceManager<Key,Value> pm){
        this.persistenceManager = pm;
    }



    //util functions
    private Node split(Node currentNode, int height)
    {
        Node newNode = new Node(BTreeImpl.MAX / 2);
        //by changing currentNode.entryCount, we will treat any value
        //at index higher than the new currentNode.entryCount as if
        //it doesn't exist
        currentNode.entryCount = BTreeImpl.MAX / 2;
        //copy top half of h into t
        for (int j = 0; j < BTreeImpl.MAX / 2; j++)
        {
            newNode.entries[j] = currentNode.entries[BTreeImpl.MAX / 2 + j];
        }
        //external node
        if (height == 0)
        {
            newNode.setNext(currentNode.getNext());
            newNode.setPrevious(currentNode);
            currentNode.setNext(newNode);
        }
        return newNode;
    }

    // comparison functions - make Comparable instead of Key to avoid casts
    private static boolean less(Comparable k1, Comparable k2)
    {
        return k1.compareTo(k2) < 0;
    }

    private static boolean isEqual(Comparable k1, Comparable k2)
    {
        return k1.compareTo(k2) == 0;
    }
}