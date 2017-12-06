package net.azurewebsites.tarunbaria.twiiterdemo;

import android.app.NotificationManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.SearchService;
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TimelineResult;

import java.util.ArrayList;
import java.util.HashMap;

import utilClasses.ConnectivityReceiver;
import utilClasses.IO;
import utilClasses.MyAdapter;
import utilClasses.OfflineAdapter;
import utilClasses.SharedData;

public class SearchActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener{

    Button searchButton;
    EditText hashtagText;
    ListView lView;
    //Default hash tag to be searched on the twitter
    private String tag = "#modi";
    private IO fileIo;
    public static HashMap<Long, Tweet> changes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        changes = SharedData.changes;

        //Bound view with the code
        searchButton = (Button) findViewById(R.id.search_button);
        hashtagText = (EditText) findViewById(R.id.hash_tag);
        lView = findViewById(R.id.tweets);

        //Initialize the twitter instance using the token and secret
        TwitterCore twitter = TwitterCore.getInstance();
        SearchService searchService = twitter.getApiClient().getSearchService();

        this.fileIo = new IO(this);

        //Check for the network connekction
        if(isNetworkAvailable(this)){
            //If online then retrieve the tweets from the twitter
            tag = "mom";
            final MyAdapter[] pAdapter = {createAdapter(getTweetTimeline(tag), this.changes)};
            lView.setAdapter(pAdapter[0]);
            //write searched tweets to the file
            fileIo.writeTweetsToFile(pAdapter[0]);
        }else {
            //If offline then load tweets from the file
            ArrayList<Tweet> tweets = fileIo.readTweetsFromFile();
            if(tweets == null){
                lView.setAdapter(null);
            }else{
                OfflineAdapter adapter = new OfflineAdapter(this, tweets);
                lView.setAdapter(adapter);
            }
        }

        //Add Callback for the search button
        searchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //If network is not available then does nothing
                if(!isNetworkAvailable(SearchActivity.this)){
                    return;
                }

                //Search tweets from the twitter for the hashtag
                String data = hashtagText.getText().toString();
                if(!data.startsWith("#")){
                    data = "#"+data;
                }

                tag = data;
                MyAdapter lAdapter = createAdapter(getTweetTimeline(data), SearchActivity.changes);
                lView.setAdapter(lAdapter);
                //Write new tweets to the file
                fileIo.writeTweetsToFile(lAdapter);
            }
        });

        hashtagText.clearFocus();
    }

    protected void onPause() {

        super.onPause();
        //Write all tweets to the file
        //When app is being closed then the tweets will be stored before closing the app
        if(lView.getAdapter() != null) {
            fileIo.writeTweetsToFile(lView.getAdapter());
        }
    }

    //When activity resumes
    @Override
    protected void onResume() {

        super.onResume();

        //Write any changes made to the tweets to the file
        if(lView.getAdapter() != null)
            fileIo.writeTweetsToFile(lView.getAdapter());

        //Add broadcast listener to the activity
        MyApplication.getInstance().setConnectivityListener(this);

        //If network is available then retrieve tweets from the twitter
        //We have to do this as ListView won't reflect changes if the data source of the adapter won't change
        if(isNetworkAvailable(this)){
            final SearchTimeline searchTimeline = getTweetTimeline(tag);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MyAdapter mAdapter = createAdapter(searchTimeline, SearchActivity.changes);
                    mAdapter.refresh(new Callback<TimelineResult<Tweet>>() {
                        @Override
                        public void success(Result<TimelineResult<Tweet>> result) {

                        }

                        @Override
                        public void failure(TwitterException exception) {

                        }
                    });
                    lView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                }
            });
        }else {
            //If app is offline while resuming the activity load the tweets from the file
            ArrayList<Tweet> tweets = fileIo.readTweetsFromFile();
            if(tweets == null){
                lView.setAdapter(null);
            }else{
                OfflineAdapter adapter = new OfflineAdapter(this, tweets);
                lView.setAdapter(adapter);
            }
        }
    }

    //Write all tweets to the file while cloasing the app
    public void onDestroy() {
        super.onDestroy();
        fileIo.writeTweetsToFile(lView.getAdapter());
    }


    //helper method to create the Custom adapter using the search timeline
    public MyAdapter createAdapter(SearchTimeline timeline, HashMap<Long, Tweet> changes) {
        MyAdapter adapter = new MyAdapter(this, timeline, changes);
        return adapter;
    }

    //Helper method to retrieve the search timeline from the twitter
    public SearchTimeline getTweetTimeline(String hashtag) {
        SearchTimeline searchTimeline = new SearchTimeline.Builder().query(hashtag).build();
        return searchTimeline;
    }

    public boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    //Listener for the network state change broadcast
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if(isConnected){
            //Offline => Online
            //Display downloading icon in the notification bar
            NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

            mBuilder.setContentTitle("Downloading")
                    .setContentText("Downloading tweets from twitter")
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setTicker("")
                    .setProgress(0, 0, true);

            manager.notify(50202020, mBuilder.build());
            Toast.makeText(this, "Downloading Tweets "+ tag, Toast.LENGTH_SHORT).show();

            //Retrieve the tweets from the twitter and store them in the file
            SearchTimeline searchTimeline = getTweetTimeline(this.tag);
            MyAdapter adapter = createAdapter(searchTimeline, this.changes);
            lView.setAdapter(adapter);
            fileIo.writeTweetsToFile(lView.getAdapter());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {

            }

            //Remove the downloading icon from the notification bar
            manager.cancel(50202020);
        }else{
            //Online => Offline

            //Load tweets from the file
            Toast.makeText(this, "You are offline now", Toast.LENGTH_SHORT).show();
            ArrayList<Tweet> tweets = fileIo.readTweetsFromFile();
            if(tweets == null){
                lView.setAdapter(null);
                return;
            }
            OfflineAdapter adapter = new OfflineAdapter(this, tweets);
            lView.setAdapter(adapter);
        }
    }
}
