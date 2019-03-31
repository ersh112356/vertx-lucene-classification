/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucene.classification.documents;

import io.vertx.core.json.JsonObject;
import java.util.Map;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

/**
 *
 * @author eran
 */
public class Documents{
 
    /**
     * The constructor.
     * All statics.
     */
    private Documents(){
    }
    
    /**
     * Create a new Lucene document.
     * 
     * @param values- a Map of parameters to put in the document.
     * 
     * @return a new Lucene document.
     */
     public static Document toDocument(Map<String,JsonObject> values){
        
        Document doc = new Document();

        values.forEach((field,jo) -> {
            
            Object first = jo.getValue("first");
            boolean tokenize = jo.getBoolean("second");
            boolean store = jo.getBoolean("third");
            
            Field f = toField(field,first,tokenize,store);
            doc.add(f);
        });
        
        return doc;
    }
     
    /**
     * Create a new Lucene document.
     * 
     * @param values- a JsonObject of parameters to put in the document.
     * 
     * @return a new Lucene document.
     */
     public static Document toDocument(JsonObject values){
        
        Document doc = new Document();

        values.forEach(entry -> {
            
            String field = entry.getKey();
            Object value = entry.getValue();
            
            if(value instanceof String)
            {
                Field f = toField(field+"_value",value,false,true);
                doc.add(f);
            }
            else if(value instanceof Integer || value instanceof Long)
            {
                Field f = toField(field+"_value",""+value,false,true);
                doc.add(f);
            }
            
            Field f = toField(field,value,true,true);
            doc.add(f);
        });
        
        return doc;
    }
     
    /**
     * Creates a new field.
     * 
     * @param field- the name of the field fromDocument create.
     * @param first- the value fromDocument insert in.
     * @param tokenize- should the value be tokenized.
     * @param store- should the value be stored.
     * 
     * @return a new Field object.
     */
     private static Field toField(String field, Object first, boolean tokenize, boolean store){
         
        Field f;
            
        if(first instanceof Integer)
        {
            int value = (Integer)first;
            //f = new IntPoint(field,value);
            f = store ? new StoredField(field,value) : new IntPoint(field,value);
        }
        else if(first instanceof Long)
        {
            long value = (Long)first;
            //f = new LongPoint(field,value);
            f = store ? new StoredField(field,value) : new LongPoint(field,value);
        }
        else if(first instanceof String)
        {   // Please mote:
            // A string is a single unit that not supposed to be separated, analyzed. 
            // For example, the id, email, url, date, etc. The string itself is a term.
            // Text is content, article, post, document and anything that may read by human. 
            // This is the thing you want to index and search. 
            // It should be analyzed, indexed and optionally stored.
            
            String text = (String)first;

            if(store)
            {
                f = tokenize ? new TextField(field,text,Field.Store.YES) : new StringField(field,text,Field.Store.YES);
            }
            else
            {
                f = tokenize ? new TextField(field,text,Field.Store.NO) : new StringField(field,text,Field.Store.NO);
            }
        }
        else
        {
            throw new IllegalArgumentException();
        }
        
        return f;
    }
     
    /**
     * Convert a given Document fromDocument a JsonObject.
     * 
     * @param doc- the document fromDocument convert.
     * 
     * @return a JsonObject.
     */
    public static JsonObject fromDocument(Document doc){
        
        final JsonObject result = new JsonObject();
        
        doc.forEach(field->{
        
            String value = doc.get(field.name());
            
            result.put(field.name(),value);
        });
        
        return result;
    }
}