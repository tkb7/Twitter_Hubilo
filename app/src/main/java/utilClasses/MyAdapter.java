package utilClasses;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.CompactTweetView;
import com.twitter.sdk.android.tweetui.Timeline;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;

import net.azurewebsites.tarunbaria.twiiterdemo.R;
import net.azurewebsites.tarunbaria.twiiterdemo.TweetActivity;

import java.util.HashMap;

/**
 * List Adapter written for the Listview
 * This adapter extends the TweetTimelineListAdapter provided by Twitter Kit
 * As TweetTimelineListAdapter tweets open in webView on clicking the tweet
 * we have to extend it as the custom adapter
 */

public class MyAdapter extends TweetTimelineListAdapter {
    /**
     * Constructs a TweetTimelineListAdapter for the given Tweet Timeline.
     *
     * @param context  the context for row views.
     * @param timeline a Timeline&lt;Tweet&gt; providing access to Tweet data items.
     * @throws IllegalArgumentException if context is null
     */

    //Hashmap to store all the tweets which were modified
    public HashMap<Long, Tweet> changes;

    //Timeline for all tweets searched on twitter
    public Timeline<Tweet> searchTimeline;

    public MyAdapter(Context context, Timeline<Tweet> timeline, HashMap<Long, Tweet> changes) {
        super(context, timeline);
        this.changes = changes;
        this.searchTimeline = timeline;
    }

    //Customize the adapter to change the behaviour of the View
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        //Get the tweet which is being displayed by this view
        Tweet ctweet = getItem(position);

        //Check if that tweet is modified, if yes then show that tweet
        if(SharedData.changes.containsKey(ctweet.id)){
            ctweet = SharedData.changes.get(ctweet.id);
        }

        final Tweet tweet = ctweet;
        //Create CompactTweetView provided by Twitter Kit using the Tweet
        CompactTweetView cv = new CompactTweetView(context, ctweet, R.style.tw__TweetLightWithActionsStyle);

        //Add on click listener to the view to open the tweet in different activity
        cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Gson jsonConvertor = new Gson();
                String jsonTweet = jsonConvertor.toJson(tweet);
                Intent intent = new Intent(context, TweetActivity.class);
                intent.putExtra("tweet", jsonTweet);
                context.startActivity(intent);
            }
        });
        cv.setOnActionCallback(null);
        cv.setTweetLinkClickListener(null);
        return cv;
    }
}
