package edu.yu.cs.com1320.project.stage5.impl;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashMap;
import java.util.function.Function;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Scanner;
import edu.yu.cs.com1320.project.stage5.*;
import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.*;
import java.io.File; 


public class DocumentStoreImpl implements DocumentStore {

    private BTree<URI, Document> bTree;
    private StackImpl<Undoable> commandStack;
    private HashMap<URI, Boolean> inHeap;
    private HashMap<URI,StackImpl<Document>> lastDocDeletedTable;
    private HashMap<URI,StackImpl<Document>> lastDocOverWrittenTable;
    private Function<URI,Boolean> lambdaDelete;
    private Function<URI,Boolean> lambdaPutNew;
    private Function<URI,Boolean> lambdaPutExisting;
    private TrieImpl<URI> trie;
    private int maxDocBytes;
    private int maxDocCount;
    private int actualDocCount, actualDocBytes;
    private MinHeapImpl<MinHeapNode> heap;
    private int heapCount;

    private class DocumentComparator implements Comparator<URI> {
        private String word;
        private boolean isPrefixOnly;

        private DocumentComparator(String str, boolean isPrefix) {
            this.word = str;
            this.isPrefixOnly = isPrefix;
        }
        @Override
        public int compare(URI uri1, URI uri2) {
            if (!isPrefixOnly) {
                if (getFromBTree(uri1).wordCount(this.word) > getFromBTree(uri2).wordCount(this.word)) {
                    return 1;
                } else if (getFromBTree(uri1).wordCount(this.word) == getFromBTree(uri2).wordCount(this.word)) {
                    return 0;
                } else {
                    return -1;
                }
            } else {
                int doc1PrefixCount = 0;
                int doc2PrefixCount = 0;
                for (String word : getFromBTree(uri1).getWords()) {
                    if (word.startsWith(this.word)) {
                        doc1PrefixCount++;
                    }
                }
                for (String word : getFromBTree(uri2).getWords()) {
                    if (word.startsWith(this.word)) {
                        doc2PrefixCount++;
                    }
                }
                if (doc1PrefixCount > doc2PrefixCount) {
                    return 1;
                } else if (doc1PrefixCount == doc2PrefixCount) {
                    return 0;
                } else {
                    return -1;
                }
            }
        }
    }

    private class MinHeapNode implements Comparable<MinHeapNode> {
        private URI uri;
        MinHeapNode(URI uri){
            this.uri = uri;
        }
        @Override
        public int compareTo(MinHeapNode node2){
            Document doc1 = bTree.get(this.uri);
            Document doc2 = bTree.get(node2.uri);
            if(doc1.getLastUseTime() > doc2.getLastUseTime()){
                return 1;
            } else if(doc1.getLastUseTime() < doc2.getLastUseTime()){
                return -1;
            }
            return 0;
        }
        @Override 
        public boolean equals(Object obj){
            if(this == obj){
                return true;
            }
            if(obj == null){
                return false;
            }
            if(!(obj instanceof MinHeapNode)){
                return false;
            }
            MinHeapNode node2 = (MinHeapNode) obj;
            if(this.uri.equals(node2.uri)){
                return true;
            }
            return false;
        }
        @Override 
        public int hashCode(){
            return this.uri.hashCode();
        }
    
    }

    public DocumentStoreImpl() {
        DocumentPersistenceManager dpm = new DocumentPersistenceManager();
        constructorMeat(dpm);
    }

    public DocumentStoreImpl(File baseDir){
        DocumentPersistenceManager dpm = new DocumentPersistenceManager(baseDir);
        constructorMeat(dpm);
    }

    private void constructorMeat(DocumentPersistenceManager dpm){
        bTree = new BTreeImpl<URI, Document>();
        bTree.setPersistenceManager(dpm);
        commandStack = new StackImpl<>();
        lastDocDeletedTable = new HashMap<URI,StackImpl<Document>>();
        lastDocOverWrittenTable = new HashMap<URI,StackImpl<Document>>();
        trie = new TrieImpl<>();
        maxDocBytes = Integer.MAX_VALUE;
        maxDocCount = Integer.MAX_VALUE;
        heapCount = 0;
        heap = new MinHeapImpl<>();
        actualDocBytes = 0;
        actualDocCount = 0;
        inHeap = new HashMap<>();
        //write the apply logic
        lambdaDelete = (uri) -> {
            StackImpl<Document> stack = lastDocDeletedTable.get(uri);
            Document doc = stack.pop();
            bTree.put(uri, doc);
            addDocToTrie(doc); //does this undo the delete from trie, ie, this doc is saved in the trie again
            addToHeap(doc);
            checkHeapSize();
            return true;
        };
        //when put added a brand new doc
        lambdaPutNew = (uri) -> {
            Document docToDelete = getFromBTree(uri);
            removeFromTrie(docToDelete); //should delete from trie
            deleteFromHeap(docToDelete);
            checkHeapSize();
            bTree.put(uri, null);
            return true;
        };
        //when put overwrote a previously existent doc
        lambdaPutExisting = (uri) -> {
            StackImpl<Document> stack = lastDocOverWrittenTable.get(uri);
            Document doc = stack.pop();
            Document docToDelete = getFromBTree(uri);
            removeFromTrie(docToDelete); //put doesnt override spot in trie, so need both steps
            addDocToTrie(doc);
            deleteFromHeap(docToDelete);
            bTree.put(uri ,doc); //then removing it from table also
            addToHeap(doc);
            checkHeapSize();
            return true;
        };
    }



    /**
     * the two document formats supported by this document store.
     * Note that TXT means plain text, i.e. a String.
     */
    enum DocumentFormat{
        TXT,BINARY
    };
    /**
     * @param input the document being put
     * @param uri unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return if there is no previous doc at the given URI, return 0. If there is a previous doc, return the hashCode of the previous doc. If InputStream is null, this is a delete, and thus return either the hashCode of the deleted doc or 0 if there is no doc to delete.
     * @throws IOException if there is an issue reading input
     * @throws IllegalArgumentException if uri or format are null
     */
    public int put(InputStream input, URI uri, DocumentStore.DocumentFormat format) throws IOException {
        if(uri == null || format == null){
            throw new IllegalArgumentException("Can not pass in null URI or DocumentFormat");
        }
        Document doc = createDocument(input,uri, format);
        int i = addToCommandStackBasic(doc, uri, format);
        if(input != null) {
            bTree.put(uri, doc);
            addToHeap(doc);
            if (this.actualDocCount > this.maxDocCount || this.actualDocBytes > this.maxDocBytes) {
                MinHeapNode node =  heap.remove();
                inHeap.put(node.uri, false);
                Document d = bTree.get(node.uri);
                try{
                    bTree.moveToDisk(d.getKey());
                } catch(Exception e){
                    if(e instanceof IOException){
                        throw (IOException)e;
                    }
                    i = 0; // effectively returning 0 for any other issue
                }
                //removeUriFromCommandStackWithoutUndo(d.getKey());
                this.actualDocCount--;
                this.actualDocBytes = this.actualDocBytes - getDocBytes(d);
            }
        }
        return i;
    }

    private void addToHeap(Document d){
        d.setLastUseTime(System.nanoTime());
        if(bTree.get(d.getKey()) == null){
            bTree.put(d.getKey(), d);
        }
        heap.insert(new MinHeapNode(d.getKey()));
        inHeap.put(d.getKey(), true);
        this.actualDocCount++;
        this.actualDocBytes = this.actualDocBytes + getDocBytes(d);
        heapCount++;
    }

    private int getDocBytes(Document d){
        if(d.getDocumentBinaryData() != null){
            return d.getDocumentBinaryData().length;
        }
        return d.getDocumentTxt().getBytes().length;
    }
    /**
     * @param uri the unique identifier of the document to get
     * @return the given document
     */
    public Document get(URI uri) {
        return getFromBTree(uri);
    }

    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    public boolean delete(URI uri){
        Document temp = bTree.put(uri, null);
        if(temp != null) {
            //if it is, then no document was deleted
            GenericCommand<URI> c = new GenericCommand(uri, lambdaDelete);
            commandStack.push(c);
            StackImpl<Document> stack = getStackToAddTo(uri, lastDocDeletedTable);
            stack.push(temp);
            removeFromTrie(temp);
            deleteFromHeap(temp);
        }
        //all these methods end up adding it back - so need to remove again
        bTree.put(uri, null);
        return (temp == null) ? false : true;
    }
    /**
     * undo the last put or delete command
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    public void undo() throws IllegalStateException{
        if(commandStack.size() == 0){
            throw new IllegalStateException("Command Stack is empty, an undo cannot be done");
        }
        Undoable command = commandStack.peek();
        if(command instanceof GenericCommand){
            commandStack.pop().undo();
        } else{
            CommandSet<GenericCommand<URI>> cs = (CommandSet<GenericCommand<URI>>) command;
            cs.undoAll();
            commandStack.pop();
        }
    }

    /**
     * undo the last put or delete that was done with the given URI as its key
     * @param uri
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    public void undo(URI uri) throws IllegalStateException{
        Undoable command = undoURIFromCommandStack(uri);
        if(command == null){
            throw new IllegalStateException("No commands for given URI to be undone");
        }
        //bTree.put(uri, null);
        /*boolean foundUri = undoURIFromCommandStack(uri);
        if(!foundUri){
            throw new IllegalStateException("No commands for given URI to be undone");
        }*/
    }

    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     * @param keyword
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> search(String keyword){
        if(keyword == null){
            throw new IllegalArgumentException("please pass in a valid keyword DocStore.search");
        }
        List<URI> list = trie.getAllSorted(keyword, new DocumentComparator(keyword, false));
        List<Document> listToReturn = new ArrayList<>();
        long time = System.nanoTime();
        for(URI uri : list){
            Document d = getFromBTree(uri);
            d.setLastUseTime(time);
            if(bTree.get(uri) != null){
                //i.e. this doc is in store
                addToHeapIfNeed(d);
                heap.reHeapify(new MinHeapNode(d.getKey()));
            }
            listToReturn.add(d);
        }
        return listToReturn;
    }

    private void addToHeapIfNeed(Document d){
        if(!inHeap.get(d.getKey())){
            addToHeap(d);
        }
    }

    /**
     * Retrieve all documents whose text starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> searchByPrefix(String keywordPrefix){
        if(keywordPrefix == null){
            throw new IllegalArgumentException("please pass in a valid keyword DocStore.searchByPrefix");
        }
        List<URI> list = trie.getAllWithPrefixSorted(keywordPrefix, new DocumentComparator(keywordPrefix, true));
        long time = System.nanoTime();
        List<Document> listToReturn = new ArrayList<>();
        for(URI uri : list){
            Document d = getFromBTree(uri);
            d.setLastUseTime(time);
            if(bTree.get(uri) != null){
                //i.e. this doc is in store
                addToHeapIfNeed(d);
                heap.reHeapify(new MinHeapNode(d.getKey()));
            }
            listToReturn.add(d);
        }
        return listToReturn;
    }

    /**
     * Completely remove any trace of any document which contains the given keyword
     * Search is CASE SENSITIVE.
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAll(String keyword){
        Set<URI> deletedDocs = trie.deleteAll(keyword);
        CommandSet<URI> cs = new CommandSet();
        if(deletedDocs != null){
            for(URI uri : deletedDocs){
                Document d = getFromBTree(uri);
                StackImpl<Document> stack = getStackToAddTo(d.getKey(), lastDocDeletedTable);
                stack.push(d);
                //bTree.put(d.getKey(), null);
                GenericCommand<URI> gc = new GenericCommand(d.getKey(),lambdaDelete);
                cs.addCommand(gc);
                removeFromTrie(d);
                deleteFromHeap(d);
                bTree.put(d.getKey(), null);
            }
            commandStack.push(cs);
        }
        return deletedDocs;
    }

    private void deleteFromHeap(Document d){
        d.setLastUseTime(0);
        if(bTree.get(d.getKey()) == null){
            bTree.put(d.getKey(), d);
        }
        MinHeapNode node = new MinHeapNode(d.getKey());
        heap.reHeapify(node);
        heap.remove();
        inHeap.put(d.getKey(), false);
        heapCount--;
        this.actualDocCount--;
        this.actualDocBytes = this.actualDocBytes - getDocBytes(d);
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix
     * Search is CASE SENSITIVE.
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithPrefix(String keywordPrefix){
        Set<URI> deletedDocs = trie.deleteAllWithPrefix(keywordPrefix);
        CommandSet<URI> cs = new CommandSet();
        for(URI uri : deletedDocs) {
            Document d = getFromBTree(uri);
            StackImpl<Document> stack = getStackToAddTo(d.getKey(), lastDocDeletedTable);
            stack.push(d);
            //bTree.put(d.getKey(), null);
            GenericCommand<URI> gc = new GenericCommand(d.getKey(), lambdaDelete);
            cs.addCommand(gc);
            removeFromTrie(d);
            deleteFromHeap(d);
            bTree.put(d.getKey(), null);
        }
        commandStack.push(cs);
        return deletedDocs;
    }

//stage 4 methods
    /**
     * set maximum number of documents that may be stored
     * @param limit
     */
    public void setMaxDocumentCount(int limit){
        if(limit < 0){
            throw new IllegalArgumentException("Limit cannot be negative");
        }
        this.maxDocCount = limit;
        checkHeapSize();
    }

    /**
     * set maximum number of bytes of memory that may be used by all the documents in memory combined
     * @param limit
     */
    public void setMaxDocumentBytes(int limit){
        if(limit < 0){
            throw new IllegalArgumentException("Limit cannot be negative");
        }
        this.maxDocBytes = limit;
        checkHeapSize();
    }


//added Methods

private Document getFromBTree(URI uri){
    Document doc = bTree.get(uri);
    if(doc != null){
        doc.setLastUseTime(System.nanoTime());
        if(!inHeap.get(uri)){
            addToHeap(doc);
            checkHeapSize();
        } else{
            heap.reHeapify(new MinHeapNode(uri));
        }
    }  
    return doc; 
}


    private void checkHeapSize(){
        while((this.actualDocCount > this.maxDocCount || this.actualDocBytes > this.maxDocBytes) && !isHeapEmpty()){
            MinHeapNode node =  heap.remove();
            Document d = bTree.get(node.uri);
            d.setLastUseTime(System.nanoTime());
            inHeap.put(node.uri, false);
            try{
                bTree.moveToDisk(node.uri);
            } catch(Exception e){
                //shouldn't happen since the value I am adding is already on disk
                return;
            }
            //removeUriFromCommandStackWithoutUndo(node.uri);
            this.actualDocCount--;
            this.actualDocBytes = this.actualDocBytes - getDocBytes(d);
        }
    }

    private boolean isHeapEmpty(){
        return this.heapCount == 0;
    }

    private Undoable undoURIFromCommandStack(URI uri) {
        StackImpl<Undoable> helper = new StackImpl<>();
        Undoable command;
        for (int i = 0; i < commandStack.size(); ) {
            command = commandStack.peek();
            if(command.getClass().equals(GenericCommand.class)){
                GenericCommand<URI> gc = (GenericCommand<URI>) command;
                if (gc.getTarget().equals(uri)) {
                    commandStack.pop().undo();
                    for (int j = 0; j < helper.size(); ) {
                        commandStack.push(helper.pop());
                    }
                    return gc;
                }
            }
            else if(command.getClass().equals(CommandSet.class)){
                CommandSet<URI> cs = (CommandSet<URI>) command;
                if(cs.containsTarget(uri)) {
                    cs.undo(uri);
                    if(cs.size() == 0){
                        commandStack.pop();
                    }
                    for (int j = 0; j < helper.size(); ) {
                        commandStack.push(helper.pop());
                    }
                    return cs;
                }
            }
            helper.push(commandStack.pop());
        }
        return null; // if gets here, there is no command for this uri in the stack
    }

    private String readInputStream(InputStream input){
        Scanner scanner = new Scanner(input);
        String text = scanner.useDelimiter("\\A").next();
        return text;
    }


    private DocumentImpl createDocument(InputStream input, URI uri, DocumentStore.DocumentFormat format) {
        DocumentImpl myDocument;
        if(input == null){
            return null;
        }
        if(format.equals(DocumentStore.DocumentFormat.TXT)){
            String text =  readInputStream(input);
            myDocument = new DocumentImpl(uri,text,null);
        } else {
            byte[] byteArray;
            try{
                byteArray = input.readAllBytes();
            } catch(IOException e) {
                throw new RuntimeException("private method createDocument failed");
            }
            myDocument = new DocumentImpl(uri, byteArray);
        }
        return myDocument;
    }

    private StackImpl getStackToAddTo(URI uri, HashMap<URI, StackImpl<Document>> table){
        if(table.get(uri) != null) {
            return table.get(uri);
        } else{
            StackImpl<Document> createdStack = new StackImpl<>();
            table.put(uri, createdStack);
            return createdStack;
        }
    }

    private void addDocToTrie(Document doc){
        if(doc.getDocumentTxt() == null){
            return;
        }
        Set<String> allWords = doc.getWords();
        for(String s : allWords){
            s = s.replaceAll("[^a-zA-Z0-9]", "");
            if(!s.isEmpty()){
                trie.put(s, doc.getKey());
            }
        }
    }

    private void removeFromTrie(Document doc){
        if(doc.getDocumentTxt() == null ){
            return;
        }
        for(String s : doc.getWords()){
            s = s.replaceAll("[^a-zA-Z0-9]", "");
            if(!s.isEmpty()) {
                trie.delete(s, doc.getKey());
            }
        }
    }

    private int deleteForNullInput(URI uri){
        GenericCommand<URI> c;
        Document prev = bTree.put(uri, null);
        deleteFromHeap(prev);
        if(prev != null){
            StackImpl<Document> stack = getStackToAddTo(uri, lastDocDeletedTable);
            stack.push(prev);
        }
        c = new GenericCommand(uri, lambdaDelete);
        commandStack.push(c);
        return (prev == null) ? 0 : prev.hashCode();
    }

    private int addToCommandStackBasic(Document myDocument, URI uri, DocumentStore.DocumentFormat format){
        GenericCommand<URI> c;
        if (myDocument == null) { //ie inputStream was null
            return deleteForNullInput(uri);
        }
        //add doc to table and trie
        if(format.equals(DocumentStore.DocumentFormat.TXT)){
            addDocToTrie(myDocument);
        }
        Document prev = bTree.put(uri, myDocument);
        if(prev == null) {
            c = new GenericCommand(uri,lambdaPutNew);
        } else{
            StackImpl<Document> stack = getStackToAddTo(uri, lastDocOverWrittenTable);
            stack.push(prev);
            c = new GenericCommand(uri,lambdaPutExisting);
            //rewriting prev, so should completely remove it
            removeFromTrie(prev);
            deleteFromHeap(prev);
        }
        commandStack.push(c);
        return (prev == null) ? 0 : prev.hashCode();
    }
}