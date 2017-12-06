package utilClasses;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.CompactTweetView;

import java.util.ArrayList;

/**
 * Adapter written to display the tweets in the ListView
 * This adapter is used when the application is  in offline mode
 */

public class OfflineAdapter extends ArrayAdapter<Tweet>{
    private ArrayList<Tweet> tweets;
    private Context context;

    public OfflineAdapter(@NonNull Context context, ArrayList<Tweet> tweets) {
        super(context, 0, tweets);

        this.tweets = tweets;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //Get the tweets to be displayed by this view
        Tweet tweet = this.tweets.get(position);
        if(tweet == null)
            return null;

        //Check if that tweet is modified or not, if modified then use that tweet instead
        if(SharedData.changes.containsKey(tweet.id)) {
            tweet = SharedData.changes.get(tweet.id);
            Toast.makeText(context, "Holla", Toast.LENGTH_SHORT).show();
            Toast.makeText(context, tweet.text, Toast.LENGTH_SHORT).show();
        }

        //create the compacttweetview using the tweet and return the view
        CompactTweetView tweetView = new CompactTweetView(this.context, tweet);
        return tweetView;
    }
}
