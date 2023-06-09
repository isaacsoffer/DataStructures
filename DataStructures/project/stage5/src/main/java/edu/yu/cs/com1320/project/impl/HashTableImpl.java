package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.*;
//stage 2

/**
 * Instances of HashTable should be constructed with two type parameters, one for the type of the keys in the table and one for the type of the values
 * @param <Key>
 * @param <Value>
 */
public class HashTableImpl<Key,Value> implements HashTable<Key,Value> {

    class Entry<Key, Value>{
        Key key;
        Value value;
        Entry nextItem;
        Entry(Key k, Value v){
            if(k == null){
                throw new IllegalArgumentException();
            }   
            key = k;
            value = v;
            nextItem = null;
        }
    }

    private Entry[] table;
    private int enteredKeysCount;

    public HashTableImpl() {
        table = new Entry[5];
        enteredKeysCount = 0;
    }

    /**
     * @param k the key whose value should be returned
     * @return the value that is stored in the HashTable for k, or null if there is no such key in the table
     */

    public Value get(Key k){
        if(k == null){
            throw new IllegalArgumentException("key cannot be null");
        }
        int expectedIndex = getExpectedArraySpot(k);
        Entry current = table[expectedIndex];
        if(current == null){
            return null;
        }
        if(current.key.equals(k)){
            return (Value)current.value;
        }
        while(current.nextItem != null && !current.nextItem.key.equals(k)){
            current = current.nextItem;
        }
        //i.e we found the key
        if(current.nextItem != null){
            assert(current.nextItem.key.equals(k));
            return (Value)current.nextItem.value;
        }
        //no such k
        return null;
    }

    /**
     * @param k the key at which to store the value
     * @param v the value to store.
     * To delete an entry, put a null value.
     * @return if the key was already present in the HashTable, return the previous value stored for the key. If the key was not already present, return null.
     */
    
    public Value put(Key k, Value v){
        if(k == null){
            throw new IllegalArgumentException("key cannot be null");
        }
        int expectedIndex = getExpectedArraySpot(k);
        Entry addOn = new Entry(k,v);
        if(v == null){
            Value temp = null;
            if(containsKey(k)){
                temp = get(k);
            }
            deleteEntry(k);
            enteredKeysCount--; //here don't have to worry about array doubling bc deletin
            return temp;
        }
        if(containsKey(k)){ // MAY HAVE TO DO TRY-CATCH HERE CHECK PIAZZA
            Value previous = get(k);
            deleteEntry(k);
            addEntry(expectedIndex, addOn);
            enteredKeysCount++;
            checkArraySize();
            return previous;
        } else{
            addEntry(expectedIndex, addOn);
            enteredKeysCount++;
        }
        checkArraySize();
        return null;
    }

    /**
     * @param key the key whose presence in the hashtabe we are inquiring about
     * @return true if the given key is present in the hashtable as a key, false if not
     * @throws NullPointerException if the specified key is null
     */

    
    public boolean containsKey(Key key) {
        if(key == null){
            throw new NullPointerException();
        }
        int expectedIndex = getExpectedArraySpot(key);
        Entry current = table[expectedIndex];
        if(current == null){
            return false;
        }
        if(current.key.equals(key)){
            return true;
        }
        while(current.nextItem != null && !current.nextItem.key.equals(key)){
            current = current.nextItem;
        }
        // means that current.nextItem is the key
        if(current.nextItem != null){
            assert(current.nextItem.key.equals(key));
            return true;
        }
        //end of list
        assert(current.nextItem == null);
        return false;
    }

    

    //added methods
    private int getExpectedArraySpot(Key k){
        return (k.hashCode() & 0x7fffffff) % this.table.length;
    }

    private int getExpectedArraySpot(Key k, int tableSize){
        return (k.hashCode() & 0x7fffffff) % tableSize;
    }

    private void addEntry(int expectedIndex, Entry addOn){
        Entry current = table[expectedIndex];
        if(current == null){
                table[expectedIndex] = addOn;
            } else{
                while(current.nextItem != null && !current.nextItem.key.equals(addOn.key)){
                    current = current.nextItem;
                }
                // means that current.nextItem is the key
                if(current.nextItem != null){ 
                    current.nextItem = addOn;
                    return;
                }
                //end of list
                if(current.nextItem == null){ 
                    current.nextItem = addOn;
                }
                return;
            }
    }
    private void addEntry(int expectedIndex, Entry addOn, Entry[] arrayToAddTo){
        Entry current = arrayToAddTo[expectedIndex];
        if(current == null){
            arrayToAddTo[expectedIndex] = addOn;
        } else{
            while(current.nextItem != null && !current.nextItem.key.equals(addOn.key)){
                current = current.nextItem;
            }
            // means that current.nextItem is the key
            if(current.nextItem != null){
                current.nextItem = addOn;
                return;
            }
            //end of list
            if(current.nextItem == null){
                current.nextItem = addOn;
            }
            return;
        }
    }

    private void deleteEntry(Key k) {
        int expectedIndex = getExpectedArraySpot(k);
        if(table[expectedIndex] == null){
            //already not in the array
            return;
        }
        Entry pointer = table[expectedIndex];
        Entry possibleCorrectValue = pointer;
        if(possibleCorrectValue.key.equals(k)){ //not sure if this is needed
            table[expectedIndex] = pointer.nextItem;
            return;
        }
        Entry current = pointer;
        while(current.nextItem != null && !current.nextItem.key.equals(k)){ //test if this also works for the first item - don't think so
            current = current.nextItem;
        }
        if(current.nextItem != null){
            current.nextItem = current.nextItem.nextItem;
            return;
        }
        return;
    }  

    private Entry[] doubleArray(Entry[] array){
        Entry[] doubledArray = new Entry[array.length * 2];
        for(int i = 0; i < array.length; i++){
        //cycle through all items in list stored at this spot in array and rehash with new array size
            Entry temp = array[i];
            while(temp != null){
                int expectedIndexOfTemp = getExpectedArraySpot((Key) temp.key, doubledArray.length);
                addEntry(expectedIndexOfTemp, temp, doubledArray);
                temp = temp.nextItem;
            }
        }
        return doubledArray;
    }

    private void checkArraySize(){
        if(!(table.length >= enteredKeysCount/4)){
            table = doubleArray(table);
        }
    }
}

