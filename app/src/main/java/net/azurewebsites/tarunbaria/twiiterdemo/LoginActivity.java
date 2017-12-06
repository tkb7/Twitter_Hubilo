package net.azurewebsites.tarunbaria.twiiterdemo;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import utilClasses.ConnectivityReceiver;

/*
 * Login Activity for all the activities related to user authentication
 * I am using Twitter Kit to authenticate the user with twitter using the OAuth token
  */
public class LoginActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private TwitterLoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Check if the network is available or not
        if(!isNetworkAvailable(this)){

            //Open search activity to dia[lay all tweets stored in file
            Toast.makeText(this, "Your app will work in offline mode", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getBaseContext(), SearchActivity.class);
            startActivity(intent);
        }

        //Initialize the twitter app using the consumer key and consumer scret
        Twitter.initialize(this);
        setContentView(R.layout.activity_login);

        //Bound the twitter login button with the java variable
        loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);

        //Add callback method for the twitter login button
        loginButton.setCallback(new Callback<TwitterSession>() {

            //Run when the login is successful
            //Start the search activity
            @Override
            public void success(Result<TwitterSession> result) {
                Intent myIntent = new Intent(getBaseContext(), SearchActivity.class);
                startActivity(myIntent);
            }

            //Method to be executed when there is an error in authentication
            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(LoginActivity.this, "Error in authentication", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Pass the result of activity back to the twitter login buttion
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the Twitter login button.
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    //Helper method to check the status of network connectivity
    public boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    //Listener for Broadcast Reciever
    //This method is called by onReceive method of the BroadCaster
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if(!isConnected) {
            //If app is offline then redirect user to search activity to work in offline mode
            Toast.makeText(this, "Your app will work in offline mode", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getBaseContext(), SearchActivity.class);
            startActivity(intent);
        }else {
            //If user is logged in and session is active then redirect the user to the search activity
            TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
            if(session != null && !session.getAuthToken().isExpired()) {
                Toast.makeText(this, "You are logged in", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getBaseContext(), SearchActivity.class);
                startActivity(intent);
            }
        }
    }
}
