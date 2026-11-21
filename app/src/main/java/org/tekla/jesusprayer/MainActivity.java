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
