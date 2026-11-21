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
        hero.addView(mid("شفيع اجتماع الشباب",13,PALE,false));
        ImageView saint=new ImageView(this);saint.setImageResource(R.drawable.saint_tekla);saint.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams img=sz(-1,265);img.setMargins(0,dp(7),0,dp(5));hero.addView(saint,img);
        TextView name=mid("كنيسة الأنبا تكلا هيمانوت الحبشي بأشمون",13,WHITE,true);hero.addView(name);gap(hero,12);
        LinearLayout prayerSection=column();prayerSection.setBackground(bordered(0xFF26384B,0xFF586277,16));padding(prayerSection,13,12);
        prayerSection.addView(mid("† اذكر يا رب عبدك القائم على هذا العمل،",13,PALE,false));
        prayerSection.addView(mid("واجعله سبب بركة للنفوس. آمين.",13,PALE,false));hero.addView(prayerSection);
        TextView section=tx("صلِّ بقلبك",24,NAVY,true);marginAdd(body,section,2);
        marginAdd(body,tx("مساحة للسكينة والصلاة، في أي وقت ومن غير إنترنت",13,MUTED,false),16);
        LinearLayout feature=block(WHITE,20);feature.setBackground(bordered(WHITE,LINE,20));feature.addView(tx("✝  صلاة يسوع",22,NAVY,true));gap(feature,9);
        TextView shortPrayer=prayerText("يا ربي يسوع المسيح، ارحمني أنا الخاطئ",23);shortPrayer.setTextColor(WINE);feature.addView(shortPrayer);gap(feature,15);
        TextView read=pill("ابدأ الصلاة  ←",GOLD,DEEP,()->showDetail(Math.min(last,prayers.size()-1)));feature.addView(read);framed(feature);
        LinearLayout tiles=horizontal();marginAdd(body,tiles,14);
        LinearLayout a=mini("☷","مكتبة الصلوات",""+prayers.size()+" صلاة",()->showList(false));
        LinearLayout b=mini("✣","المسبحة","العدد: "+count,()->showCounter());
        LinearLayout.LayoutParams half=new LinearLayout.LayoutParams(0,-2,1);half.setMargins(0,0,dp(6),0);tiles.addView(a,half);tiles.addView(b,new LinearLayout.LayoutParams(0,-2,1));
        LinearLayout resume=block(0xFFF1E9D9,18);resume.addView(tx("✧  متابعة من حيث توقفت",16,NAVY,true));gap(resume,6);resume.addView(tx("الصلاة رقم "+(last+1)+"   •   اضغط للاستكمال",13,MUTED,false));resume.setOnClickListener(v->showDetail(Math.min(last,prayers.size()-1)));framed(resume);
        marginAdd(body,mid("الموسيقى اختيارية ويمكن كتمها من أعلى الشاشة",12,MUTED,false),12);
    }
    private LinearLayout mini(String ico,String label,String detail,Runnable click){LinearLayout l=block(WHITE,19);l.setBackground(bordered(WHITE,LINE,19));l.addView(mid(ico,28,GOLD,true));gap(l,6);l.addView(mid(label,14,NAVY,true));l.addView(mid(detail,12,MUTED,false));l.setOnClickListener(v->click.run());return l;}
    private LinearLayout results;
    private void showList(boolean favOnly){page=0;renderList(favOnly);}
    private void renderList(boolean favOnly){shell(favOnly?"الصلوات المفضلة":"مكتبة الصلوات",favOnly?"fav":"list");
        marginAdd(body,tx(favOnly?"صلوات احتفظت بها":"كل صلاة هي لقاء جديد",24,NAVY,true),3);
        marginAdd(body,tx(favOnly?"اضغط على الصلاة لاستكمال القراءة":"اقرأ وتأمل وابحث في نصوص الصلاة",13,MUTED,false),15);
        if(!favOnly){EditText input=new EditText(this);input.setSingleLine(true);input.setTextSize(15);input.setHint("⌕  ابحث في الصلوات...");input.setBackground(bordered(WHITE,LINE,16));padding(input,14,10);input.setText(search);marginAdd(body,input,14);
            input.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){search=s.toString();page=0;populate(false);}public void afterTextChanged(Editable x){}});}
        results=column();add(results);populate(favOnly);
    }
    private void populate(boolean favOnly){results.removeAllViews();Set<String> f=favorites();ArrayList<Integer> ids=new ArrayList<>();for(int i=0;i<prayers.size();i++)if((!favOnly||f.contains(""+i))&&(favOnly||prayers.get(i).contains(search)))ids.add(i);
        marginAdd(results,tx(ids.size()+" صلاة",13,GOLD,true),12);
        if(ids.isEmpty()){marginAdd(results,mid("لا توجد صلوات هنا حاليًا",16,MUTED,false),20);return;}
        int begin=page*PAGE_SIZE,end=Math.min(begin+PAGE_SIZE,ids.size());for(int n=begin;n<end;n++){final int i=ids.get(n);LinearLayout c=block(WHITE,16);c.setBackground(bordered(WHITE,LINE,16));
            c.addView(tx("✝   الصلاة "+(i+1)+(f.contains(""+i)?"      ♥":""),13,GOLD,true));gap(c,8);
            TextView excerpt=prayerText(prayers.get(i),20);excerpt.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);excerpt.setMaxLines(3);excerpt.setLineSpacing(dp(3),1.1f);c.addView(excerpt);c.setOnClickListener(v->showDetail(i));marginAdd(results,c,10);}
        LinearLayout next=horizontal();TextView before=pill("السابق",WHITE,NAVY,()->{if(page>0){page--;populate(favOnly);}});TextView after=pill("التالي",GOLD,DEEP,()->{if((page+1)*PAGE_SIZE<ids.size()){page++;populate(favOnly);}});
        next.addView(before,new LinearLayout.LayoutParams(0,dp(50),1));LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(0,dp(50),1);ap.setMargins(dp(8),0,0,0);next.addView(after,ap);marginAdd(results,next,10);marginAdd(results,mid("صفحة "+(page+1)+" من "+((ids.size()+PAGE_SIZE-1)/PAGE_SIZE),12,MUTED,false),20);
    }
    private void showDetail(int id){last=id;prefs.edit().putInt("prayer",last).apply();shell("صلاة يسوع","detail");
        marginAdd(body,mid("✝",44,GOLD,true),7);
        marginAdd(body,mid("صلاة "+(id+1)+" من "+prayers.size(),14,MUTED,true),17);
        LinearLayout panel=block(WHITE,21);panel.setBackground(bordered(WHITE,LINE,21));
        marginAdd(panel,mid("باسم الآب والابن والروح القدس",14,GOLD,true),19);
        TextView content=prayerText(prayers.get(id),font+4);padding(content,4,10);panel.addView(content);gap(panel,17);
        panel.addView(mid("✦  آمين  ✦",13,GOLD,true));framed(panel);
        bigButton(favorites().contains(""+id)?"♥  إزالة من المفضلة":"♡  حفظ في المفضلة",()->{favorite(id);showDetail(id);},false);
        LinearLayout arrows=horizontal();TextView prev=pill("→ السابقة",WHITE,NAVY,()->showDetail((id-1+prayers.size())%prayers.size()));TextView next=pill("التالية ←",GOLD,DEEP,()->showDetail((id+1)%prayers.size()));arrows.addView(prev,new LinearLayout.LayoutParams(0,dp(52),1));LinearLayout.LayoutParams q=new LinearLayout.LayoutParams(0,dp(52),1);q.setMargins(dp(8),0,0,0);arrows.addView(next,q);marginAdd(body,arrows,22);
        marginAdd(body,tx("حجم خط الصلاة",14,NAVY,true),5);SeekBar slider=new SeekBar(this);slider.setMax(17);slider.setProgress(Math.max(0,Math.min(17,font-17)));marginAdd(body,slider,15);slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int n,boolean u){font=n+17;content.setTextSize(font+4);prefs.edit().putInt("font",font).apply();}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});
    }
    private void showCounter(){
        shell("المسبحة الإلكترونية","counter");
        marginAdd(body,mid("✝",36,GOLD,true),3);
        marginAdd(body,mid("صلاة واحدة في كل ضغطة",22,NAVY,true),6);
        marginAdd(body,mid("نص جديد مع كل صلاة، دون تغيّر أبعاد البطاقة",13,MUTED,false),15);

        // The card dimensions DO NOT depend on the length of the prayer.
        // Long prayers scroll within a dedicated fixed-height panel.
        LinearLayout prayerCard=column();
        prayerCard.setBackground(bordered(WHITE,LINE,22));
        prayerCard.setElevation(dp(3));
        padding(prayerCard,16,14);
        LinearLayout.LayoutParams cardParams=sz(-1,-2);
        cardParams.bottomMargin=dp(15);
        body.addView(prayerCard,cardParams);

        TextView indexLabel=mid("",14,GOLD,true);
        prayerCard.addView(indexLabel,sz(-1,28));
        View rule=new View(this);rule.setBackgroundColor(LINE);
        LinearLayout.LayoutParams ruleParams=sz(-1,1);
        ruleParams.topMargin=dp(7);ruleParams.bottomMargin=dp(8);
        prayerCard.addView(rule,ruleParams);

        // Fixed-height reader; the TextView size stays the same for every prayer.
        ScrollView prayerScroller=new ScrollView(this);
        prayerScroller.setFillViewport(true);
        prayerScroller.setVerticalScrollBarEnabled(true);
        prayerScroller.setScrollbarFadingEnabled(true);
        int screenHeight=(int)(getResources().getDisplayMetrics().heightPixels/getResources().getDisplayMetrics().density);
        int readerHeight=Math.max(170,Math.min(275,(int)(screenHeight*.27f)));
        prayerCard.addView(prayerScroller,sz(-1,readerHeight));
        TextView currentPrayer=prayerText("",Math.max(21,Math.min(25,font+2)));
        padding(currentPrayer,8,13);
        prayerScroller.addView(currentPrayer,new ScrollView.LayoutParams(-1,-1));

        TextView scrollHint=mid("للصلوات الطويلة: مرّر النص داخل الإطار",11,MUTED,false);
        prayerCard.addView(scrollHint,sz(-1,27));
        View rule2=new View(this);rule2.setBackgroundColor(LINE);
        prayerCard.addView(rule2,sz(-1,1));
        TextView amen=mid("✦  آمين  ✦",13,GOLD,true);
        prayerCard.addView(amen,sz(-1,34));

        LinearLayout circle=block(0xFFF1E7D3,23);
        circle.setGravity(Gravity.CENTER);
        circle.addView(mid("✦  عدد الصلوات  ✦",13,GOLD,true));
        TextView number=mid("",49,NAVY,true);
        circle.addView(number);
        LinearLayout.LayoutParams cl=sz(-1,111);
        cl.bottomMargin=dp(13);body.addView(circle,cl);

        Runnable refresh=()->{
            int index=prayers.isEmpty()?0:(count%prayers.size());
            number.setText(String.valueOf(count));
            indexLabel.setText("الصلاة "+(index+1)+" من "+prayers.size());
            currentPrayer.setText(prayers.isEmpty()?"يا ربي يسوع المسيح ارحمني أنا الخاطئ.":prayers.get(index));
            prayerScroller.scrollTo(0,0);
            currentPrayer.setAlpha(.65f);
            currentPrayer.animate().alpha(1f).setDuration(180).start();
        };
        refresh.run();

        TextView plus=pill("✝   الصلاة التالية   +١",GOLD,DEEP,()->{
            if(count<Integer.MAX_VALUE)count++;
            saveCount();refresh.run();
            number.animate().scaleX(1.05f).scaleY(1.05f).setDuration(110)
                .withEndAction(()->number.animate().scaleX(1f).scaleY(1f).setDuration(120).start()).start();
        });
        marginAdd(body,plus,10);
        bigButton("↶  الصلاة السابقة",()->{
            count=Math.max(0,count-1);saveCount();refresh.run();
        },false);
        TextView reset=mid("تصفير العداد والعودة إلى أول صلاة",12,MUTED,false);
        padding(reset,9,9);add(reset);
        reset.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("البدء من جديد")
            .setMessage("هل تريد تصفير العداد والرجوع إلى أول صلاة؟")
            .setNegativeButton("إلغاء",null)
            .setPositiveButton("تصفير",(d,w)->{count=0;saveCount();refresh.run();}).show());
        LinearLayout note=block(0xFFF1E9D9,15);
        note.addView(mid("يُحفظ العدد وترتيب الصلاة تلقائيًا",12,NAVY,false));
        framed(note);
    }
    private void saveCount(){prefs.edit().putInt("counter",count).apply();}
    @Override public void onBackPressed(){if(!current.equals("home"))showHome();else super.onBackPressed();}
    @Override protected void onPause(){super.onPause();if(audio!=null&&audio.isPlaying()){audio.pause();music=false;if(audioButton!=null)audioButton.setText("♫ كتم");}}
    @Override protected void onDestroy(){if(audio!=null){audio.release();audio=null;}super.onDestroy();}
}
