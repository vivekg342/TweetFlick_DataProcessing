/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Trends;

import Trends.DataStructures.DBFreq;
import Trends.DataStructures.DBWord;
import Trends.DataStructures.Freq;
import Utilities.SettingsMap;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoURI;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bson.types.ObjectId;

/**
 *
 * @author Karthik
 */
public class DBAdapter {
	static MongoURI uri;
    static DB db;
    static {
    	 uri = new MongoURI(SettingsMap.get("DB_URI"));
	try {
		db = uri.connectDB();
		db.authenticate(uri.getUsername(), uri.getPassword());
	} catch (MongoException e) {
		e.printStackTrace();
	} catch (UnknownHostException e) {
		e.printStackTrace();
	}
    }
  public static  ArrayList<DBWord> readDB() {
        ArrayList<DBWord> trends = new ArrayList<DBWord>();
        try {            
            DBCollection coll = db.getCollection(SettingsMap.get("DB_TRENDS_COLL"));
            DBCursor cur = coll.find();
            while (cur.hasNext()) {
                DBObject trendingWord = cur.next();
                String word = (String) trendingWord.get("word");
                int totalScore = (Integer) trendingWord.get("totalScore");
                List<DBObject> historyDb = (List<DBObject>) trendingWord.get("history");
                LinkedList<DBFreq> history = new LinkedList<DBFreq>();
                for (DBObject dbfreqObj : historyDb) {
                    int limit = (Integer) dbfreqObj.get("limit");
                    DBObject freqObj = (DBObject) dbfreqObj.get("freq");
                    int cc = (Integer) freqObj.get("cc");
                    int fc = (Integer) freqObj.get("fc");
                    int sc = (Integer) freqObj.get("sc");

                    DBFreq freq = new DBFreq(cc, fc,sc,limit);
                    history.add(freq);
                }
                trends.add(new DBWord(word, totalScore, history));
            }
        } catch (Exception e) {
            System.out.println("Ex in DBAdapter:" + e);
        } finally {
           // mongo.close();
        }
        return trends;

    }

   static void updateTrends( ArrayList<DBWord> trends) {
       try {            

            DBCollection coll = db.getCollection(SettingsMap.get("DB_TRENDS_COLL"));
            coll.rename(SettingsMap.get("DB_TRENDS_COLL_BCK"), true);
            DBCollection newColl = db.getCollection(SettingsMap.get("DB_TRENDS_COLL"));
            BasicDBObject indexes = new BasicDBObject();
            indexes.put("word", 1);
            indexes.put("score", -1);
            newColl.ensureIndex(indexes);
//            newColl.putAll(celebs);
//            ArrayList<DBObject> celebsArr = new ArrayList<DBObject>();
//            celebsArr.addAll(celebsDb.values());

//            newColl.insert(trends);
//            System.out.println("updated celebs:" + celebsArr.size());

        } catch (Exception e) {

            System.out.println("ex in updateCelebs:" + e);
        } finally {
            //uri.close();
        }
    }
}
