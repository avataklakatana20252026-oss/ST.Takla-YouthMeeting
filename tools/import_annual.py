#!/usr/bin/env python3
"""Import only reading references from the official daily plan; validate against local Bible."""
import json,re,unicodedata,urllib.request,time,concurrent.futures,sys
from html.parser import HTMLParser
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]; AS=ROOT/'app/src/main/assets'
BOOKS=json.JSONDecoder().raw_decode((AS/'bible_data.js').read_text().split('=',1)[1])[0]
NAMES='التكوين|الخروج|اللاويين|العدد|التثنية|يشوع|القضاة|راعوث|صموئيل الأول|صموئيل الثاني|الملوك الأول|الملوك الثاني|أخبار الأيام الأول|أخبار الأيام الثاني|عزرا|نحميا|طوبيا|يهوديت|أستير|أيوب|المزامير|أمثال|الجامعة|نشيد الأنشاد|الحكمة|يشوع بن سيراخ|إشعياء|إرميا|مراثي إرميا|باروخ|حزقيال|دانيال|هوشع|يوئيل|عاموس|عوبديا|يونان|ميخا|ناحوم|حبقوق|صفنيا|حجي|زكريا|ملاخي|مكابيين الأول|مكابيين الثاني|متى|مرقس|لوقا|يوحنا|أعمال الرسل|رومية|كورنثوس الأولى|كورنثوس الثانية|غلاطية|أفسس|فيلبي|كولوسي|تسالونيكي الأولى|تسالونيكي الثانية|تيموثاوس الأولى|تيموثاوس الثانية|تيطس|فليمون|العبرانيين|يعقوب|بطرس الأولى|بطرس الثانية|يوحنا الأولى|يوحنا الثانية|يوحنا الثالثة|يهوذا|رؤيا يوحنا'.split('|')
def norm(s):
 s=''.join(c for c in unicodedata.normalize('NFKD',s) if not unicodedata.combining(c));return re.sub(r'[^\w]','',s.translate(str.maketrans('أإآىةـ','ااايه '))).lower()
ALIASES={}
for i,name in enumerate(NAMES,1):
 k={19:'19-1',21:'21-1',32:'32-1'}.get(i,f'{i:02}'); ALIASES[norm(name)]=k
 for a in [name.replace('الأولى','الأول').replace('الثانية','الثاني').replace('الثالثة','الثالث'),name.replace('الأول','الأولى').replace('الثاني','الثانية'),name.removeprefix('ال')]: ALIASES[norm(a)]=k
for a,k in {'مزمور':'21-1','المزمور':'21-1','مزامير':'21-1','الأمثال':'22','امثال':'22','الامثال':'22','اعمال':'51','رؤيا':'73','الرؤيا':'73','رومية':'52','روميه':'52','غلاطيه':'55','كولوسي':'58','كولوسى':'58','تسالونيكي 1':'59','تسالونيكي 2':'60','فليمون':'64','العبرانيون':'65','عبرانيين':'65','تيموثاوس 1':'61','تيموثاوس 2':'62','يوحنا 1':'69','يوحنا 2':'70','يوحنا 3':'71','كورنثوس 1':'53','كورنثوس 2':'54','صموئيل 1':'09','صموئيل 2':'10','ملوك 1':'11','ملوك 2':'12','الملوك 1':'11','الملوك 2':'12','أخبار الأيام 1':'13','أخبار الأيام 2':'14','بطرس 1':'67','بطرس 2':'68','دانيآل':'32-1','مراثي':'29','نشيد الانشاد':'24'}.items():ALIASES[norm(a)]=k
for a,k in list(ALIASES.items()):
 for p in ['سفر','انجيل','رسالة']:
  ALIASES[norm(p)+a]=k
DIG=str.maketrans('٠١٢٣٤٥٦٧٨٩۰۱۲۳۴۵۶۷۸۹','01234567890123456789')
# Source headings contain chapter:verse - verse or chapter:verse إلى chapter:verse.
RX=re.compile(r'^\s*(.+?)\s+(\d+)\s*:\s*(\d+)\s*(?:[-–—−]|إلى|الى|إلي|الي)\s*(?:(.*?)\s*(\d+)\s*:\s*)?(\d+)\s*$')
SINGLE=re.compile(r'^\s*(.+?)\s+(\d+)\s*:\s*(\d+)\s*$')
class Headings(HTMLParser):
 def __init__(self):super().__init__(convert_charrefs=True);self.tag=None;self.parts=[];self.head=[]
 def handle_starttag(self,tag,attrs):
  if tag in ('h1','h2','h3','h4','h5','h6'):
   self.tag=tag;self.parts=[]
 def handle_data(self,s):
  if self.tag:self.parts.append(s)
 def handle_endtag(self,tag):
  if tag==self.tag:self.head.append((tag,' '.join(''.join(self.parts).split())));self.tag=None

def parse_ref(raw):
 s=raw.translate(DIG);s=re.sub('[\u200e\u200f\u061c\u202a-\u202e]','',s).strip();m=RX.fullmatch(s)
 if m:
  name,c,v,endname,ec,ev=m.groups();c=int(c);v=int(v);ec=int(ec or c);ev=int(ev)
 elif (m:=SINGLE.fullmatch(s)):
  name,c,v=m.groups();c=int(c);v=int(v);ec=c;ev=v;endname=None
 else:return None
 k=ALIASES.get(norm(name));endk=ALIASES.get(norm(endname)) if endname and endname.strip() else k
 if not k or endk!=k:raise ValueError(f'Unknown or cross-book reference: {raw}')
 if not 1<=c<=ec<=len(BOOKS[k]['chapters']):raise ValueError(f'Invalid chapter range: {raw}')
 for ch in range(c,ec+1):
  text=BOOKS[k]['chapters'][ch-1]['text'];nums=list(map(int,re.findall(r'[0-9]+',text)))
  if nums!=list(range(1,len(nums)+1)):raise ValueError(f'Non-sequential local verse numbers: {k}/{ch}')
  lo=v if ch==c else 1;hi=ev if ch==ec else len(nums)
  if not 1<=lo<=hi<=len(nums):raise ValueError(f'Invalid verse range: {raw}, local chapter {ch}')
 return dict(label=raw,k=k,c=c,v=v,ec=ec,ev=ev)

def extract(html,n):
 h=Headings();h.feed(html);selected=[]
 for tag,raw in h.head:
  if tag!='h5':continue
  ref=parse_ref(raw)
  if ref and ref not in selected:selected.append(ref)
 if len(selected)!=3:raise ValueError(f'Day {n}: expected 3 distinct reading headings, got {len(selected)}: {h.head[:16]}')
 return selected

def fetch(n):
 url=f'https://1year.alketabalmokadas.net/day/{n}'
 for attempt in range(3):
  try:
   req=urllib.request.Request(url,headers={'User-Agent':'Notety-Reading-Plan-Reference-Importer/1.0','Accept':'text/html'})
   with urllib.request.urlopen(req,timeout=30) as r:
    if r.geturl().rstrip('/')!=url:raise ValueError(f'Unexpected redirect {r.geturl()}')
    html=r.read().decode(r.headers.get_content_charset() or 'utf-8')
   return n,extract(html,n)
  except Exception as e:
   if attempt==2:raise RuntimeError(f'Day {n}: {e}') from e
   time.sleep(2**attempt)

def main():
 result={}
 with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:
  for n,refs in pool.map(fetch,range(1,366)):
   result[str(n)]=refs
   if n%25==0:print(f'Validated {n}/365 days',flush=True)
 assert len(result)==365 and sum(map(len,result.values()))==1095
 # Audit coverage; do not silently imply inclusion of the deuterocanonical books.
 coverage={r['k'] for refs in result.values() for r in refs}
 report={'days':365,'readings':1095,'books':sorted(coverage),'local_books_not_in_plan':sorted(set(BOOKS)-coverage),'source':'https://1year.alketabalmokadas.net/msg/706'}
 (AS/'annual_plan.js').write_text('window.ANNUAL_PLAN='+json.dumps(result,ensure_ascii=False,separators=(',',':'))+';\n',encoding='utf-8')
 (ROOT/'annual_audit.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8');print(json.dumps(report,ensure_ascii=False),flush=True)
if __name__=='__main__':main()
