from pathlib import Path
import json, shutil, hashlib
from PIL import Image
root=Path(__file__).resolve().parents[1]; assets=root/'app/src/main/assets'; out=root/'public_site'
plan=json.loads((assets/'annual_plan.js').read_text().split('=',1)[1].strip().rstrip(';'))
assert isinstance(plan,dict) and set(plan)=={str(i) for i in range(1,366)} and all(len(x)==3 for x in plan.values()), 'Annual plan incomplete; refusing publication'
if out.exists(): shutil.rmtree(out)
shutil.copytree(root/'docs',out); app=out/'app'; shutil.copytree(assets,app)
for f in (root/'web').iterdir():
 if f.name!='sw-template.txt': shutil.copy2(f,app/f.name)
for n in (192,512):
 im=Image.open(assets/'launch-icon.png').convert('RGBA'); im.thumbnail((n,n)); canvas=Image.new('RGBA',(n,n),'#350a17'); canvas.alpha_composite(im,((n-im.width)//2,(n-im.height)//2)); canvas.convert('RGB').save(app/f'icon-{n}.png')
p=app/'index.html'; s=p.read_text(); s=s.replace('</head>','<link rel="manifest" href="manifest.webmanifest"><link rel="apple-touch-icon" href="icon-192.png"><meta name="apple-mobile-web-app-capable" content="yes"><meta name="apple-mobile-web-app-status-bar-style" content="black-translucent"><link rel="stylesheet" href="web.css"></head>'); s=s.replace('<script src="app.js">','<script src="web-bridge.js"></script><script src="app.js">'); s=s.replace('<script>boot();','<script src="web-ui.js"></script><script>boot();'); p.write_text(s)
files=['./']+['./'+f.name for f in sorted(app.iterdir()) if f.is_file() and f.name!='sw.js']; digest=hashlib.sha256();
for name in files[1:]: digest.update((app/name[2:]).read_bytes())
(app/'sw.js').write_text((root/'web/sw-template.txt').read_text().replace('__CACHE__','notety-v32-'+digest.hexdigest()[:16]).replace('__FILES__',json.dumps(files)))
print('PWA packaged:',len(files)-1,'files, 365 days, 1095 readings')
