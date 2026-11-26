package org.tekla.jesusprayer;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.print.PrintManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONObject;
import java.util.Calendar;

public class MainActivity extends Activity {
    private WebView web;
    private boolean checkingUpdate=false;
    private static final String RELEASE_API="https://api.github.com/repos/avataklakatana20252026-oss/ST.Takla-YouthMeeting/releases/latest";
    private static final String DOWNLOAD_PAGE="https://avataklakatana20252026-oss.github.io/ST.Takla-YouthMeeting/";
    public static final String CHANNEL="notety_daily";
    @Override public void onCreate(Bundle b){super.onCreate(b);
        getWindow().setStatusBarColor(0xFF350A17);getWindow().setNavigationBarColor(0xFF350A17);
        if(Build.VERSION.SDK_INT>=26){getWindow().getDecorView().setSystemUiVisibility(0);NotificationChannel c=new NotificationChannel(CHANNEL,"تذكيراتي الروحية",NotificationManager.IMPORTANCE_DEFAULT);c.setDescription("رسالة اليوم والتنبيهات الروحية");getSystemService(NotificationManager.class).createNotificationChannel(c);}
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},29);
        web=new WebView(this);web.setBackgroundColor(0xFF350A17);web.setWebViewClient(new WebViewClient(){@Override public void onPageFinished(WebView v,String url){super.onPageFinished(v,url);openFastIntent();}});web.setWebChromeClient(new WebChromeClient());
        WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(true);s.setAllowFileAccessFromFileURLs(true);s.setAllowUniversalAccessFromFileURLs(false);s.setDefaultTextEncodingName("UTF-8");s.setTextZoom(100);
        web.addJavascriptInterface(new Bridge(),"Android");setContentView(web);web.loadUrl("file:///android_asset/index.html");scheduleAll(this);
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(()->checkUpdate(false),4000);
    }
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);openFastIntent();}
    private void openFastIntent(){Intent i=getIntent();if(web==null||i==null)return;if(i.getBooleanExtra("annualNotice",false)){web.evaluateJavascript("if(typeof openAnnualNotice==='function'){openAnnualNotice();true}else{false}",result->{if("true".equals(result))i.removeExtra("annualNotice");});return;}String period=i.getStringExtra("fastPeriod"),date=i.getStringExtra("fastDate");if(period==null||date==null||!date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")||!(period.equals("morning")||period.equals("evening")))return;web.evaluateJavascript("if(typeof openFastNotice==='function'){openFastNotice("+JSONObject.quote(date)+","+JSONObject.quote(period)+");true}else{false}",result->{if("true".equals(result)){i.removeExtra("fastPeriod");i.removeExtra("fastDate");}});}
    private void checkUpdate(boolean manual){
        if(isFinishing()||isDestroyed()||checkingUpdate)return;
        android.content.SharedPreferences prefs=getSharedPreferences("notety_update",MODE_PRIVATE);
        if(!manual && System.currentTimeMillis()-prefs.getLong("checked",0)<86400000L)return;
        checkingUpdate=true;
        new Thread(()->{
            int available=0; boolean ok=false;
            java.net.HttpURLConnection conn=null;
            try{
                conn=(java.net.HttpURLConnection)new java.net.URL(RELEASE_API).openConnection();
                conn.setConnectTimeout(8000);conn.setReadTimeout(8000);conn.setInstanceFollowRedirects(false);
                conn.setRequestProperty("Accept","application/vnd.github+json");conn.setRequestProperty("User-Agent","Notety-Android");
                if(conn.getResponseCode()==200){
                    java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();
                    try(java.io.InputStream in=conn.getInputStream()){byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1){bytes.write(b,0,n);if(bytes.size()>524288)throw new java.io.IOException("Large response");}}
                    JSONObject release=new JSONObject(bytes.toString("UTF-8"));String tag=release.optString("tag_name");
                    if(tag.matches("v[0-9]{1,8}")&&!release.optBoolean("draft")&&!release.optBoolean("prerelease")){
                        available=Integer.parseInt(tag.substring(1));org.json.JSONArray assets=release.optJSONArray("assets");
                        if(assets!=null)for(int j=0;j<assets.length();j++){JSONObject item=assets.optJSONObject(j);if(item!=null&&"notety.apk".equals(item.optString("name"))&&item.optLong("size")>0){ok=true;break;}}
                    }
                }
            }catch(Exception ignored){}finally{if(conn!=null)conn.disconnect();}
            final int latest=available;final boolean verified=ok;
            runOnUiThread(()->{checkingUpdate=false;if(isFinishing()||isDestroyed())return;
                if(!verified){if(manual)android.widget.Toast.makeText(this,"تعذر التحقق من وجود تحديث الآن. حاول لاحقًا.",android.widget.Toast.LENGTH_LONG).show();return;}
                prefs.edit().putLong("checked",System.currentTimeMillis()).apply();
                int current;try{current=getPackageManager().getPackageInfo(getPackageName(),0).versionCode;}catch(Exception ex){return;}
                if(latest>current){new android.app.AlertDialog.Builder(this).setTitle("يتوفر تحديث جديد").setMessage("يتوفر إصدار أحدث من نوتتي الروحية ("+latest+"). حمّله من صفحة التطبيق الرسمية وثبّته فوق النسخة الحالية.").setPositiveButton("صفحة التحميل",(d,w)->openDownloadPage()).setNegativeButton("لاحقًا",null).show();}
                else if(manual)android.widget.Toast.makeText(this,"أنت تستخدم أحدث إصدار متاح.",android.widget.Toast.LENGTH_LONG).show();
            });
        }).start();
    }
    private void openDownloadPage(){try{startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(DOWNLOAD_PAGE)));}catch(Exception ignored){}}
    public class Bridge {
        @JavascriptInterface public void checkForUpdate(){runOnUiThread(()->checkUpdate(true));}
        @JavascriptInterface public void openOfficialDownload(){runOnUiThread(()->openDownloadPage());}
        @JavascriptInterface public void finishApp(){runOnUiThread(()->finish());}
        @JavascriptInterface public void syncData(String json){getSharedPreferences("notety_data",MODE_PRIVATE).edit().putString("state",json).apply();}
        @JavascriptInterface public void schedule(){runOnUiThread(()->scheduleAll(MainActivity.this));}
        @JavascriptInterface public boolean notificationsEnabled(){return getSystemService(NotificationManager.class).areNotificationsEnabled();}
        @JavascriptInterface public void openNotificationSettings(){runOnUiThread(()->{Intent i=new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);i.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,getPackageName());if(Build.VERSION.SDK_INT<26){i=new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:"+getPackageName()));}startActivity(i);});}
        @JavascriptInterface public void testNotification(){runOnUiThread(()->{if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},30);return;}if(!notificationsEnabled()){openNotificationSettings();return;}NotificationReceiver.send(MainActivity.this,"اختبار الإشعارات","الإشعارات تعمل على جهازك.",999);});}
        @JavascriptInterface public void share(String value){runOnUiThread(()->{Intent intent=new Intent(Intent.ACTION_SEND);intent.setType("text/plain");intent.putExtra(Intent.EXTRA_TEXT,value);startActivity(Intent.createChooser(intent,"مشاركة"));});}
        @JavascriptInterface public void notify(String title,String content){runOnUiThread(()->{getSharedPreferences("notety_data",MODE_PRIVATE).edit().putString("lastOpeningVerseNotice",new java.text.SimpleDateFormat("yyyy-MM-dd",java.util.Locale.US).format(new java.util.Date())).apply();NotificationReceiver.send(MainActivity.this,title,content,701);});}
        @JavascriptInterface public void printPage(){runOnUiThread(()->{if(Build.VERSION.SDK_INT>=19){PrintManager pm=(PrintManager)getSystemService(Context.PRINT_SERVICE);pm.print("تقرير نوتتي الروحية",web.createPrintDocumentAdapter("تقرير نوتتي الروحية"),null);}});}
    }
    static void scheduleAll(Context c){scheduleAll(c,false);}
    static void scheduleAll(Context c,boolean force){AlarmManager manager=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);if(manager==null)return;
        JSONObject prefs=null;try{JSONObject state=new JSONObject(c.getSharedPreferences("notety_data",Context.MODE_PRIVATE).getString("state","{}"));prefs=state.optJSONObject("settings");}catch(Exception ignored){}
        registerAlarm(c,manager,force,101,at(prefs,"primeTime","07:00"));registerAlarm(c,manager,force,102,at(prefs,"sunsetTime","18:00"));registerAlarm(c,manager,force,104,at(prefs,"sleepTime","22:00"));registerAlarm(c,manager,force,103,at(prefs,"reminderHour","21:00"));registerAlarm(c,manager,force,105,at(prefs,"verseTime","09:00"));
        registerAlarm(c,manager,force,106,"11:00");registerAlarm(c,manager,force,107,"21:00");registerAlarm(c,manager,force,108,at(prefs,"annualTime","09:00"));
    }
    private static String at(JSONObject prefs,String key,String def){return prefs==null?def:prefs.optString(key,def);}
    private static void registerAlarm(Context c,AlarmManager manager,boolean force,int id,String time){try{String[] h=time.split(":");int hour=Integer.parseInt(h[0]),minute=Integer.parseInt(h[1]);if(hour<0||hour>23||minute<0||minute>59)return;
        android.content.SharedPreferences alarmPrefs=c.getSharedPreferences("notety_alarms",Context.MODE_PRIVATE);
        Intent i=new Intent(c,NotificationReceiver.class);i.setAction("org.tekla.jesusprayer.NOTIFY_"+id);i.putExtra("kind",id);if(!force && time.equals(alarmPrefs.getString("time"+id,"")) && PendingIntent.getBroadcast(c,id,i,PendingIntent.FLAG_NO_CREATE|PendingIntent.FLAG_IMMUTABLE)!=null)return;
        PendingIntent p=PendingIntent.getBroadcast(c,id,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);Calendar cal=Calendar.getInstance();cal.set(Calendar.HOUR_OF_DAY,hour);cal.set(Calendar.MINUTE,minute);cal.set(Calendar.SECOND,0);cal.set(Calendar.MILLISECOND,0);if(cal.getTimeInMillis()<=System.currentTimeMillis())cal.add(Calendar.DAY_OF_YEAR,1);manager.setInexactRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),AlarmManager.INTERVAL_DAY,p);alarmPrefs.edit().putString("time"+id,time).apply();
    }catch(Exception ignored){}}
    @Override public void onBackPressed(){if(web!=null)web.evaluateJavascript("if(typeof go==='function'&&screen!=='home'){go('home')}else{window.Android.finishApp&&window.Android.finishApp()}",null);else super.onBackPressed();}
    @Override protected void onDestroy(){if(web!=null){web.destroy();web=null;}super.onDestroy();}
}
