package utilClasses;

import java.io.Serializable;

/**
 * Wrapper class to store the tweets in json string
 * We implemented this as Tweet class provided by Twitter Kit is not Serializable
 */

public class MyTweet implements Serializable{
    public String tweet = "";
}
