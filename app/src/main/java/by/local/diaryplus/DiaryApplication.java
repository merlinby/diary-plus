package by.local.diaryplus;
import android.app.Application;
import android.webkit.WebView;

public final class DiaryApplication extends Application {
 @Override public void onCreate(){
  super.onCreate();
  // Must run before ANY android.webkit API in this process.
  // First profile retains the original default WebView directory for upgrades.
  if(getProcessName().endsWith(":student2"))WebView.setDataDirectorySuffix("student2");
 }
}
