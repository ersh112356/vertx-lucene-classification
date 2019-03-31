# vertx-lucene-classification

Lucene is here for a long time, ML was added to Lucene for a few releases now, yet some aspects were left out.
ML can be found under lucene-classification-lucene-version.jar.
At the moment, the ML offers a basic algorithms, for instance, the KNearest and SimpleNaiveBayes. Both do a great job in classify a phrase.  
What I was missing, is the ability to classify a given document ("the new event").
The KNearest itself is based on MLT, so it took not much of efforts to modify the algorithms to support classify via a document.
The basic implementation is as simple as:
1.	Train the model- that of course is mandatory, and can be done in offline. 
2.	Providing an array of fields to consider. Those are the fields of the document.
3.	Create any restriction on the query. For instance limit the age field to values in the range of 55-66. Please see below. 
4.	Create an instance of the classifier.
5.	And classify a given document (i.e. the new event we want to classify).

As this runs under Vert.x (vertx.io), it runs acync.

A code example:
// Create a new JsonArray from a String object.
JsonObject jo = new JsonObject(event);   
Map<String,JsonObject> values = new HashMap<>();
            
jo.forEach(entry ->{
                String name = entry.getKey();
                JsonObject j = (JsonObject)entry.getValue();
                
                values.put(name,j);
  });
   
 // That creates a new Lucene document.         
  Document doc = Documents.toDocument(values);
            
   // Filter by a range of ages, and drop myself idenfified by a UUID value.
   // The field to filter by MUST NOT be tokenized.
   Query range =  IntPoint.newRangeQuery("age",55,66);
   TermQuery user = new TermQuery(new Term("username","7c6ea5db-26bc-43dc-9e6b"));
            
  BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
  booleanQuery.add(range,BooleanClause.Occur.MUST);
  booleanQuery.add(user,BooleanClause.Occur.MUST_NOT);
             
 MLTDocumentClassifier classifier =   MLTDocumentClassifier.create(vertx,"../resources/index/users/mlt",fields);
 classifier.classifyAsync(doc,booleanQuery.build(),10).setHandler(res->{            
                if(res.succeeded())
                {
                    JsonArray ja = res.result();
                    System.out.println(ja.encodePrettily());            
                }
                else
                {
                    // Do something.
                }
 });
 
Enloy!
