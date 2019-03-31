/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.lucene.classification.classifiers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lucene.classification.classifiers.MLTDocumentClassifier;
import lucene.classification.classifiers.base.LuceneDocumentClassifier;
import lucene.classification.documents.Documents;
import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author eran
 */
public class MLTDocumentClassifierTest{
    
    public MLTDocumentClassifierTest(){
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
     */
    @Test
    @SuppressWarnings("UseSpecificCatch")
    public void testClassify() throws Exception{
        
        System.out.println("classify");
        
        try
        {
            String[] fields = {"marketingCampignCreationDate","marketingCampignExposeDate","marketingCampignCustomerClickTimestamp","marketingCampignCustomerClickIndication",
                               "marketingCampignLandingPageCustomerClickTimestamp","marketingCampignLandingPageIndication","digitalStartCustomerClickTimestamp","digitalStartCustomerClickIndication",
                               "digitalStep1StartCustomerClickIndication","digitalUpdateDetailsCustomerIndication","digitalStep1EndCustomerClickIndication","digitalStep2StartCustomerClickIndication",
                               "digitalUpdateDetailsCustomerStep","digitalStep1StartCustomerClickTimestamp","digitalStep1EndCustomerClickTimestamp","digitalStep2EndCustomerClickIndication",
                               "digitalStep2StartCustomerClickTimestamp","digitalStep2EndCustomerClickTimestamp","digitalStep3StartCustomerClickIndication","digitalStep3EndCustomerClickIndication",
                               "digitalStep3StartCustomerClickTimestamp","digitalStep3EndCustomerClickTimestamp","digitalStep4StartCustomerClickIndication","digitalStep4EndCustomerClickIndication",
                               "digitalStep4StartCustomerClickTimestamp","digitalStep4EndCustomerClickTimestamp","digitalStep5StartCustomerClickIndication","digitalStep5EndCustomerClickIndication",
                               "digitalStep5StartCustomerClickTimestamp","digitalStep5EndCustomerClickTimestamp","marketingCampignGeneralClickCost","marketingCampignGeneralLandingCost",
                               "digitalRequestForEmailCustomerIndication","contact_channel","source","score","source_system","browserType","deviceType","mobileType","yearOfManufacture","claimsInTheLast3Years",
                               "vehicleDrivesOnSaturdayDesc","youngestDriverBirthDate","youngestDriverGender","youngestDriverYearOfIssuingLicense","insuranceStartDate"};
            
            String line = "{\"marketingCampignCreationDate\":\"01/01/2018\",\"marketingCampignExposeDate\":\"21/01/2018\",\"marketingCampignCustomerClickTimestamp\":\"14/06/2018\",\"marketingCampignCustomerClickIndication\":0,\"marketingCampignLandingPageCustomerClickTimestamp\":\"24/08/2018\",\"marketingCampignLandingPageIndication\":0,\"digitalStartCustomerClickTimestamp\":\"11/11/2018\",\"digitalStartCustomerClickIndication\":0,\"digitalStep1StartCustomerClickIndication\":0,\"digitalUpdateDetailsCustomerIndication\":0,\"digitalStep1EndCustomerClickIndication\":0,\"digitalStep2StartCustomerClickIndication\":0,\"digitalUpdateDetailsCustomerStep\":1,\"digitalStep1StartCustomerClickTimestamp\":\"04/12/2018\",\"digitalStep1EndCustomerClickTimestamp\":\"15/12/2018\",\"digitalStep2EndCustomerClickIndication\":0,\"digitalStep2StartCustomerClickTimestamp\":\"17/12/2018\",\"digitalStep2EndCustomerClickTimestamp\":\"20/12/2018\",\"digitalStep3StartCustomerClickIndication\":0,\"digitalStep3EndCustomerClickIndication\":0,\"digitalStep3StartCustomerClickTimestamp\":\"23/12/2018\",\"digitalStep3EndCustomerClickTimestamp\":\"30/12/2018\",\"digitalStep4StartCustomerClickIndication\":0,\"digitalStep4EndCustomerClickIndication\":0,\"digitalStep4StartCustomerClickTimestamp\":\"30/12/2018\",\"digitalStep4EndCustomerClickTimestamp\":\"30/12/2018\",\"digitalStep5StartCustomerClickIndication\":0,\"digitalStep5EndCustomerClickIndication\":0,\"digitalStep5StartCustomerClickTimestamp\":\"30/12/2018\",\"digitalStep5EndCustomerClickTimestamp\":\"30/12/2018\",\"marketingCampignGeneralClickCost\":0.01,\"marketingCampignGeneralLandingCost\":0.001,\"digitalRequestForEmailCustomerIndication\":0,\"contact_channel\":\"נציג\",\"source\":\"עובד חברה\",\"score\":84,\"source_system\":\"אתר מידע אישי\",\"browserType\":\"פיירפוקס\",\"deviceType\":\"נייד\",\"mobileType\":\"LG\",\"yearOfManufacture\":2016,\"claimsInTheLast3Years\":2,\"vehicleDrivesOnSaturdayDesc\":0,\"youngestDriverBirthDate\":\"13/05/1986\",\"youngestDriverGender\":\"נקבה\",\"youngestDriverYearOfIssuingLicense\":1996,\"insuranceStartDate\":\"01/01/2019\"}";
            line = line.replace("{","[{").replace("}","}]").replace("\",\"","\"},{\"")
                   .replace("0,\"","0},{\"").replace("1,\"","1},{\"").replace("2,\"","2},{\"")
                   .replace("3,\"","3},{\"").replace("4,\"","4},{\"").replace("5,\"","5},{\"")
                   .replace("6,\"","6},{\"").replace("7,\"","7},{\"").replace("8,\"","8},{\"")
                   .replace("9,\"","9},{\"");
            
            Map<String,JsonObject> values = new HashMap<>();
            JsonArray array = new JsonArray(line);
            array.forEach(object ->{
                JsonObject jo = (JsonObject)object;

                jo.forEach(entry->{
                    Object o = entry.getValue();
                    String value = o.toString();
                    
                    if(value.contains("/"))
                    {
                        value = ""+fromDate(o.toString());
                    }
                    
                    values.put(entry.getKey(),from(value,false,true));
                });
            });
            
            Document doc = Documents.toDocument(values);
            
            LuceneDocumentClassifier classifier = MLTDocumentClassifier.create("../resources/index/users/mlt",fields);
            JsonArray ja = classifier.classify(doc,null,10);
            
            System.out.println(ja.encodePrettily());
            
            assertFalse(ja.isEmpty());
        }
        catch(Throwable t)
        {
            fail(""+t);
        }
    }
 
    /**
     * 
     * 
     */
    private long fromDate(String st){
        
        try
        {
            String pattern = "dd/MM/yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            Date date = simpleDateFormat.parse(st);

            return date.getTime();
        }
        catch(ParseException e)
        {
            return 0;
        }
    }
    
    /**
     * 
     * 
     */
    private JsonObject from(Object... params){
        
        return new JsonObject().put("first",params[0]).put("second",params[1]).put("third",params[2]);
    }
}