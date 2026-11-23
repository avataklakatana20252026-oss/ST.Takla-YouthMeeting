package org.tekla.jesusprayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import java.util.Iterator;
import java.util.ArrayList;

public class NotificationReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent intent){
  try{
   JSONObject state=new JSONObject(c.getSharedPreferences("notety_data",Context.MODE_PRIVATE).getString("state","{}"));
   JSONObject settings=state.optJSONObject("settings");if(settings==null)settings=new JSONObject();
   if(!settings.optBoolean("reminders",true))return;
   JSONObject acts=state.optJSONObject("activities");if(acts==null)acts=new JSONObject();
   String today=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());
   JSONObject current=acts.optJSONObject(today);if(current==null)current=new JSONObject();
   int kind=intent.getIntExtra("kind",103);
   boolean agpeya=any(current,new String[]{"prime","third","sixth","ninth","sunset","sleep","midnight"});
   if(kind==105){if(settings.optBoolean("dailyVerse",true)&&!today.equals(c.getSharedPreferences("notety_data",Context.MODE_PRIVATE).getString("lastOpeningVerseNotice","")))send(c,"رسالة ربنا ليك النهارده †","آية ورسالة النهارده مستنياك في نوتتي الروحية.",801);return;}
   if(kind==101||kind==102||kind==104){if(!settings.optBoolean("prayerReminders",true)||agpeya)return;
       String key=kind==101?"notifyPrime":kind==102?"notifySunset":"notifySleep";
       if(!settings.optBoolean(key,true))return;
       String label=kind==101?"باكر":kind==102?"الغروب":"النوم";
       send(c,"تذكير الأجبية †","افتكر صلاة "+label+" لو مناسبة ليك النهارده.",810+kind);return;}
   ArrayList<String> reminders=new ArrayList<>();
   if(settings.optBoolean("agpeyaReminder",true)&&!agpeya)reminders.add("الأجبية");
   JSONObject counts=state.optJSONObject("jesus");
   if(settings.optBoolean("jesusReminder",true)&&!current.optBoolean("jesus")&&(counts==null||counts.optInt(today,0)<=0))reminders.add("صلاة يسوع");
   String lastBible=lastDate(acts,"bible");if(settings.optBoolean("readReminder",true)&&!current.optBoolean("bible")&&(lastBible.isEmpty()||days(lastBible,today)>=2))reminders.add("قراءة الكتاب المقدس");
   String lastBooks=lastDate(acts,"books");if(settings.optBoolean("booksReminder",false)&&!current.optBoolean("books")&&(lastBooks.isEmpty()||days(lastBooks,today)>Math.max(1,settings.optInt("booksDays",5))))reminders.add("قراءة الكتب الروحية");
   String lastMass=lastDate(acts,"mass");if(settings.optBoolean("massReminder",true)&&!current.optBoolean("mass")&&(lastMass.isEmpty()||days(lastMass,today)>7))reminders.add("القداس");
   String lastLiturgy=lastAny(acts,new String[]{"mass","vespers","praise"});if(settings.optBoolean("liturgyReminder",true)&&!any(current,new String[]{"mass","vespers","praise"})&&(lastLiturgy.isEmpty()||days(lastLiturgy,today)>7))reminders.add("الليتورجيا");
   String lastCommunion=lastDate(acts,"communion");if(settings.optBoolean("communionReminder",false)&&!current.optBoolean("communion")&&!lastCommunion.isEmpty()&&days(lastCommunion,today)>Math.max(1,settings.optInt("communionDays",30)))reminders.add("التناول");
   if(settings.optBoolean("confessionReminder",true)){
       String last=lastDate(acts,"confession");if(!last.isEmpty()){
          Calendar plus=parse(last),now=parse(today);plus.add(Calendar.MONTH,2);
          if(now.after(plus))send(c,"تنبيه مهم بخصوص الاعتراف †","مرّ أكثر من شهرين على آخر اعتراف سجّلته. من المهم عدم إهمال المتابعة مع أب اعترافك.",802);
          else if(days(last,today)>28)send(c,"تذكير بسر التوبة والاعتراف †","مرّ أكثر من ٤ أسابيع على آخر اعتراف سجّلته. افتكر ترتّب موعدًا مناسبًا.",803);
       }
   }
   if(!reminders.isEmpty())send(c,"تذكيراتي الروحية †","مش متسجّل عندك مؤخرًا: "+android.text.TextUtils.join("، ",reminders)+". تقدر تبدأ بخطوة صغيرة.",806);
  }catch(Exception ignored){}
 }
 static boolean any(JSONObject o,String[] keys){for(String k:keys)if(o.optBoolean(k))return true;return false;}
 static String lastDate(JSONObject days,String key){String latest="";Iterator<String> it=days.keys();while(it.hasNext()){String date=it.next();if(date.compareTo(latest)>0&&days.optJSONObject(date)!=null&&days.optJSONObject(date).optBoolean(key))latest=date;}return latest;}
 static String lastAny(JSONObject days,String[] keys){String latest="";Iterator<String> it=days.keys();while(it.hasNext()){String date=it.next();JSONObject a=days.optJSONObject(date);if(a!=null&&any(a,keys)&&date.compareTo(latest)>0)latest=date;}return latest;}
 static Calendar parse(String d){Calendar c=Calendar.getInstance();String[] x=d.split("-");if(x.length==3){c.set(Integer.parseInt(x[0]),Integer.parseInt(x[1])-1,Integer.parseInt(x[2]),0,0,0);c.set(Calendar.MILLISECOND,0);}return c;}
 static int days(String a,String b){return (int)Math.round((parse(b).getTimeInMillis()-parse(a).getTimeInMillis())/86400000.0);}
 static void send(Context c,String title,String body,int id){try{if(Build.VERSION.SDK_INT>=33&&c.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return;
  Intent open=new Intent(c,MainActivity.class);open.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);PendingIntent pi=PendingIntent.getActivity(c,id,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(c,MainActivity.CHANNEL):new Notification.Builder(c);
  b.setSmallIcon(R.drawable.ic_notification).setContentTitle(title).setContentText(body).setStyle(new Notification.BigTextStyle().bigText(body)).setContentIntent(pi).setAutoCancel(true).setWhen(System.currentTimeMillis());
  ((NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE)).notify(id,b.build());
 }catch(Exception ignored){}}
}
