package comp.ubiquitous.saksham.stepcounter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "StepCounter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    /**
     * callback to inflate the options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_settings:
                Intent intent = new Intent(this, MotionSensorData.class);
                startActivity(intent);
                break;
            case R.id.menu_feedback:
//                intent = new Intent(this, FeedbackEmailPage.class);
//                startActivity(intent);
                break;
            case R.id.menu_share:
//            Intent shareAppIntent = new Intent(Intent.ACTION_SEND);
//            shareAppIntent.setType("text/plain");
//            shareAppIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_app_opening_subject));
//            shareAppIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_opening_text));
//
//            shareAppIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
//            startActivity(Intent.createChooser(shareAppIntent, "Send to friend"));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
