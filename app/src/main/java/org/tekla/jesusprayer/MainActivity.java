package org.tekla.jesusprayer;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PrintManager;
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
    public static final String CHANNEL="notety_daily";
    @Override public void onCreate(Bundle b){super.onCreate(b);
        getWindow().setStatusBarColor(0xFFFAF7F2);getWindow().setNavigationBarColor(0xFFFAF7F2);
        if(Build.VERSION.SDK_INT>=26){getWindow().getDecorView().setSystemUiVisibility(0x00002000|0x00000010);NotificationChannel c=new NotificationChannel(CHANNEL,"تذكيراتي الروحية",NotificationManager.IMPORTANCE_DEFAULT);c.setDescription("رسالة اليوم والتنبيهات الروحية");getSystemService(NotificationManager.class).createNotificationChannel(c);}
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},29);
        web=new WebView(this);web.setBackgroundColor(0xFFFAF7F2);web.setWebViewClient(new WebViewClient());web.setWebChromeClient(new WebChromeClient());
        WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(true);s.setAllowFileAccessFromFileURLs(true);s.setAllowUniversalAccessFromFileURLs(false);s.setDefaultTextEncodingName("UTF-8");s.setTextZoom(100);
        web.addJavascriptInterface(new Bridge(),"Android");setContentView(web);web.loadUrl("file:///android_asset/index.html");scheduleAll(this);
    }
    public class Bridge {
        @JavascriptInterface public void finishApp(){runOnUiThread(()->finish());}
        @JavascriptInterface public void syncData(String json){getSharedPreferences("notety_data",MODE_PRIVATE).edit().putString("state",json).apply();}
        @JavascriptInterface public void schedule(){runOnUiThread(()->scheduleAll(MainActivity.this));}
        @JavascriptInterface public void share(String value){runOnUiThread(()->{Intent intent=new Intent(Intent.ACTION_SEND);intent.setType("text/plain");intent.putExtra(Intent.EXTRA_TEXT,value);startActivity(Intent.createChooser(intent,"مشاركة"));});}
        @JavascriptInterface public void notify(String title,String content){runOnUiThread(()->{getSharedPreferences("notety_data",MODE_PRIVATE).edit().putString("lastOpeningVerseNotice",new java.text.SimpleDateFormat("yyyy-MM-dd",java.util.Locale.US).format(new java.util.Date())).apply();NotificationReceiver.send(MainActivity.this,title,content,701);});}
        @JavascriptInterface public void printPage(){runOnUiThread(()->{if(Build.VERSION.SDK_INT>=19){PrintManager pm=(PrintManager)getSystemService(Context.PRINT_SERVICE);pm.print("تقرير نوتتي الروحية",web.createPrintDocumentAdapter("تقرير نوتتي الروحية"),null);}});}
    }
    static void scheduleAll(Context c){AlarmManager manager=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);if(manager==null)return;
        JSONObject prefs=null;try{JSONObject state=new JSONObject(c.getSharedPreferences("notety_data",Context.MODE_PRIVATE).getString("state","{}"));prefs=state.optJSONObject("settings");}catch(Exception ignored){}
        registerAlarm(c,manager,101,at(prefs,"primeTime","07:00"));registerAlarm(c,manager,102,at(prefs,"sunsetTime","18:00"));registerAlarm(c,manager,104,at(prefs,"sleepTime","22:00"));registerAlarm(c,manager,103,at(prefs,"reminderHour","21:00"));registerAlarm(c,manager,105,"09:00");
    }
    private static String at(JSONObject prefs,String key,String def){return prefs==null?def:prefs.optString(key,def);}
    private static void registerAlarm(Context c,AlarmManager manager,int id,String time){try{String[] h=time.split(":");int hour=Integer.parseInt(h[0]),minute=Integer.parseInt(h[1]);if(hour<0||hour>23||minute<0||minute>59)return;
        Intent i=new Intent(c,NotificationReceiver.class);i.setAction("org.tekla.jesusprayer.NOTIFY_"+id);i.putExtra("kind",id);PendingIntent p=PendingIntent.getBroadcast(c,id,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);Calendar cal=Calendar.getInstance();cal.set(Calendar.HOUR_OF_DAY,hour);cal.set(Calendar.MINUTE,minute);cal.set(Calendar.SECOND,0);if(cal.getTimeInMillis()<=System.currentTimeMillis())cal.add(Calendar.DAY_OF_YEAR,1);manager.setInexactRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),AlarmManager.INTERVAL_DAY,p);
    }catch(Exception ignored){}}
    @Override public void onBackPressed(){if(web!=null)web.evaluateJavascript("if(typeof go==='function'&&screen!=='home'){go('home')}else{window.Android.finishApp&&window.Android.finishApp()}",null);else super.onBackPressed();}
    @Override protected void onDestroy(){if(web!=null){web.destroy();web=null;}super.onDestroy();}
}
