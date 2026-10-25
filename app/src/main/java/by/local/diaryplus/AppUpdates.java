package by.local.diaryplus;

import java.io.*;
import java.net.*;
import org.json.*;

final class AppUpdates {
 private static final String API="https://api.github.com/repos/merlinby/diary-plus/releases/latest";
 // Returns the exact published APK URL, or null when the installed version is current.
 static String findDownload(String installed) throws IOException, JSONException {
  HttpURLConnection connection=(HttpURLConnection)new URL(API).openConnection();
  connection.setConnectTimeout(10000);
  connection.setReadTimeout(10000);
  connection.setInstanceFollowRedirects(false);
  connection.setRequestProperty("Accept","application/vnd.github+json");
  connection.setRequestProperty("User-Agent","DiaryPlus/"+installed);
  connection.setUseCaches(false);
  try {
   if(connection.getResponseCode()!=200)throw new IOException("Release lookup failed");
   ByteArrayOutputStream body=new ByteArrayOutputStream();
   try(InputStream in=connection.getInputStream()){
    byte[] buffer=new byte[8192];int n;
    while((n=in.read(buffer))!=-1){if(body.size()+n>1024*1024)throw new IOException("Release response too large");body.write(buffer,0,n);}
   }
   return downloadFromRelease(new JSONObject(body.toString("UTF-8")),installed);
  } finally {connection.disconnect();}
 }
 static String downloadFromRelease(JSONObject release,String installed) throws IOException, JSONException {
  if(release.getBoolean("draft")||release.getBoolean("prerelease"))throw new IOException("Not a stable release");
  String tag=release.getString("tag_name");
  ReleaseVersion latest=new ReleaseVersion(tag);
  if(latest.compareTo(new ReleaseVersion(installed))<=0)return null;
  String name="DiaryPlus-"+latest.name+".apk";
  String expected="https://github.com/merlinby/diary-plus/releases/download/"+tag+"/"+name;
  JSONArray assets=release.getJSONArray("assets");
  for(int i=0;i<assets.length();i++){
   JSONObject asset=assets.getJSONObject(i);
   if(name.equals(asset.optString("name"))&&"uploaded".equals(asset.optString("state"))&&asset.optLong("size")>0&&expected.equals(asset.optString("browser_download_url")))return expected;
  }
  throw new IOException("Release APK unavailable");
 }
}
