package utilClasses;

import com.twitter.sdk.android.core.models.Tweet;

import java.util.HashMap;

/**
 * Class created to store the Global Variable which is accessible throught the application
 */

public class SharedData {
    public static HashMap<Long, Tweet>  changes = new HashMap<>();
}
