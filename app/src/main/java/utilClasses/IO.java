package utilClasses;

import android.content.Context;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.google.gson.Gson;
import com.twitter.sdk.android.core.models.Tweet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Special IO class written to store the tweets loaded before in file to access them in offline mode
 */

public class IO {
    //Name of the file to or from tweets should be transferred
    private final String FILE_NAME = "tweet_offline.data";
    private ObjectInputStream oIn;
    private ObjectOutputStream oOut;
    private Context context;

    //Constructor for the Class
    public IO(Context context) {
        this.context = context;

        //If file doesn't exist then create one
        File file = new File(context.getFilesDir(), this.FILE_NAME);
        if(!file.exists())
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    //Method to write the data of the adapter to the file
    public void writeTweetsToFile(ListAdapter adapter) {
        FileOutputStream fOs;
        try {
            fOs = context.openFileOutput(this.FILE_NAME, Context.MODE_PRIVATE);
            oOut = new ObjectOutputStream(fOs);
            Gson json = new Gson();

            if(adapter != null) {
                //Write number of objects in the adapter to the file
                oOut.writeInt(adapter.getCount());

                //Write all tweets to the file
                for(int i = 0; i < adapter.getCount(); i++){
                    MyTweet mtweet = new MyTweet();
                    Tweet t = (Tweet)adapter.getItem(i);

                    //If tweet is edited then save that tweet instead of original tweet
                    if(SharedData.changes.containsKey(t.id)) {
                        t = SharedData.changes.get(i);
                    }
                    //Store tweets in the JSON form
                    mtweet.tweet = json.toJson(t);
                    oOut.writeObject(mtweet);
                }
            }

            //Close all output streams
            oOut.close();
            fOs.close();
        } catch (IOException e) {
            return;
        }
    }

    //method to read all the tweets stored in the file
    public ArrayList<Tweet> readTweetsFromFile() {
        FileInputStream fIs;

        //Array List to store all the tweets read from the file
        ArrayList<Tweet> tweets = new ArrayList<>();

        Gson json = new Gson();
        try{
            fIs = context.openFileInput(this.FILE_NAME);

            //Read the count of objects stored in the file
            oIn = new ObjectInputStream(fIs);
            int count;

            //Read all tweets from the file
            if((count = oIn.readInt()) > 0) {
                for(int i = 0; i < count; i++) {
                    MyTweet myTweet = (MyTweet)oIn.readObject();
                    //Convert json string to the object
                    Tweet tweet = json.fromJson(myTweet.tweet, Tweet.class);
                    tweets.add(tweet);
                }
            }
            oIn.close();
            fIs.close();

            //Return the tweets
            return tweets;
        }catch (IOException e) {
            Toast.makeText(this.context, "Error in reading your tweets offline", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Toast.makeText(this.context, "Error in reading your tweets offline", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return null;
    }
}
