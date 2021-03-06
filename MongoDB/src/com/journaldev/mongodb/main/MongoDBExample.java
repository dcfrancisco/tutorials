package com.journaldev.mongodb.main;
 
/*
 http://www.journaldev.com/3963/mongodb-java-crud-example-tutorial

 ds037622.mongolab.com:37622
 */

import java.net.UnknownHostException;
 
import com.journaldev.mongodb.model.User;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
 
public class MongoDBExample {
 
    public static void main(String[] args) throws UnknownHostException {
     
        User user = createUser();
        DBObject doc = createDBObject(user);
         
        MongoClient mongo = new MongoClient("ds037622.mongolab.com", 37622);
		
        DB db = mongo.getDB("code101");
        db.authenticate("dcfrancisco","@gina4462".toCharArray());
		
        DBCollection col = db.getCollection("users");
         
        //create user
        WriteResult result = col.insert(doc);
       	System.out.println("insert"); 
		System.out.println(result.getUpsertedId());
        System.out.println(result.getN());
        System.out.println(result.isUpdateOfExisting());
        System.out.println(result.getLastConcern());
         
        //read example
        DBObject query = BasicDBObjectBuilder.start().add("_id", user.getId()).get();
        DBCursor cursor = col.find(query);
        while(cursor.hasNext()){
            System.out.println(cursor.next());
        }
         
        //update example
        user.setName("Danny Francisco");
        doc = createDBObject(user);
        result = col.update(query, doc);
        System.out.println(result.getUpsertedId());
        System.out.println(result.getN());
        System.out.println(result.isUpdateOfExisting());
        System.out.println(result.getLastConcern());
         
        //delete example
        result = col.remove(query);
        System.out.println(result.getUpsertedId());
        System.out.println(result.getN());
        System.out.println(result.isUpdateOfExisting());
        System.out.println(result.getLastConcern());
       
        //close resources
        mongo.close();
    }
 
    private static DBObject createDBObject(User user) {
        BasicDBObjectBuilder docBuilder = BasicDBObjectBuilder.start();
                                 
        docBuilder.append("_id", user.getId());
        docBuilder.append("name", user.getName());
        docBuilder.append("role", user.getRole());
        docBuilder.append("isEmployee", user.isEmployee());
		docBuilder.append("status", user.getStatus());
		
        return docBuilder.get();
    }
 
    private static User createUser() {
        User u = new User();
        u.setId(7);
        u.setName("Danny");
        u.setEmployee(true);
        u.setRole("GUEST");
		u.setStatus("R");
        return u;
    }
     
     
 
}
