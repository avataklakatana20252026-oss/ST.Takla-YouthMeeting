package org.tekla.jesusprayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONArray;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final int NAVY=0xFF16263B, DEEP=0xFF101D30, GOLD=0xFFB68A43,
        PALE=0xFFE9D3A5, CREAM=0xFFF8F5EF, WHITE=0xFFFFFFFF, INK=0xFF293244,
        MUTED=0xFF7C8190, LINE=0xFFE9E2D7, WINE=0xFF6A3145;
    private SharedPreferences prefs;
    private final ArrayList<String> prayers=new ArrayList<>();
    private MediaPlayer audio;
    private boolean music=false;
    private int count=0, last=0, font=20, page=0;
    private final int PAGE_SIZE=25;
    private LinearLayout body, main, foot;
    private TextView audioButton;
    private String current="home", search="";

    @Override public void onCreate(Bundle b){super.onCreate(b);
        getWindow().setStatusBarColor(DEEP);getWindow().setNavigationBarColor(DEEP);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        prefs=getSharedPreferences("tekla_prayer",MODE_PRIVATE);
        count=prefs.getInt("counter",0);last=prefs.getInt("prayer",0);font=prefs.getInt("font",20);
        try(InputStream in=getAssets().open("prayers.json")){
            ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n;
            while((n=in.read(buf))!=-1)out.write(buf,0,n);
            JSONArray a=new JSONArray(out.toString("UTF-8"));for(int i=0;i<a.length();i++)prayers.add(a.getString(i));
        }catch(Exception e){prayers.add("يا ربي يسوع المسيح ارحمني أنا الخاطئ.");}
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        try{audio=MediaPlayer.create(this,R.raw.oud_ambient);if(audio!=null){audio.setLooping(true);audio.setVolume(.16f,.16f);}}catch(Exception ignored){}
        showHome();
    }
    private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable fill(int col,int rad){GradientDrawable d=new GradientDrawable();d.setColor(col);d.setCornerRadius(dp(rad));return d;}
    private GradientDrawable grad(int c1,int c2,int rad){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{c1,c2});d.setCornerRadius(dp(rad));return d;}
    private GradientDrawable bordered(int col,int border,int rad){GradientDrawable d=fill(col,rad);d.setStroke(dp(1),border);return d;}
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);return l;}
    private LinearLayout horizontal(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    private LinearLayout.LayoutParams sz(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
    private void padding(View v,int x,int y){v.setPadding(dp(x),dp(y),dp(x),dp(y));}
    private void gap(LinearLayout l,int n){l.addView(new View(this),sz(1,n));}
    private TextView tx(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextColor(color);t.setTextSize(size);t.setTextDirection(View.TEXT_DIRECTION_RTL);t.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);if(bold)t.setTypeface(Typeface.create("sans-serif-medium",Typeface.BOLD));else t.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));return t;}
    private TextView mid(String s,int size,int color,boolean bold){TextView t=tx(s,size,color,bold);t.setGravity(Gravity.CENTER);return t;}
    // A book-like, readable Arabic serif face using Android's installed fonts.
    private TextView prayerText(String value,int sp){
        TextView t=mid(value,sp,INK,false);
        t.setTypeface(Typeface.create("serif",Typeface.NORMAL));
        t.setIncludeFontPadding(true);
        t.setLineSpacing(dp(9),1.16f);
        t.setTextDirection(View.TEXT_DIRECTION_RTL);
        t.setGravity(Gravity.CENTER);
        return t;
    }
    private void add(View v){body.addView(v,sz(-1,-2));}
    private void marginAdd(LinearLayout parent,View v,int bottom){LinearLayout.LayoutParams p=sz(-1,-2);p.bottomMargin=dp(bottom);parent.addView(v,p);}
    private TextView pill(String s,int bg,int fg,Runnable on){TextView v=mid(s,15,fg,true);v.setBackground(fill(bg,15));padding(v,14,14);v.setOnClickListener(w->on.run());return v;}
    private void bigButton(String s,Runnable on,boolean solid){TextView v=pill(s,solid?GOLD:WHITE,solid?DEEP:NAVY,on);if(!solid)v.setBackground(bordered(WHITE,LINE,15));marginAdd(body,v,11);}
    private LinearLayout block(int color,int radius){LinearLayout x=column();x.setBackground(fill(color,radius));padding(x,18,18);return x;}
    private void framed(LinearLayout l){marginAdd(body,l,14);l.setElevation(dp(2));}
    private void shell(String title,String tab){current=tab;main=column();main.setBackgroundColor(CREAM);setContentView(main);
        LinearLayout top=horizontal();top.setBackgroundColor(DEEP);padding(top,15,10);main.addView(top,sz(-1,64));
        TextView emblem=mid("✝",28,PALE,true);top.addView(emblem,sz(40,42));emblem.setOnClickListener(v->showHome());
        LinearLayout headings=column();top.addView(headings,new LinearLayout.LayoutParams(0,-2,1));
        headings.addView(tx(title,17,WHITE,true));headings.addView(tx("اجتماع الشباب • الأنبا تكلا بأشمون",11,PALE,false));
        audioButton=mid(music?"♫ تشغيل":"♫ كتم",12,music?DEEP:PALE,true);padding(audioButton,9,9);audioButton.setBackground(bordered(music?PALE:NAVY,GOLD,12));top.addView(audioButton,sz(-2,-2));audioButton.setOnClickListener(v->toggleAudio());
        ScrollView s=new ScrollView(this);s.setFillViewport(true);s.setVerticalScrollBarEnabled(false);main.addView(s,new LinearLayout.LayoutParams(-1,0,1));
        body=column();padding(body,17,18);s.addView(body,sz(-1,-2));
        foot=horizontal();foot.setBackgroundColor(WHITE);foot.setElevation(dp(10));main.addView(foot,sz(-1,70));
        nav("⌂","الرئيسية","home",()->showHome());nav("☷","الصلوات","list",()->showList(false));nav("✣","المسبحة","counter",()->showCounter());nav("♡","المفضلة","fav",()->showList(true));
    }
    private void nav(String icon,String name,String key,Runnable action){LinearLayout c=column();c.setGravity(Gravity.CENTER);TextView i=mid(icon,24,current.equals(key)?GOLD:MUTED,true);c.addView(i);TextView n=mid(name,11,current.equals(key)?NAVY:MUTED,current.equals(key));c.addView(n);foot.addView(c,new LinearLayout.LayoutParams(0,-1,1));c.setOnClickListener(v->action.run());}
    private void toggleAudio(){if(audio==null){Toast.makeText(this,"الملف الصوتي غير متاح",Toast.LENGTH_SHORT).show();return;}try{music=!music;if(music)audio.start();else audio.pause();audioButton.setText(music?"♫ تشغيل":"♫ كتم");audioButton.setTextColor(music?DEEP:PALE);audioButton.setBackground(bordered(music?PALE:NAVY,GOLD,12));}catch(Exception ex){music=false;}}
    private Set<String> favorites(){return new HashSet<>(prefs.getStringSet("fav",new HashSet<>()));}
    private void favorite(int i){Set<String> x=favorites();String k=""+i;if(!x.add(k))x.remove(k);prefs.edit().putStringSet("fav",x).apply();}
    private void showHome(){shell("الصلاة السهمية","home");
        LinearLayout hero=column();hero.setBackground(grad(NAVY,0xFF33485B,24));padding(hero,17,16);framed(hero);
        TextView crown=mid("✦  خَلْوَةٌ مَعَ المَسِيح  ✦",12,PALE,true);hero.addView(crown);gap(hero,7);
        TextView saintTitle=mid("القديس الأنبا تكلا هيمانوت",20,WHITE,true);hero.addView(saintTitle);gap(hero,2);
