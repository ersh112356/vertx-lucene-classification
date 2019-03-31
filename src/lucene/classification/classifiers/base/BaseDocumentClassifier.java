/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucene.classification.classifiers.base;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.classification.ClassificationResult;
import org.apache.lucene.classification.document.DocumentClassifier;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author eran
 */
public abstract class BaseDocumentClassifier implements LuceneDocumentClassifier{
    
    /** The Logger object. */
    protected final Logger logger = LoggerFactory.getLogger(BaseDocumentClassifier.class);
    /** Holds the Lucene classifier that runs the show. */
    protected DocumentClassifier classifier;
    /** Holds the Vertx object that holds the context. */
    protected Vertx vertx;
    
    /**
     * Initiate this service.
     * 
     * @param path- the path to Lucene files.
     * @param category- the field that holds the category to classify by.
     * @param fields- the field that take a part here.
     * 
     * @throws java.io.IOException
     */
    protected void init(String path, String category, String... fields) throws IOException{
        
        if(classifier==null)
        {   // Time to initiate the classifier.
            Analyzer analyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);
            
            Path fpath = FileSystems.getDefault().getPath(path);
            Directory ldir = FSDirectory.open(fpath);
            IndexReader reader = DirectoryReader.open(ldir);
            
            final Map<String,Analyzer> map = new HashMap<>();
            
            Stream.of(fields).forEach(field->{
                map.put(field,analyzer);
            });
            
            classifier = create(reader,category,map,fields);
        }
    }
    
    /**
     * Create a new instance of the classifier.
     * 
     * @param reader- the IndexReeader to use.
     * @param category- the field that holds the category value.
     * @param map- a Map of Analyzer to a Field.
     * @param fields- the fields to consider.
     * 
     * @return the classifier.
     */
    protected abstract DocumentClassifier create(IndexReader reader, String category, Map<String,Analyzer> map, String[] fields);
    
    /**
     * Start the classification process.
     * 
     * @param doc- the document to classify.
     * @param query- n optional Query to allow filter conditions.
     * @param num- the number of documents to fetch.
     * 
     * @return the classification outcome.
     * 
     * @throws java.io.IOException on errors.
     */
    @Override
    public JsonArray classify(Document doc, Query query, int num) throws IOException{

        List<ClassificationResult<BytesRef>> results = classifier.getClasses(doc,num);
        /*ClassificationResult<BytesRef> dresult = classifier.assignClass(doc);
        String dclassified = dresult.getAssignedClass().utf8ToString();
        double dconfidence = dresult.getScore();*/
        
        final JsonArray json = new JsonArray();
        
        results.forEach(result -> {
            String classified = result.getAssignedClass().utf8ToString();
            double confidence = result.getScore();

            JsonObject outcome = new JsonObject()
                .put("cat",classified)
                .put("result",confidence);

            json.add(outcome);
        });
        
        return json;
    }
    
    /**
     * Start the classification process.
     * 
     * @param doc- the document to classify.
     * @param query- n optional Query to allow filter conditions.
     * @param num- the number of documents to fetch.
     * @param future- the Future object to report back.
     */
    @Override
    public void classifyAsync(Document doc, Query query, int num, Future<JsonArray> future){
        
        vertx.executeBlocking(res->{
            try
            {
                JsonArray ja = classify(doc,query,num);
                future.complete(ja);
            }
            catch(IOException e)
            {
                logger.error("Failed to classify due to {}",e.getMessage());
                
                future.fail(e);
            }
        },null);
    }
    
    /**
     * Start the classification process.
     * 
     * @param doc- the document to classify.
     * @param query- n optional Query to allow filter conditions.
     * @param num- the number of documents to fetch.
     * 
     * @return the classification outcome.
     */
    @Override
    public Future<JsonArray> classifyAsync(Document doc, Query query, int num){
        
        Future<JsonArray> future = Future.future();
        
        vertx.executeBlocking(res->{
            try
            {
                JsonArray ja = classify(doc,query,num);
                future.complete(ja);
            }
            catch(IOException e)
            {
                logger.error("Failed to classify due to {}",e.getMessage());
                
                future.fail(e);
            }
        },null);
        
        return future;
    }
}
