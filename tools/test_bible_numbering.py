from import_annual import BOOKS,parse_ref
import re
count=0
for k,b in BOOKS.items():
 for ch,c in enumerate(b['chapters'],1):
  nums=list(map(int,re.findall(r'[0-9]+',c['text'])))
  if not nums or nums!=list(range(1,len(nums)+1)):
   raise ValueError(f'Invalid verse numbering: {k}/{ch}: {nums}')
  count+=1
assert parse_ref('متى 17:25-27')['ev']==27
assert parse_ref('متى 21:31-32')['v']==31
assert parse_ref('حزقيال 38:22-23')['ev']==23
assert parse_ref('المزامير 150:1-6')['ev']==6
assert parse_ref('المزامير 150:6-6')['v']==6
print(f'PASS: {count} chapters, sequential verse numbers and regression references')
