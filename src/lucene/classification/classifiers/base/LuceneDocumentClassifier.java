/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucene.classification.classifiers.base;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import java.io.IOException;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;

/**
 *
 * @author eran
 */
public interface LuceneDocumentClassifier{
    
    /**
     * Start the classification process.
     * 
     * @param doc- the document to classify.
     * @param query- n optional Query to allow filter conditions.
     * @param num- the number of documents to fetch.
     * 
     * @return the classification outcome.
     * 
     * @throws java.io.IOException
     */
    public JsonArray classify(Document doc, Query query, int num) throws IOException;
    
    /**
     * Start the classification process.
     * 
     * @param doc- the document to classify.
     * @param query- n optional Query to allow filter conditions.
     * @param num- the number of documents to fetch.
     * @param future- the Future object to report back.
     */
    public void classifyAsync(Document doc, Query query, int num, Future<JsonArray> future);
    
    /**
     * Start the classification process.
     * 
     * @param doc- the document to classify.
     * @param query- n optional Query to allow filter conditions.
     * @param num- the number of documents to fetch.
     * 
     * @return the classification outcome.
     */
    public Future<JsonArray> classifyAsync(Document doc, Query query, int num);
}