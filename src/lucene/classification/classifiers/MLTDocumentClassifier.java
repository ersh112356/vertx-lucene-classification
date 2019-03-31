/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucene.classification.classifiers;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import lucene.classification.classifiers.base.LuceneDocumentClassifier;
import lucene.classification.documents.Documents;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author eran
 */
public class MLTDocumentClassifier implements LuceneDocumentClassifier{
    
    /** The Logger object. */
    protected final Logger logger = LoggerFactory.getLogger(MLTDocumentClassifier.class);
    /** Holds the searcher object. */
    private IndexSearcher searcher;
    /** Holds the searcher manager. */
    private SearcherManager sm;
    /** Holds the Analyzer to use here. */
    private Analyzer defaultAnalyzer;
    /** Holds the fields that take action here. */
    private String[] fields;
    /** Holds a map between a field and an analyzer. */
    private final Map<String,Analyzer> map = new HashMap<>();
    /** Holds the Vertx object that holds the context. */
    private Vertx vertx;
    
    /**
     * constructor. 
     * 
     * @param path- the path to the index files.
     * @param fields- the fields that take a part here.
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    private MLTDocumentClassifier(String path, String... fields){
        
        this(null,path,fields);
    }
    
    /**
     * constructor. 
     * 
     * @param vertx- the Vertx object that holds the context.
     * @param path- the path to the index files.
     * @param fields- the fields that take a part here.
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    private MLTDocumentClassifier(Vertx vertx, String path, String... fields){
        
        this.vertx = vertx;
        
        try
        {
            init(path,fields);
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
     * @param fields- the fields that take a part here.
     * 
     * @return this object.
     */
    public static MLTDocumentClassifier create(String path, String... fields){
        
        return create(null,path,fields);
    }
    
    /**
     * Create a new instance of this object.
     * 
     * @param vertx- the Vertx object that holds the context. If not using async calls, that can go null.
     * @param path- the path to the index files.
     * @param fields- the fields that take a part here.
     * 
     * @return this object.
     */
    public static MLTDocumentClassifier create(Vertx vertx, String path, String... fields){
        
        return new MLTDocumentClassifier(vertx,path,fields);
    }
    
    /**
     * Initiate this service.
     * 
     * @param path- the path to Lucene files.
     * @param fields- the field that take a part here.
     * 
     * @throws java.io.IOException
     */
    protected void init(String path, String... fields) throws IOException{
        
        this.fields = fields;
        
        if(searcher==null)
        {   // Time to initiate the classifier.
            defaultAnalyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);
            
            Path fpath = FileSystems.getDefault().getPath(path);
            Directory ldir = FSDirectory.open(fpath);
            sm = new SearcherManager(ldir,null);
           
            Stream.of(fields).forEach(field->{
                map.put(field,defaultAnalyzer);
            });
        }
    }
    
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
    @Override
    public JsonArray classify(Document doc, Query query, int num) throws IOException{
        
        try
        {   // Implementation of the near real time searcher.
            searcher = sm.acquire();
            IndexReader reader = searcher.getIndexReader();
            MoreLikeThis mlt = new MoreLikeThis(reader);
            mlt.setFieldNames(fields);
            mlt.setMinTermFreq(1);
            mlt.setMinDocFreq(1);
            mlt.setAnalyzer(defaultAnalyzer);
            
            TopDocs topDocs = mtlSearch(mlt,doc,query,num);

            JsonArray result = new JsonArray();
            double total = 0.0;

            for(ScoreDoc scoreDoc : topDocs.scoreDocs)
            {
                total+=scoreDoc.score;
            }

            for(ScoreDoc scoreDoc : topDocs.scoreDocs)
            {   // This retrieves the actual Document object from Document the index using the document number.
                // scoreDoc.doc is an integer that is the doc's id.
                double confidence = scoreDoc.score/total;
                Document ldoc = searcher.doc(scoreDoc.doc);

                JsonObject outcome = new JsonObject()
                    .put("doc",Documents.fromDocument(ldoc))
                    .put("result",confidence);

                result.add(outcome);
            }

            return result;
        }
        finally
        {
            sm.release(searcher);
        }
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
    
    /**
     * Returns the top n results from query based on a Document via a More-Like-This.
     *
     * @param mlt- the MoreLikeThis object to use.
     * @param document- the document to use for More Like This search.
     * @param query- a few condition to filter by.
     * @param num- the number of documents to fetch.
     * 
     * @return the top results for the MLT query.
     * 
     * @throws IOException If there is a low-level I/O error.
     */
    private TopDocs mtlSearch(MoreLikeThis mlt, Document document, Query query, int num) throws IOException{
      
        BooleanQuery.Builder mltQuery = new BooleanQuery.Builder();
        
        // We want always to use the boost coming from Document TF * IDF of the term.
        mlt.setBoost(true);

        for(String fieldName : fields)
        {
            String boost = null;
            
            if(fieldName.contains("^"))
            {
                String[] field2boost = fieldName.split("\\^");
                fieldName = field2boost[0];
                boost = field2boost[1];
            }

            String[] fieldValues = document.getValues(fieldName);
            
            if(boost!=null)
            {   // This is an additional multiplicative boost coming fromDocument the field boost.
                mlt.setBoostFactor(Float.parseFloat(boost));
            }

            Analyzer analyzer = map.get(fieldName);
            mlt.setAnalyzer(analyzer);
            
            for(String fieldContent : fieldValues)
            {
                mltQuery.add(new BooleanClause(mlt.like(fieldName,new StringReader(fieldContent)),BooleanClause.Occur.SHOULD));
            }
        }
        
        if(query!=null)
        {
            mltQuery.add(query,BooleanClause.Occur.MUST);
        }
        
        return searcher.search(mltQuery.build(),num);
    }
}
