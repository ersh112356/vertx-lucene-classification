/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucene.classification.indexes;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author eran
 */
public class Indexer{
 
    /** The Logger object. */
    protected final Logger logger = LoggerFactory.getLogger(Indexer.class);
    /** Holds the default Analyzer. */
    private static final Analyzer analyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);
    /** Holds the IndexWriter Object. */
    private IndexWriter indexWriter = null;
    /** Holds a flag to determine whether all Writer/Reader are in place and ready to be used. */
    private boolean ready = false;
    /** Holds the path to Lucene files. */
    @SuppressWarnings("FieldMayBeFinal")
    private String path;
    /** Holds the Vertx that holds the context. That needs only for async calls, and can go null otherwise.  */
    @SuppressWarnings("FieldMayBeFinal")
    private Vertx vertx;
    /** Holds a map of IndexWriter to path. That's needed as there's only one IndexWriter attached to a path.  */
    private static final Map<String,Indexer> indexers = new ConcurrentHashMap<>();
    
    /**
     * The constructor.
     * 
     * @param vertx- the Vertx that holds the context.
     * @param path- the path to Lucene files.
     */
    @SuppressWarnings("static-access")
    private Indexer(Vertx vertx, String path){
        
        this.vertx = vertx;
        this.path = path;
    }
    
    /**
     * Create a new Indexer.
     * 
     * @param path- the path to the index files.
     * 
     * @return this object.
     */
    public synchronized static Indexer create(String path){
        
        return create(null,path);
    }
    
    /**
     * Create a new Indexer, if not yet used here.
     * If so, the Indexer is taken from a Map. 
     * That's due to that only one IndexWriter can be attached to a given path.
     * 
     * @param vertx- the Vertx that holds the context. If not using async calls, can go null.
     * @param path- the path to the index files.
     * 
     * @return this object.
     */
    public synchronized static Indexer create(Vertx vertx, String path){
        
        Indexer indexer = indexers.get(path);
        
        if(indexer==null)
        {
            indexer = new Indexer(vertx,path);
            indexers.put(path,indexer);
        }
        
        return indexer; 
    }
    
    /**
     * Insert a new Document object into Lucene.
     * 
     * @param doc- the Document to insert.
     * 
     * @return this object to allow chaining.
     * 
     * @throws IOException on error.
     */
    public Indexer add(Document doc) throws IOException{
        
        initWriter(path);
        indexWriter.addDocument(doc);
        
        // Just to be on the safe side on this. Might not be good on a big volume of documents.
        indexWriter.commit();
        
        return this;
    }
    
    /**
     * Insert a new Document object into Lucene.
     * 
     * @param doc- the Document to insert.
     * 
     * @return a Future object to report back.
     */
    public Future<Boolean> addAsync(Document doc){
        
        Future<Boolean> future = Future.future();
        
        vertx.executeBlocking(res->{
            
            try
            {
                add(doc);
                future.complete(true);
            }
            catch(IOException e)
            {
                logger.error("Failed to insert a Document due to {}",e.getMessage());
                
                future.fail(e);
            }
        },null);
        
        return future;
    }
    
    /**
     * Insert a new Document object into Lucene.
     * 
     * @param doc- the Document to insert.
     * @param future- the Future object to report back.
     */
    public void addAsync(Document doc, Future<Boolean> future){
        
        vertx.executeBlocking(res->{
            
            try
            {
                add(doc);
                future.complete(true);
            }
            catch(IOException e)
            {
                logger.error("Failed to insert a Document due to {}",e.getMessage());
                
                future.fail(e);
            }
        },null);
    }
    
    /**
     * Delete a Lucene Document.
     * 
     * @param query- the query to filter the index.
     * 
     * @return this object.
     * 
     * @throws java.io.IOException on errors.
     */
    public Indexer delete(Query query) throws IOException{
        
        initWriter(path);
        indexWriter.deleteDocuments(query);
        
        return this;
    }
    
    /**
     * Delete async a Lucene Document.
     * 
     * @param query- the query to filter the index.
     * 
     * @return a Future object to report back.
     */
    public Future<Boolean> deleteAsync(Query query){
        
        Future<Boolean> future = Future.future();
        
        vertx.executeBlocking(res->{
            
            try
            {
                delete(query);
                future.complete(true);
            }
            catch(IOException e)
            {
                logger.error("Failed to delete a Document due to {}",e.getMessage());
                
                future.fail(e);
            }
        },null);
        
        return future;
    }
    
    /**
     * Delete async a Lucene Document.
     * 
     * @param query- the query to filter the index.
     * @param future- a Future object to report back.
     */
    public void deleteAsync(Query query, Future<Boolean> future){
        
        vertx.executeBlocking(res->{
            
            try
            {
                delete(query);
                future.complete(true);
            }
            catch(IOException e)
            {
                logger.error("Failed to delete a Document due to {}",e.getMessage());
                
                future.fail(e);
            }
        },null);
    }
    
    /**
     * Initiate the IndexWriter.
     * 
     * @param path- the path to Lucene files. 
     */
    private synchronized void initWriter(String path){
        
        if(!ready)
        {
            try
            {
                if(indexWriter==null)
                {   // Time to initiate the indexer.
                    Path fpath = FileSystems.getDefault().getPath(path);
                    Directory ldir = FSDirectory.open(fpath);
                    IndexWriterConfig iwConf = new IndexWriterConfig(analyzer)
                        .setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND)
                        .setCommitOnClose(true);
                    
                    indexWriter = new IndexWriter(ldir,iwConf);
                }

                ready = true;
            }
            catch(IOException e)
            {
                logger.error("Failed to create an IndexWriter due to {}",e.getMessage());
            }
        }
    }
    
    /**
     * Try to close the IndexWriter.
     */
    public void close(){
        
        if(indexWriter!=null)
        {
            try
            {
                indexWriter.close();
            }
            catch(IOException e)
            {
                logger.error("Failed to close the IndexWriter due to {}",e.getMessage());
            }
        }
    }
}