package net.azurewebsites.tarunbaria.twiiterdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.CompactTweetView;
import com.twitter.sdk.android.tweetui.internal.TweetMediaView;

import java.io.InputStream;

import utilClasses.SharedData;

/*
*   Activity to display the selected tweet and edit it
*   User can edit only the content of the tweet
 */

public class TweetActivity extends AppCompatActivity {

    TextView userName;
    TextView handleName;
    TextView tweetText;
    EditText editTweet;
    ImageView profilePicture;
    ProgressBar progressBar;
    LinearLayout retweetLayout;
    Menu menu;
    Tweet tweet;
    TweetMediaView mediaView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize the twitter object
        Twitter.initialize(this);
        setContentView(R.layout.activity_tweet);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //Bound views with the code
        userName = findViewById(R.id.userName);
        handleName = findViewById(R.id.handle);
        tweetText = findViewById(R.id.tweet);
        editTweet = findViewById(R.id.edit_tweet);
        profilePicture = findViewById(R.id.profilePicture);
        progressBar = findViewById(R.id.progressBar);
        retweetLayout = findViewById(R.id.retweet_layout);
        mediaView = findViewById(R.id.mediaView);

        Gson jsonConverter = new Gson();
        //Convert the selected tweet to Tweet object from the json
        this.tweet = jsonConverter.fromJson(getIntent().getStringExtra("tweet"), Tweet.class);

        //Check if this tweet is modified, if yes then show that tweet
        if(SharedData.changes.containsKey(tweet.id))
            this.tweet = SharedData.changes.get(this.tweet.id);

        //Set all the views according to the tweet
        userName.setText(tweet.user.name);
        handleName.setText("@"+tweet.user.screenName);
        tweetText.setText(tweet.text);
        editTweet.setText(tweet.text);

        //Download the profile image of the twitter
        new DownloadImage().execute(tweet.user.profileImageUrl);

        Tweet retweet = tweet.quotedStatus;
        //Display the retweet in the UI
        //Unable to edit
        if(retweet != null) {
            CompactTweetView cView = new CompactTweetView(this, retweet);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cView.setBackground(getDrawable(R.drawable.border));
            }
            retweetLayout.addView(cView);
        }
        //Toast.makeText(this, tweet.quotedStatus.text, Toast.LENGTH_SHORT).show();

        if(!tweet.entities.media.isEmpty()){
            TweetMediaView mediaView = findViewById(R.id.mediaView);
            mediaView.setTweetMediaEntities(tweet, tweet.entities.media);
        }

    }


    //Helper class to downlioad the Image from the Url using AsyncTask
    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(String... URL) {

            String imageURL = URL[0];

            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // Set the bitmap into ImageView
            profilePicture.setImageBitmap(result);
            // Close progressBar
            progressBar.setVisibility(View.GONE);
        }
    }


    //Menu rendering
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit,menu);
        this.menu = menu;
        return true;
    }

    //When user selects perticular menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.item_edit) {
            onEditMenuSelected();
            return true;
        }else if(id == R.id.item_save) {
            onSaveMenuSelected();
        }
        return super.onOptionsItemSelected(item);
    }

    //When user selects edit option then change the ui to let user edit the tweet
    private void onEditMenuSelected() {
        tweetText.setVisibility(View.GONE);
        editTweet.setText(tweetText.getText());
        editTweet.setVisibility(View.VISIBLE);
        mediaView.setVisibility(View.GONE);
    }

    //When user selcts save menu then save all the changes made to the tweet and store in to the global variable of SharedData class
    private void onSaveMenuSelected() {
        mediaView.setVisibility(View.VISIBLE);
        editTweet.setVisibility(View.GONE);
        tweetText.setText(editTweet.getText());
        tweetText.setVisibility(View.VISIBLE);

        //Create new tweet with all the changes
        this.tweet = new Tweet(tweet.coordinates, tweet.createdAt, tweet.currentUserRetweet, tweet.entities, tweet.extendedEntities, tweet.favoriteCount, tweet.favorited,
                tweet.filterLevel, tweet.id, tweet.idStr, tweet.inReplyToScreenName, tweet.inReplyToStatusId, tweet.inReplyToStatusIdStr, tweet.inReplyToUserId, tweet.inReplyToUserIdStr, tweet.lang,
                tweet.place, tweet.possiblySensitive, tweet.scopes, tweet.quotedStatusId, tweet.quotedStatusIdStr, tweet.quotedStatus, tweet.retweetCount, tweet.retweeted, tweet.retweetedStatus,
                tweet.source, tweetText.getText().toString(), tweet.displayTextRange, tweet.truncated, tweet.user, tweet.withheldCopyright, tweet.withheldInCountries, tweet.withheldScope, tweet.card);
        SharedData.changes.put(tweet.id, this.tweet);
    }
}
