package org.tekla.jesusprayer;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.text.Editable;
import android.text.TextWatcher;
import org.json.JSONArray;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int WINE=Color.rgb(91,27,53), GOLD=Color.rgb(177,132,54), IVORY=Color.rgb(250,246,239), INK=Color.rgb(55,43,45);
    private LinearLayout root, body, toolbar;
    private SharedPreferences prefs;
    private final ArrayList<String> prayers = new ArrayList<>();
    private MediaPlayer player;
    private boolean musicOn=false;
    private int fontSize=19, counter=0, selectedPrayer=0;
    private String screen="home", filter="";
    private int page=0;
    private TextView musicToggle;
    private final int PAGE_SIZE=36;
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(WINE);getWindow().setNavigationBarColor(WINE);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        prefs=getSharedPreferences("tekla_prayer",MODE_PRIVATE);
        fontSize=prefs.getInt("font",19);counter=prefs.getInt("counter",0);selectedPrayer=prefs.getInt("prayer",0);
        try { InputStream in=getAssets().open("prayers.json");ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)b.write(buf,0,n);in.close();JSONArray j=new JSONArray(b.toString("UTF-8"));for(int i=0;i<j.length();i++)prayers.add(j.getString(i)); }catch(Exception ex){prayers.add("يا ربي يسوع المسيح ارحمني أنا الخاطئ.");}
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        try {player=MediaPlayer.create(this,R.raw.oud_ambient);if(player!=null){player.setLooping(true); player.setVolume(.22f,.22f);}}catch(Exception ignored){}
        showHome();
    }
    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private GradientDrawable outline(int color,int stroke,int radius){GradientDrawable d=bg(color,radius);d.setStroke(dp(1),stroke);return d;}
    private LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);return l;}
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);return l;}
    private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
    private TextView text(String txt,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(txt);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);t.setTextDirection(View.TEXT_DIRECTION_RTL);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private void pad(View v,int x,int y){v.setPadding(dp(x),dp(y),dp(x),dp(y));}
    private void space(LinearLayout parent,int height){View v=new View(this);parent.addView(v,lp(1,height));}
    private TextView button(String label,Runnable onClick,boolean primary){TextView b=text(label,16,primary?Color.WHITE:WINE,true);b.setGravity(Gravity.CENTER);b.setBackground(outline(primary?WINE:Color.WHITE,primary?WINE:GOLD,16));pad(b,12,13);b.setOnClickListener(v->onClick.run());return b;}
    private void begin(String title){
        root=col();root.setBackgroundColor(IVORY);setContentView(root);
        toolbar=row();toolbar.setBackgroundColor(WINE);pad(toolbar,17,13);root.addView(toolbar,lp(-1,65));
        TextView back=text("‹",31,Color.WHITE,true);back.setGravity(Gravity.CENTER);toolbar.addView(back,lp(35,-1));back.setOnClickListener(v->showHome());
        TextView name=text(title,18,Color.WHITE,true); name.setGravity(Gravity.CENTER);toolbar.addView(name,new LinearLayout.LayoutParams(0,-1,1));
        musicToggle=text(musicOn?"♫ ●":"♫ ○",23,0xFFF0D296,true);musicToggle.setGravity(Gravity.CENTER);toolbar.addView(musicToggle,lp(61,-1));musicToggle.setOnClickListener(v->toggleMusic());
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));body=col();pad(body,18,18);scroll.addView(body);
        LinearLayout bottom=row();bottom.setBackgroundColor(Color.WHITE);bottom.setElevation(dp(6));root.addView(bottom,lp(-1,61));
        addNav(bottom,"الرئيسية",()->showHome());addNav(bottom,"الصلوات",()->showList(false));addNav(bottom,"المسبحة",()->showCounter());addNav(bottom,"المفضلة",()->showList(true));
    }
    private void addNav(LinearLayout parent,String label,Runnable onClick){TextView b=text(label,13,WINE,true);b.setGravity(Gravity.CENTER);parent.addView(b,new LinearLayout.LayoutParams(0,-1,1));b.setOnClickListener(v->onClick.run());}
    private void toggleMusic(){if(player==null){Toast.makeText(this,"الصوت غير متاح على هذا الجهاز",Toast.LENGTH_SHORT).show();return;}musicOn=!musicOn;try{if(musicOn)player.start();else player.pause();}catch(Exception e){musicOn=false;}musicToggle.setText(musicOn?"♫ ●":"♫ ○");}
    private void addButton(LinearLayout parent,String label,Runnable click,boolean primary){TextView b=button(label,click,primary);LinearLayout.LayoutParams p=lp(-1,-2);p.bottomMargin=dp(12);parent.addView(b,p);}
    private void card(LinearLayout parent,View child){LinearLayout shell=col();shell.setBackground(outline(Color.WHITE,0xFFE4D7BF,19));shell.setElevation(dp(2));pad(shell,15,15);LinearLayout.LayoutParams p=lp(-1,-2);p.bottomMargin=dp(15);parent.addView(shell,p);shell.addView(child,lp(-1,-2));}
    private TextView centered(String value,int size,int color,boolean bold){TextView t=text(value,size,color,bold);t.setGravity(Gravity.CENTER);return t;}
    private void heroImage(int drawable,int height){ImageView hero=new ImageView(this);hero.setImageResource(drawable);hero.setScaleType(ImageView.ScaleType.CENTER_CROP);hero.setBackground(bg(WINE,19));hero.setClipToOutline(true);LinearLayout.LayoutParams p=lp(-1,height);p.bottomMargin=dp(16);body.addView(hero,p);}
    private void showHome(){screen="home";begin("اجتماع الشباب");
        TextView church=centered("اجتماع الشباب بكنيسة الأنبا تكلا هيمانوت الحبشي بأشمون",17,WINE,true);body.addView(church,lp(-1,-2));space(body,8);
        heroImage(R.drawable.prayer_cover,220);
        body.addView(centered("الصلاة السهمية",28,WINE,true),lp(-1,-2));space(body,5);
        body.addView(centered("صلاة يسوع • حضور القلب وهدوء النفس",15,GOLD,false),lp(-1,-2));space(body,12);
        body.addView(centered("† اذكر يا رب عبدك القائم على هذا العمل، واجعله سبب بركة للنفوس. آمين.",15,WINE,false),lp(-1,-2));space(body,22);
        addButton(body,"✦ ابدأ القراءة",()->showDetail(Math.min(Math.max(selectedPrayer,0),prayers.size()-1)),true);
        addButton(body,"☷ تصفّح جميع الصلوات ("+prayers.size()+")",()->showList(false),false);
        addButton(body,"✝ المسبحة الإلكترونية",()->showCounter(),false);
        addButton(body,"♡ الصلوات المفضلة",()->showList(true),false);
        LinearLayout info=col();info.addView(centered("الموسيقى اختيارية ومغلقة عند بدء التطبيق",14,INK,false));space(info,6);info.addView(centered("العمل دون إنترنت • حفظ موضع القراءة تلقائيًا",13,GOLD,false));card(body,info);
    }
    private Set<String> favorites(){return new HashSet<>(prefs.getStringSet("fav",new HashSet<>()));}
    private void toggleFavorite(int idx){Set<String> fav=favorites();String key=String.valueOf(idx);if(fav.contains(key))fav.remove(key);else fav.add(key);prefs.edit().putStringSet("fav",fav).apply();}
    private void showList(boolean onlyFav){screen=onlyFav?"favorites":"list";begin(onlyFav?"المفضلة":"جميع الصلوات");
        if(!onlyFav){EditText search=new EditText(this);search.setSingleLine(true);search.setTextSize(16);search.setHint("ابحث في نصوص الصلاة...");search.setBackground(outline(Color.WHITE,0xFFE4D7BF,14));pad(search,12,11);body.addView(search,lp(-1,52));space(body,13);search.setText(filter);search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int st,int before,int count){filter=s.toString();page=0;showListResults(onlyFav);}public void afterTextChanged(Editable e){} });}
        showListResults(onlyFav);
    }
    private LinearLayout results;
    private void showListResults(boolean onlyFav){
        if(results!=null && results.getParent()==body)body.removeView(results);
        results=col();body.addView(results,lp(-1,-2));List<Integer> indices=new ArrayList<>();Set<String> fav=favorites();
        for(int i=0;i<prayers.size();i++){if(onlyFav&&!fav.contains(String.valueOf(i)))continue;if(!onlyFav&&!prayers.get(i).contains(filter))continue;indices.add(i);}
        results.addView(text(indices.size()+" صلاة",14,GOLD,true));space(results,10);
        if(indices.isEmpty()){results.addView(centered(onlyFav?"لم تضف أي صلاة للمفضلة بعد":"لا توجد نتائج مطابقة",17,INK,false));return;}
        int start=Math.min(page*PAGE_SIZE,Math.max(0,indices.size()-1)),end=Math.min(start+PAGE_SIZE,indices.size());
        for(int j=start;j<end;j++){final int idx=indices.get(j);LinearLayout box=col();TextView heading=text("صلاة رقم "+(idx+1)+"     "+(fav.contains(String.valueOf(idx))?"♥":"♡"),14,GOLD,true);box.addView(heading);space(box,7);TextView value=text(prayers.get(idx),fontSize,INK,false);value.setLineSpacing(dp(4),1.1f);box.addView(value);card(results,box);box.setOnClickListener(v->showDetail(idx));}
        LinearLayout controls=row();TextView prev=button("السابق",()->{if(page>0){page--;showListResults(onlyFav);}},false);TextView next=button("التالي",()->{if((page+1)*PAGE_SIZE<indices.size()){page++;showListResults(onlyFav);}},true);
        LinearLayout.LayoutParams half=new LinearLayout.LayoutParams(0,dp(52),1);half.setMargins(dp(3),0,dp(3),0);controls.addView(prev,half);controls.addView(next,new LinearLayout.LayoutParams(0,dp(52),1));results.addView(controls);results.addView(centered("صفحة "+(page+1)+" من "+((indices.size()+PAGE_SIZE-1)/PAGE_SIZE),14,GOLD,false));
    }
    private void showDetail(int idx){screen="detail";selectedPrayer=idx;prefs.edit().putInt("prayer",idx).apply();begin("صلاة يسوع");
        body.addView(centered("✝",40,GOLD,true));space(body,8);
        body.addView(centered("الصلاة "+(idx+1)+" من "+prayers.size(),16,WINE,true));space(body,18);
        TextView prayer=centered(prayers.get(idx),fontSize+5,INK,false);prayer.setLineSpacing(dp(10),1.25f);pad(prayer,12,25);card(body,prayer);
        addButton(body,favorites().contains(String.valueOf(idx))?"♥ إزالة من المفضلة":"♡ إضافة إلى المفضلة",()->{toggleFavorite(idx);showDetail(idx);},false);
        LinearLayout controls=row();TextView previous=button("◀ السابقة",()->showDetail((idx-1+prayers.size())%prayers.size()),false);TextView next=button("التالية ▶",()->showDetail((idx+1)%prayers.size()),true);controls.addView(previous,new LinearLayout.LayoutParams(0,dp(54),1));controls.addView(next,new LinearLayout.LayoutParams(0,dp(54),1));body.addView(controls);space(body,17);
        body.addView(centered("حجم الخط",15,WINE,true));
        SeekBar seek=new SeekBar(this);seek.setMax(18);seek.setProgress(fontSize-15);body.addView(seek);seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean u){fontSize=15+p;prayer.setTextSize(fontSize+5);prefs.edit().putInt("font",fontSize).apply();}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){} });
    }
    private void showCounter(){screen="counter";begin("المسبحة الإلكترونية");body.addView(centered("✝",52,GOLD,true));space(body,10);
        body.addView(centered("يا ربي يسوع المسيح ارحمني أنا الخاطئ",21,WINE,true));space(body,26);
        TextView number=centered(String.valueOf(counter),62,WINE,true);pad(number,28,22);number.setBackground(outline(Color.WHITE,GOLD,100));LinearLayout.LayoutParams n=lp(-1,160);n.setMargins(dp(16),0,dp(16),dp(22));body.addView(number,n);
        addButton(body,"✝ صلاة +1",()->{counter++;prefs.edit().putInt("counter",counter).apply();number.setText(String.valueOf(counter));},true);
        addButton(body,"↶ تراجع مرة",()->{counter=Math.max(0,counter-1);prefs.edit().putInt("counter",counter).apply();number.setText(String.valueOf(counter));},false);
        addButton(body,"إعادة العدّاد إلى صفر",()->{new android.app.AlertDialog.Builder(this).setMessage("هل تريد تصفير عدّاد الصلاة؟").setNegativeButton("إلغاء",null).setPositiveButton("تصفير",(d,w)->{counter=0;prefs.edit().putInt("counter",0).apply();number.setText("0");}).show();},false);
        body.addView(centered("العدد محفوظ تلقائيًا على هذا الجهاز",14,GOLD,false));
    }
    @Override public void onBackPressed(){if(!screen.equals("home"))showHome();else super.onBackPressed();}
    @Override protected void onPause(){super.onPause();if(player!=null&&player.isPlaying()){player.pause();musicOn=false;if(musicToggle!=null)musicToggle.setText("♫ ○");}}
    @Override protected void onDestroy(){if(player!=null){player.release();player=null;}super.onDestroy();}
}
