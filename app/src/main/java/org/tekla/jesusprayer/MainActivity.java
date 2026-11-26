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
    public static final String CHANNEL="notety_daily";
    @Override public void onCreate(Bundle b){super.onCreate(b);
        getWindow().setStatusBarColor(0xFF350A17);getWindow().setNavigationBarColor(0xFF350A17);
        if(Build.VERSION.SDK_INT>=26){getWindow().getDecorView().setSystemUiVisibility(0);NotificationChannel c=new NotificationChannel(CHANNEL,"تذكيراتي الروحية",NotificationManager.IMPORTANCE_DEFAULT);c.setDescription("رسالة اليوم والتنبيهات الروحية");getSystemService(NotificationManager.class).createNotificationChannel(c);}
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},29);
        web=new WebView(this);web.setBackgroundColor(0xFF350A17);web.setWebViewClient(new WebViewClient(){@Override public void onPageFinished(WebView v,String url){super.onPageFinished(v,url);openFastIntent();}});web.setWebChromeClient(new WebChromeClient());
        WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(true);s.setAllowFileAccessFromFileURLs(true);s.setAllowUniversalAccessFromFileURLs(false);s.setDefaultTextEncodingName("UTF-8");s.setTextZoom(100);
        web.addJavascriptInterface(new Bridge(),"Android");setContentView(web);web.loadUrl("file:///android_asset/index.html");scheduleAll(this);
    }
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);openFastIntent();}
    private void openFastIntent(){Intent i=getIntent();if(web==null||i==null)return;if(i.getBooleanExtra("annualNotice",false)){web.evaluateJavascript("if(typeof openAnnualNotice==='function'){openAnnualNotice();true}else{false}",result->{if("true".equals(result))i.removeExtra("annualNotice");});return;}String period=i.getStringExtra("fastPeriod"),date=i.getStringExtra("fastDate");if(period==null||date==null||!date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")||!(period.equals("morning")||period.equals("evening")))return;web.evaluateJavascript("if(typeof openFastNotice==='function'){openFastNotice("+JSONObject.quote(date)+","+JSONObject.quote(period)+");true}else{false}",result->{if("true".equals(result)){i.removeExtra("fastPeriod");i.removeExtra("fastDate");}});}
    public class Bridge {
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
