package edu.yu.cs.com1320.project.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.lang.Character;
import edu.yu.cs.com1320.project.Trie;
import java.util.Arrays;
import java.lang.String;

public class TrieImpl<Value> implements Trie<Value> {
    private static final int alphabetSize = 256; // extended ASCII
    private Node root; // root of trie
    private Set<Value> tempSetValues;

    private static class Node<Value> {
        private ArrayList<Value> values = new ArrayList();
        private Node[] links = new Node[TrieImpl.alphabetSize];
        private int letterCount = 0;
    }

    public TrieImpl(){
        root = new Node<Value>();
    }

    public List<Value> getAllSorted(String key, Comparator<Value> comparator) {
        if (key == null || comparator == null || key.isEmpty()) {
            throw new IllegalArgumentException("please pass in valid parameters");
        }
        Node x = this.get(this.root, key, 0);
        List<Value> list = new ArrayList<>();
        if (x == null || x.values == null || x.values.isEmpty()) {
            assert(list.isEmpty());
            return list;
        }
        list = x.values;
        list.sort(comparator);
        return list;
    }
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator){
        if (prefix == null || prefix.isEmpty() || comparator == null) {
            throw new IllegalArgumentException("Invalid prefix or comparator");
        }
        Node<Value> node = get(this.root, prefix, 0);
        List<Value> values = new ArrayList<>();
        recursiveGetAllWithPrefix(node, values);
        values.sort(comparator/*.reversed()*/);
        return values;
    }

    private void recursiveGetAllWithPrefix(Node<Value> node, List<Value> values) {
        if (node == null) {
            return;
        }
        if (!node.values.isEmpty()) {
            values.addAll(node.values);
        }
        for (int i = 0; i < alphabetSize; i++) {
            recursiveGetAllWithPrefix(node.links[i], values);
        }
    }

    private Node get(Node x, String key, int d) {
        //link was null - return null, indicating a miss
        if (x == null)
        {
            return null;
        }
        //we've reached the last node in the key,
        //return the node
        if (d == key.length())
        {
            return x;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);
        return this.get(x.links[c], key, d + 1);
    }

    public void put(String key, Value val){
        if(key == null || key.isEmpty() ) {
            throw new IllegalArgumentException("please pass in a valid key");
        }
        String tester = key.replaceAll("[^a-zA-Z0-9]", "");
        if(!key.equals(tester)){
            throw new IllegalArgumentException("please pass in a valid key");
        }
        if (val != null) {
            this.root = put(this.root, key, val, 0);
        }
    }

    private Node put(Node x, String key, Value val, int d) {
        //create a new node
        if (x == null)
        {
            x = new Node();
        }
        //we've reached the last node in the key,
        //set the value for the key and return the node
        if (d == key.length())
        {
            x.values.add(val);
            return x;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);
        x.links[c] = this.put(x.links[c], key, val, d + 1);
        x.letterCount++;
        return x;
    }

    public Set<Value> deleteAll(String key){
        if(key == null || key.isEmpty()) {
            throw new IllegalArgumentException("please pass in a valid key to deleteAll");
        }
        this.root = deleteAll(this.root, key, 0);
        return this.tempSetValues;
    }

    public Set<Value> deleteAllWithPrefix(String prefix){
        if(prefix == null || prefix.isEmpty()) {
            throw new IllegalArgumentException("please pass in a valid key to deleteAllWithPrefix");
        }
        Node x = this.get(this.root, prefix, 0);
        Set<Value> set = new HashSet<Value>();
        if(x == null){
            return set; // there is no node for this prefix, nothing to delete
        }
        deleteAllWithPrefix(x, set);
        deleteSubtreeIfNoValues(x);
        Node t = this.get(this.root, prefix.substring(0, prefix.length()-2), 0);
        deleteSubtreeIfNoValues(t); //check if parent node of prefix should be deleted
        return set;
    }

    public Value delete(String key, Value val){
        if(key == null || key.isEmpty()) {
            throw new IllegalArgumentException("please pass in a valid key to delete");
        }
        Node x = this.get(this.root, key, 0);
        if(x == null || x.values == null || x.values.isEmpty()){
            return null;
        }
        if(x.values.contains(val)){
            x.values.remove(val);
            deleteSubtreeIfNoValues(x);
            return val;
        }
        return null;
    }


    private boolean deleteSubtreeIfNoValues(Node node) {
        if (node == null) {
            return false;
        }
        boolean canDelete = true;
        // Recursively check if all child nodes have no values
        for (int i = 0; i < node.links.length; i++) {
            if (node.links[i] != null) {
                if (!deleteSubtreeIfNoValues(node.links[i])) {
                    canDelete = false;
                }
            }
        }
        // If there are no values stored in the current node and all its child nodes,
        // delete the subtree rooted at this node
        if (node.values.isEmpty() && canDelete) {
            node.links = new Node[TrieImpl.alphabetSize];
            return true;
        }

        return false;
    }


    private void deleteAllWithPrefix(Node node, Set<Value> deletedValues) {
        for (int i = 0; i < node.links.length; i++) {
            if(node.links[i] != null){
                deleteAllWithPrefix(node.links[i], deletedValues);
            }
        }
        if (node.values != null) {
            deletedValues.addAll(node.values);
            node.letterCount = 0;
            node.values = new ArrayList<>();
            node.links = new Node[TrieImpl.alphabetSize];
        }
    }

    private Node deleteAll(Node x, String key, int d) {
        if (x == null) {
            return null;
        }
        //we're at the node to del - set the val to null
        if (d == key.length()) {
            this.tempSetValues = new HashSet(x.values);
            x.values = new ArrayList();
        }
        //continue down the trie to the target node
        else {
            char c = key.charAt(d);
            x.links[c] = this.deleteAll(x.links[c], key, d + 1);
        }
        //this node has a val – do nothing, return the node
        if (!x.values.isEmpty()) {
            return x;
        }
        //remove subtrie rooted at x if it is completely empty
        for (int c = 0; c <TrieImpl.alphabetSize; c++) {
            if (x.links[c] != null) {
                return x; //not empty
            }
        }
        //empty - set this link to null in the parent
        return null;
    }





}