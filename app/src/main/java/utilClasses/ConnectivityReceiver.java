package utilClasses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import net.azurewebsites.tarunbaria.twiiterdemo.MyApplication;

//Broadcast reciever to receive broadcast for internet state change
public class ConnectivityReceiver
        extends BroadcastReceiver {

    //Keep track of all listeners
    public static ConnectivityReceiverListener connectivityReceiverListener;

    //Constructor for the Class
    public ConnectivityReceiver() {
        super();
    }

    //This method is called everytime the state of connectivity changes
    @Override
    public void onReceive(Context context, Intent arg1) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        //get the status of network connectivity
        boolean isConnected = activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();

        //Notify all listeners about the change
        if (connectivityReceiverListener != null) {
            connectivityReceiverListener.onNetworkConnectionChanged(isConnected);
        }
    }

    //Method to check the status of network connectivity
    public static boolean isConnected() {
        ConnectivityManager
                cm = (ConnectivityManager) MyApplication.getInstance().getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
    }

    //Interface to be implemented by activities
    public interface ConnectivityReceiverListener {
        void onNetworkConnectionChanged(boolean isConnected);
    }
}
