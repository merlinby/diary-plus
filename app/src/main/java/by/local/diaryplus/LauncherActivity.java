package by.local.diaryplus;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;

public final class LauncherActivity extends Activity {
 @Override public void onCreate(Bundle state){super.onCreate(state);boolean second="2".equals(ProfileStore.read(getFilesDir(),"last-profile","1"));startActivity(new Intent(this,second?StudentTwoActivity.class:MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP));finish();}
}
