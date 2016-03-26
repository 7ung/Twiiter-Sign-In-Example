package tungho.android.example;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import com.twitter.sdk.android.tweetui.TweetUi;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import tungho.android.example.databinding.ActivityMainBinding;

/*
* Created by Ho Hoang Tung on 25/03/2016
*
**/
public class MainActivity extends Activity
    implements OnClickListener{

    TwitterLoginButton loginButton;

    TwitterSession session;
    TwitterAuthToken authToken;

    TwitterCore core;
    TweetUi tweetUi;
    TweetComposer composer;

    ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        final TwitterAuthConfig authConfig =  new TwitterAuthConfig(
                Constants.TWITTER_CONSUMER_KEY,
                Constants.TWITTER_CONSUMER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        updateUI(false);
        // Now, you can access Twitter's service from three following object
        core = Twitter.getInstance().core;
        //OR    core = TwitterCore.getInstance(); // both are ok.

        tweetUi = Twitter.getInstance().tweetUi;
        //OR    tweetUi = TweetUi.getInstance();

        composer = Twitter.getInstance().tweetComposer;
        //OR composer = TweetComposer.getInstance();

        loginButton = (TwitterLoginButton) findViewById(R.id.login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // connect success. Try getting infomation from Twitter.
                // Kết nối thành công. Giờ ta có thể lấy dữ liệu.
                Toast.makeText(getApplicationContext(),
                        "Connect success. User name is " + result.data.getUserName() , Toast.LENGTH_LONG )
                        .show();

                // you can get TwitterSession and TwitterAuthToken for using later.
                session = result.data;
                authToken = session.getAuthToken();

                // some info you can get from session
                String username = session.getUserName();
                long userId = session.getUserId();
                // => MORE: https://docs.fabric.io/javadocs/twittercore/1.3.4/com/twitter/sdk/android/core/TwitterSession.html

                // some info you can get from authToken
                String secret = authToken.secret;
                boolean isExpired = authToken.isExpired();
                // => MORE: https://docs.fabric.io/javadocs/twitter-core/1.5.0/com/twitter/sdk/android/core/TwitterAuthToken.html


                // updateUI
                binding.setUserName(result.data.getUserName());
                updateUI(true);
            }

            @Override
            public void failure(TwitterException e) {
                // do something when connection failed
                Toast.makeText(getApplicationContext(),
                        "Connection failed", Toast.LENGTH_LONG)
                        .show();
                Log.d("ConnectionFailure", "Msg: " + e.getMessage());
            }
        });

        findViewById(R.id.logout_button).setOnClickListener(this);
        findViewById(R.id.tweet_button).setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        loginButton.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.logout_button){
            core.logOut();
            updateUI(false);
        }
        else if (v.getId() == R.id.tweet_button){
            core.getApiClient().getStatusesService().update(
                    ((EditText) findViewById(R.id.tweet_txt)).getText().toString(), // text status to tweet
                    null, // Long - inReplyToStatusId. Your status will appear in reply to status has this Id.
                    null, // Boolean - possibly Sensitive content. set true if your content is nude, violent ...    // Nội dung nhạy cảm
                    null, // Double - latitude of your Geo-location. value is between -90 and 90.       // Vĩ độ
                    null, // Double - longitude of your Geo-location. value is between -180 and 180     // Kinh độ
                    null, // String - place id in the world
                    null, // Boolean - displaycoordinate. set true if you want people know where example are you in
                    null, // Boolean - trimUser. set true you will receive only user id in user information of return result. Otherwise, your will receive complete object
                    new Callback<Tweet>() {
                        @Override
                        public void success(Result<Tweet> result) {
                            Toast.makeText(getApplicationContext(),
                                    "Tweet successed", Toast.LENGTH_LONG)
                                    .show();
                        }

                        @Override
                        public void failure(TwitterException e) {
                            Toast.makeText(getApplicationContext(),
                                    "Tweet failed", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
            );
            // All of null value setted above are in placed in optional parameter.
        }
    }


    // note: I am using find all view by tag and data-binding to handle updateUI
    // it may be little confuse, dont worry just ignore them and handle updateUI in the common way you used
    private void updateUI(boolean loggedIn){
        // false mean logged out. or not have logged in yet.
        // true mean logged in.
        if (loggedIn){
            for (View v:
                 getViewsByTag((FrameLayout)findViewById(R.id.root),"Logged In")) {
                v.setVisibility(View.VISIBLE);
            }
            for (View v:
                    getViewsByTag((FrameLayout)findViewById(R.id.root),"Logged Out")) {
                v.setVisibility(View.GONE);
            }
        }
        else{
            for (View v:
                    getViewsByTag((FrameLayout)findViewById(R.id.root),"Logged In")) {
                v.setVisibility(View.GONE);
            }
            for (View v:
                    getViewsByTag((FrameLayout)findViewById(R.id.root),"Logged Out")) {
                v.setVisibility(View.VISIBLE);
            }
        }
    }
    private static ArrayList<View> getViewsByTag(ViewGroup root, String tag){
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }
}
