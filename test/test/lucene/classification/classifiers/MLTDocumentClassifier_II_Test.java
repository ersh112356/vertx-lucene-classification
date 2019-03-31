/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.lucene.classification.classifiers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.HashMap;
import java.util.Map;
import lucene.classification.classifiers.MLTDocumentClassifier;
import lucene.classification.classifiers.base.LuceneDocumentClassifier;
import lucene.classification.documents.Documents;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author eran
 */
public class MLTDocumentClassifier_II_Test{
 
    public MLTDocumentClassifier_II_Test(){
    }
    
    @BeforeClass
    public static void setUpClass(){
    }
    
    @AfterClass
    public static void tearDownClass(){
    }
    
    @Before
    public void setUp(){
    }
    
    @After
    public void tearDown(){
    }
    
    /**
     * Test of classify method, of class MLTDocumentClassifier.
     * 
     */
    @Test
    @SuppressWarnings("UseSpecificCatch")
    public void testClassify(){
        
        System.out.println("classify");
        
        try
        {
            String[] fields = {"education","goal","gender","city","description","moto","nick","aestheticShape","pref","time","status","child","height",""};
            
            String event = "{\n" +
                            "  \"education\" : {\n" +
                            "    \"first\" : \"BA\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"goal\" : {\n" +
                            "    \"first\" : \"relationship\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"gender\" : {\n" +
                            "    \"first\" : \"male\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"city\" : {\n" +
                            "    \"first\" : \"Arad\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"description\" : {\n" +
                            "    \"first\" : \"My name is Lucas Martin, and I enjoy meeting new people and finding ways to help them have an uplifting experience. I have had a variety of customer service opportunities, through which I was able to have fewer returned products and increased repeat customers, when compared with co-workers. I am dedicated, outgoing, and a team player. Who could I speak with in your customer service department about your organization’s customer service needs?\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"moto\" : {\n" +
                            "    \"first\" : \"My parents think I am a responsible boy/girl as I have been supporting them by getting a job in a good firm and they also praise me a lot for giving them a good name in the society. They have a belief that I will achieve my aim to become an officer in armed forces, and they provide me with full support to achieve it.\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"nick\" : {\n" +
                            "    \"first\" : \"Sky Bully\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"aestheticShape\" : {\n" +
                            "    \"first\" : \"hot\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"pref\" : {\n" +
                            "    \"first\" : \"widowed\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"time\" : {\n" +
                            "    \"first\" : \"1546496366937\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"status\" : {\n" +
                            "    \"first\" : \"widowed\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"child\" : {\n" +
                            "    \"first\" : \"4\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"height\" : {\n" +
                            "    \"first\" : \"165\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  }\n" +
                            "}\n" +
                            "";
            
            String event1 = "{\n" +
                            "  \"education\" : {\n" +
                            "    \"first\" : \"BA\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"goal\" : {\n" +
                            "    \"first\" : \"dating\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"gender\" : {\n" +
                            "    \"first\" : \"male\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"city\" : {\n" +
                            "    \"first\" : \"Lod\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"description\" : {\n" +
                            "    \"first\" : \"I am passionate about my work. Because I love what I do, I have a steady source of motivation that drives me to do my best. In my last job, this passion led me to challenge myself daily and learn new skills that helped me to do better work. For example, I taught myself how to use Photoshop to improve the quality of our photos and graphics. I soon became the go-to person for any design needs.\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"moto\" : {\n" +
                            "    \"first\" : \"I think that I am a responsible and honest boy/girl who wants to do things successfully. I am punctual towards my work and do it before time. I believe that mutual cooperation is a way to success and like to help people whenever they seek my help. I am an average student and like to read books and play chess.\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"nick\" : {\n" +
                            "    \"first\" : \"Midas\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"aestheticShape\" : {\n" +
                            "    \"first\" : \"irresistible\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"pref\" : {\n" +
                            "    \"first\" : \"separated\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"time\" : {\n" +
                            "    \"first\" : \"1547213972640\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"status\" : {\n" +
                            "    \"first\" : \"separated\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"child\" : {\n" +
                            "    \"first\" : \"1\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  },\n" +
                            "  \"height\" : {\n" +
                            "    \"first\" : \"187\",\n" +
                            "    \"second\" : true,\n" +
                            "    \"third\" : true\n" +
                            "  }\n" +
                            "}";
            
            //JsonObject jo = new JsonObject(event);
            JsonObject jo = new JsonObject(event1);
            
            Map<String,JsonObject> values = new HashMap<>();
            
            jo.forEach(entry ->{
                String name = entry.getKey();
                JsonObject j = (JsonObject)entry.getValue();
                
                values.put(name,j);
            });
            
            Document doc = Documents.toDocument(values);
            
            // Filter by a range of ages, and drop myself idenfified by a UUID value.
            // The field to filter by MUST NOT be tokenized.
            Query range =  IntPoint.newRangeQuery("age",55,66);
            TermQuery user = new TermQuery(new Term("username","7c6ea5db-26bc-43dc-9e6b-191ef11b7d1a"));
            
            BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
            booleanQuery.add(range,BooleanClause.Occur.MUST);
            booleanQuery.add(user,BooleanClause.Occur.MUST_NOT);
            
            LuceneDocumentClassifier classifier = MLTDocumentClassifier.create("../resources/index/users/mlt",fields);
            JsonArray ja = classifier.classify(doc,booleanQuery.build(),10);
            
            System.out.println(ja.encodePrettily());
            
            assertFalse(ja.isEmpty());
        }
        catch(Throwable t)
        {
            fail(""+t);
        }
    }
}