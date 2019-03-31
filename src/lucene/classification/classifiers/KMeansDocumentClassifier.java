/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucene.classification.classifiers;

import io.vertx.core.Vertx;
import lucene.classification.classifiers.base.BaseDocumentClassifier;
import java.io.IOException;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.classification.document.DocumentClassifier;
import org.apache.lucene.classification.document.KNearestNeighborDocumentClassifier;
import org.apache.lucene.index.IndexReader;

/**
 *
 * @author eran
 */
public class KMeansDocumentClassifier extends BaseDocumentClassifier{
    
    /**
     * constructor. 
     * 
     * @param path- the path to the index files.
     * @param category- the field that holds the category to classify by.
     * @param fields- the fields that take a part here.
     */
    private KMeansDocumentClassifier(String path, String category, String... fields){
        
        this(null,path,category,fields);
    }
    
    /**
     * constructor. 
     * 
     * @param vertx- the Vertx object that holds the context. Used only with async calls, and for that can go null.
     * @param path- the path to the index files.
     * @param category- the field that holds the category to classify by.
     * @param fields- the fields that take a part here.
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    private KMeansDocumentClassifier(Vertx vertx, String path, String category, String... fields){
        
        this.vertx = vertx;
        
        try
        {
            init(path,category,fields);
        }
        catch(IOException e)
        {
            logger.error("got an error {}",e);
            
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Create a new instance of this object.
     * 
     * @param path- the path to the index files.
     * @param category- the field that holds the category to classify by.
     * @param fields- the fields that take a part here.
     * 
     * @return this object.
     */
    public static KMeansDocumentClassifier create(String path, String category, String... fields){
        
        return create(null,path,category,fields);
    }
    
    /**
     * Create a new instance of this object.
     * 
     * @param vertx- the Vertx object that holds the context. Used only with async calls, and for that can go null.
     * @param path- the path to Lucene files.
     * @param category- the field that holds the category value.
     * @param fields- the fields to consider.
     * 
     * @return this object.
     */
    public static KMeansDocumentClassifier create(Vertx vertx, String path, String category, String... fields){
        
        return new KMeansDocumentClassifier(vertx,path,category,fields);
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
    @Override
    protected DocumentClassifier create(IndexReader reader, String category, Map<String,Analyzer> map, String[] fields){
        
        return new KNearestNeighborDocumentClassifier(reader,null,null,5,1,1,category,map,fields);
    }
}