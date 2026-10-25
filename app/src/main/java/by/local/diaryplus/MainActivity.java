package by.local.diaryplus;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
 private static final String HOME="https://diary.e-schools.by/";
 private WebView web;
 private ProgressBar progress;
 private TextView status;
 private ValueCallback<Uri[]> files;
 private String analytics;
 private String loginScript;
 private boolean failed;
 private Button analysisButton;
 private Button profileButton;
 private boolean authRunning=false,foreground=false,autofillDeclined=false;
 private Runnable credentialAction;
 private int profileId(){return this instanceof StudentTwoActivity?2:1;}
 private String profileName(int id){return ProfileStore.read(getFilesDir(),"profile-name-"+id,"Ученик "+id);}
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 private boolean official(Uri u){String h=u.getHost();return "https".equalsIgnoreCase(u.getScheme())&&h!=null&&(h.equalsIgnoreCase("e-schools.by")||h.toLowerCase(java.util.Locale.ROOT).endsWith(".e-schools.by"))&&(u.getPort()==-1||u.getPort()==443);}
 private boolean diary(String u){if(u==null)return false;Uri p=Uri.parse(u);return "https".equals(p.getScheme())&&"diary.e-schools.by".equals(p.getHost())&&(p.getPort()==-1||p.getPort()==443);}
 private Button button(String label,LinearLayout row){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(14);b.setMinWidth(0);b.setMinimumWidth(0);row.addView(b,new LinearLayout.LayoutParams(0,dp(48),1));return b;}
 @Override public void onCreate(Bundle saved){
  super.onCreate(saved);
  try(InputStream in=getAssets().open("analytics.js")){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);analytics=out.toString("UTF-8");}catch(IOException e){analytics=null;}
  try(InputStream in=getAssets().open("login.js")){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[4096];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);loginScript=out.toString("UTF-8");}catch(IOException e){loginScript=null;}
  LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(Color.WHITE);
  if(Build.VERSION.SDK_INT>=30)root.setOnApplyWindowInsetsListener((v,i)->{android.graphics.Insets x=i.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout()|WindowInsets.Type.ime());v.setPadding(x.left,x.top,x.right,x.bottom);return i;});
  LinearLayout profileRow=new LinearLayout(this);profileRow.setPadding(dp(8),0,dp(8),0);root.addView(profileRow);
  profileButton=button(profileName(profileId())+"  ⇄",profileRow);profileButton.setTextColor(Color.rgb(37,99,235));profileButton.setOnClickListener(v->chooseProfile());
  LinearLayout top=new LinearLayout(this);top.setPadding(dp(8),0,dp(8),0);root.addView(top);
  button("Дневник",top).setOnClickListener(v->web.loadUrl(HOME));
  button("Обновить",top).setOnClickListener(v->web.reload());
  Button menu=button("Ещё ⋮",top);menu.setOnClickListener(v->showMenu(menu));
  status=new TextView(this);status.setText("diary.e-schools.by");status.setTextSize(11);status.setPadding(dp(16),0,dp(16),dp(4));status.setTextColor(Color.rgb(85,98,118));root.addView(status);
  progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);root.addView(progress,new LinearLayout.LayoutParams(-1,dp(3)));
  web=new WebView(this);root.addView(web,new LinearLayout.LayoutParams(-1,0,1));
  LinearLayout bottom=new LinearLayout(this);bottom.setPadding(dp(12),dp(3),dp(12),dp(5));root.addView(bottom);
  analysisButton=button("▥  Аналитика оценок",bottom);analysisButton.setTextColor(Color.rgb(37,99,235));analysisButton.setOnClickListener(v->openAnalytics());
  getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
  setContentView(root);
  WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(false);s.setAllowContentAccess(true);s.setAllowFileAccessFromFileURLs(false);s.setAllowUniversalAccessFromFileURLs(false);s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);s.setSafeBrowsingEnabled(true);s.setSupportMultipleWindows(true);s.setJavaScriptCanOpenWindowsAutomatically(false);s.setBuiltInZoomControls(true);s.setDisplayZoomControls(false);s.setLoadWithOverviewMode(true);s.setUseWideViewPort(true);
  CookieManager.getInstance().setAcceptCookie(true);CookieManager.getInstance().setAcceptThirdPartyCookies(web,false);
  web.setWebViewClient(new WebViewClient(){
   @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){if(!r.isForMainFrame())return false;if(official(r.getUrl()))return false;external(r.getUrl());return true;}
   @Override public void onPageStarted(WebView v,String url,android.graphics.Bitmap icon){failed=false;autofillDeclined=false;progress.setVisibility(View.VISIBLE);analysisButton.setEnabled(false);Uri u=Uri.parse(url);status.setText(u.getHost()==null?"Дневник":u.getHost());}
   @Override public void onPageFinished(WebView v,String url){progress.setVisibility(View.GONE);analysisButton.setEnabled(diary(url)&&!failed);CookieManager.getInstance().flush();fillSavedLogin();}
   @Override public void onReceivedError(WebView v,WebResourceRequest r,WebResourceError e){if(r.isForMainFrame()){failed=true;status.setText("Не удалось открыть дневник. Проверьте интернет и нажмите «Обновить».");}}
   @Override public void onReceivedHttpError(WebView v,WebResourceRequest r,WebResourceResponse e){if(r.isForMainFrame()&&e.getStatusCode()>=400){failed=true;status.setText("Сайт ответил HTTP "+e.getStatusCode()+". Попробуйте обновить позже.");}}
  });
  web.setWebChromeClient(new WebChromeClient(){
   @Override public void onProgressChanged(WebView v,int p){progress.setProgress(p);}
   @Override public boolean onShowFileChooser(WebView v,ValueCallback<Uri[]> cb,FileChooserParams params){if(!official(Uri.parse(v.getUrl()==null?"":v.getUrl())))return false;if(files!=null)files.onReceiveValue(null);files=cb;try{Intent intent=params.createIntent();intent.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(intent,71);}catch(ActivityNotFoundException e){files.onReceiveValue(null);files=null;Toast.makeText(MainActivity.this,"Не найдено приложение выбора файлов",Toast.LENGTH_LONG).show();}return true;}
   @Override public boolean onCreateWindow(WebView view,boolean dialog,boolean gesture,Message msg){if(!gesture)return false;WebView popup=new WebView(MainActivity.this);popup.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){Uri u=r.getUrl();if(official(u))web.loadUrl(u.toString());else external(u);v.destroy();return true;}});((WebView.WebViewTransport)msg.obj).setWebView(popup);msg.sendToTarget();return true;}
  });
  web.setDownloadListener((url,ua,cd,mime,length)->download(url,ua,cd,mime));
  try{ProfileStore.write(getFilesDir(),"last-profile",String.valueOf(profileId()));}catch(IOException ignored){}
  web.loadUrl(HOME);
 }
 private String savedLogin(){return ProfileStore.read(getFilesDir(),"profile-login-"+profileId(),"");}
 private void authenticate(Runnable success){
  if(authRunning)return;
  android.app.KeyguardManager kg=(android.app.KeyguardManager)getSystemService(KEYGUARD_SERVICE);
  if(!kg.isDeviceSecure()){new AlertDialog.Builder(this).setTitle("Настройте блокировку телефона").setMessage("Для сохранения пароля задайте PIN, пароль или графический ключ в настройках телефона.").setPositiveButton(CredentialsVault.any(this)?"Удалить сохранённые пароли":"Понятно",(d,w)->{if(CredentialsVault.any(this))try{CredentialsVault.delete(this,1);CredentialsVault.delete(this,2);fillSavedLogin();}catch(IOException ignored){finish();}}).show();return;}
  authRunning=true;
  if(Build.VERSION.SDK_INT<30){credentialAction=success;startActivityForResult(kg.createConfirmDeviceCredentialIntent("Дневник +","Подтвердите вход"),72);return;}
  android.hardware.biometrics.BiometricPrompt prompt=new android.hardware.biometrics.BiometricPrompt.Builder(this).setTitle("Дневник +").setSubtitle("Подтвердите доступ к дневнику").setAllowedAuthenticators(android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG|android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL).build();
  prompt.authenticate(new android.os.CancellationSignal(),getMainExecutor(),new android.hardware.biometrics.BiometricPrompt.AuthenticationCallback(){
   @Override public void onAuthenticationSucceeded(android.hardware.biometrics.BiometricPrompt.AuthenticationResult result){authRunning=false;autofillDeclined=false;success.run();}
   @Override public void onAuthenticationError(int code,CharSequence text){authRunning=false;autofillDeclined=true;Toast.makeText(MainActivity.this,"Подтверждение отменено",Toast.LENGTH_SHORT).show();}
  });
 }
 private void injectCredentials(String password){if(loginScript==null||!diary(web.getUrl()))return;try{org.json.JSONObject data=new org.json.JSONObject();data.put("username",savedLogin());data.put("password",password);web.evaluateJavascript(loginScript.replace("__SAVED_LOGIN_JSON__",data.toString()),null);}catch(org.json.JSONException ignored){}}
 private void fillSavedLogin(){if(loginScript==null||!diary(web.getUrl())||!foreground||authRunning)return;
  if(!CredentialsVault.has(this,profileId())){injectCredentials("");return;}
  // Inspect the actual form before touching the authentication-bound key.
  web.evaluateJavascript(loginScript.replace("__SAVED_LOGIN_JSON__","{\"inspect\":true}"),raw->{if(!diary(web.getUrl())||!foreground||authRunning)return;
   try{Object result=new org.json.JSONTokener(raw).nextValue();if(!(result instanceof org.json.JSONObject)){injectCredentials("");return;}org.json.JSONObject form=(org.json.JSONObject)result;String username=form.optString("username","");if(!form.optBoolean("passwordNeeded")||(!username.isEmpty()&&!username.equals(savedLogin()))||autofillDeclined){injectCredentials("");return;}}
   catch(org.json.JSONException e){return;}
   try{injectCredentials(CredentialsVault.load(this,profileId(),savedLogin()));}catch(android.security.keystore.UserNotAuthenticatedException e){authenticate(()->fillSavedLogin());}catch(Exception e){injectCredentials("");Toast.makeText(this,"Сохранённый пароль недоступен. Укажите его заново в меню входа.",Toast.LENGTH_LONG).show();}
  });
 }
 private void editLogin(){if(CredentialsVault.has(this,profileId())){authenticate(()->loginDialog(savedLogin()));return;}String existing=savedLogin();if(!existing.isEmpty()||loginScript==null||!diary(web.getUrl())){loginDialog(existing);return;}web.evaluateJavascript(loginScript.replace("__SAVED_LOGIN_JSON__","null"),raw->{String candidate="";try{Object v=new org.json.JSONTokener(raw).nextValue();if(v instanceof String)candidate=(String)v;}catch(Exception ignored){}if(!isFinishing())loginDialog(candidate);});}
 private void loginDialog(String value){
  LinearLayout form=new LinearLayout(this);form.setOrientation(1);form.setPadding(dp(20),0,dp(20),0);
  EditText input=new EditText(this);input.setSingleLine(true);input.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(120)});input.setText(value);input.setHint("Логин ученика");form.addView(input);
  CheckBox remember=new CheckBox(this);remember.setText("Запоминать пароль");remember.setChecked(CredentialsVault.has(this,profileId()));form.addView(remember);
  EditText password=new EditText(this);password.setSingleLine(true);password.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);password.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(256)});password.setHint("Пароль");password.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);form.addView(password);password.setVisibility(remember.isChecked()?View.VISIBLE:View.GONE);
  if(remember.isChecked())try{password.setText(CredentialsVault.load(this,profileId(),value));}catch(Exception e){Toast.makeText(this,"Введите пароль заново",Toast.LENGTH_SHORT).show();}
  remember.setOnCheckedChangeListener((b,checked)->password.setVisibility(checked?View.VISIBLE:View.GONE));
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Вход · "+profileName(profileId())).setMessage("Логин сохраняется для этого ученика. Пароль — только по флажку, с шифрованием и подтверждением блокировкой телефона. Снятие флажка удаляет сохранённый пароль.").setView(form).setPositiveButton("Сохранить",null).setNeutralButton("Скопировать логин",(d,w)->{((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Логин дневника",input.getText().toString()));}).setNegativeButton("Отмена",null).create();
  dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String login=input.getText().toString().trim(),pass=password.getText().toString();if(remember.isChecked()&&(login.isEmpty()||pass.isEmpty())){Toast.makeText(this,"Укажите логин и пароль",Toast.LENGTH_SHORT).show();return;}Runnable save=()->{try{if(remember.isChecked())CredentialsVault.save(this,profileId(),login,pass);else CredentialsVault.delete(this,profileId());ProfileStore.write(getFilesDir(),"profile-login-"+profileId(),login);dialog.dismiss();fillSavedLogin();Toast.makeText(this,"Настройки входа сохранены",Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(this,"Не удалось сохранить пароль. Повторите подтверждение или сохраните только логин.",Toast.LENGTH_LONG).show();}};if(remember.isChecked())authenticate(save);else save.run();}));dialog.show();
 }
 private void chooseProfile(){String[] names={profileName(1),profileName(2),"Переименовать текущий профиль"};new AlertDialog.Builder(this).setTitle("Выберите ученика").setItems(names,(d,which)->{if(which==2){renameProfile();return;}int id=which+1;if(id==profileId())return;CookieManager.getInstance().flush();startActivity(new Intent(this,id==2?StudentTwoActivity.class:MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP));finish();}).setNegativeButton("Отмена",null).show();}
 private void renameProfile(){EditText input=new EditText(this);input.setSingleLine(true);input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(40)});input.setText(profileName(profileId()));input.setSelectAllOnFocus(true);new AlertDialog.Builder(this).setTitle("Имя ученика").setView(input).setPositiveButton("Сохранить",(d,w)->{String name=input.getText().toString().trim();if(name.isEmpty())name="Ученик "+profileId();try{ProfileStore.write(getFilesDir(),"profile-name-"+profileId(),name);profileButton.setText(name+"  ⇄");}catch(IOException e){Toast.makeText(this,"Не удалось сохранить имя",Toast.LENGTH_SHORT).show();}}).setNegativeButton("Отмена",null).show();}
 private void openAnalytics(){if(!diary(web.getUrl())){Toast.makeText(this,"Откройте дневник и войдите в аккаунт",Toast.LENGTH_LONG).show();return;}if(analytics==null){Toast.makeText(this,"Не удалось загрузить модуль аналитики",Toast.LENGTH_LONG).show();return;}web.evaluateJavascript(analytics,null);}
 private void external(Uri u){String scheme=u.getScheme();if(!("https".equalsIgnoreCase(scheme)||"mailto".equalsIgnoreCase(scheme)||"tel".equalsIgnoreCase(scheme))){Toast.makeText(this,"Эта ссылка не поддерживается",Toast.LENGTH_SHORT).show();return;}try{startActivity(new Intent(Intent.ACTION_VIEW,u));}catch(ActivityNotFoundException e){Toast.makeText(this,"Нет приложения для этой ссылки",Toast.LENGTH_SHORT).show();}}
 private void download(String url,String ua,String cd,String mime){Uri u=Uri.parse(url);if(!official(u)){external(u);return;}String name=URLUtil.guessFileName(url,cd,mime).replaceAll("[\\\\/:*?\"<>|]","_");new AlertDialog.Builder(this).setTitle("Скачать файл?").setMessage(name).setPositiveButton("Скачать",(d,w)->{try{DownloadManager.Request r=new DownloadManager.Request(u);r.setTitle(name);r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,name);String cookie=CookieManager.getInstance().getCookie(url);if(cookie!=null)r.addRequestHeader("Cookie",cookie);r.addRequestHeader("User-Agent",ua);((DownloadManager)getSystemService(DOWNLOAD_SERVICE)).enqueue(r);Toast.makeText(this,"Загрузка началась",Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(this,"Не удалось скачать. Попробуйте открыть в браузере.",Toast.LENGTH_LONG).show();}}).setNegativeButton("Отмена",null).show();}
 private void showMenu(View anchor){PopupMenu p=new PopupMenu(this,anchor);p.getMenu().add("Вход и пароль");p.getMenu().add("Открыть в браузере");p.getMenu().add("Очистить вход и данные");p.getMenu().add("О приложении");p.setOnMenuItemClickListener(item->{String t=item.getTitle().toString();if(t.equals("Вход и пароль"))editLogin();else if(t.equals("Открыть в браузере"))external(Uri.parse(diary(web.getUrl())?web.getUrl():HOME));else if(t.equals("Очистить вход и данные"))new AlertDialog.Builder(this).setTitle("Выйти из текущего профиля?").setMessage("Будут удалены вход и локальные данные только текущего ученика. Второй профиль и сохранённые логины сохранятся.").setPositiveButton("Очистить",(d,w)->{web.stopLoading();web.loadUrl("about:blank");WebStorage.getInstance().deleteAllData();web.clearCache(true);web.clearHistory();CookieManager.getInstance().removeAllCookies(ok->{CookieManager.getInstance().flush();web.loadUrl(HOME);});}).setNegativeButton("Отмена",null).show();else new AlertDialog.Builder(this).setTitle("Дневник + · 0.3.1").setMessage("Неофициальная тестовая оболочка для diary.e-schools.by.\n\nПолный сайт дневника и локальный расчёт средних оценок. Серверов разработчика, рекламы и телеметрии в оболочке нет. Сам сайт работает по своим правилам.\n\nДва профиля учеников хранят отдельные сессии на телефоне. В меню можно сохранить логин и, по желанию, зашифрованный пароль с подтверждением блокировкой телефона только при доступе к паролю. Возврат в уже открытый дневник не требует подтверждения. Переключение — кнопкой вверху. Аналитика использует сессию только для запросов к дневнику. Средний балл не заменяет итоговую оценку учителя.\n\nPush-уведомления не реализованы. Вход и вложения нужно проверить с реальным аккаунтом.").setPositiveButton("Понятно",null).show();return true;});p.show();}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==72){authRunning=false;Runnable action=credentialAction;credentialAction=null;if(result==RESULT_OK){autofillDeclined=false;if(action!=null)action.run();}else autofillDeclined=true;return;}if(request==71&&files!=null){files.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result,data));files=null;}}
 @Override public void onBackPressed(){if(diary(web.getUrl()))web.evaluateJavascript("(function(){var e=document.getElementById('diary-plus-analytics');if(e){e.dispatchEvent(new Event('diary-close'));return true;}return false;})()",value->{if(!"true".equals(value))goBack();});else goBack();}
 private void goBack(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
 @Override protected void onPause(){super.onPause();foreground=false;if(loginScript!=null&&diary(web.getUrl()))web.evaluateJavascript(loginScript.replace("__SAVED_LOGIN_JSON__","null"),null);CookieManager.getInstance().flush();web.onPause();}
 @Override protected void onResume(){super.onResume();foreground=true;if(web!=null){web.onResume();fillSavedLogin();}}
 @Override protected void onDestroy(){if(files!=null){files.onReceiveValue(null);files=null;}if(web!=null){web.stopLoading();web.destroy();}super.onDestroy();}
}
