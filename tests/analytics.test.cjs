const {JSDOM}=require('jsdom');const fs=require('fs');const assert=require('node:assert/strict');
const source=fs.readFileSync(require('path').join(__dirname,'../app/src/main/assets/analytics.js'),'utf8');
const tick=()=>new Promise(r=>setTimeout(r,15));
async function scenario(name){
 const dom=new JSDOM('<html><body>Official diary mock</body></html>',{url:'https://diary.e-schools.by/',runScripts:'outside-only'});const w=dom.window;w.Headers=Headers;w.AbortController=AbortController;
 const RealDate=w.Date;w.Date=class extends RealDate{constructor(...args){super(...(args.length?args:['2026-10-25T12:00:00Z']));}static now(){return +new RealDate('2026-10-25T12:00:00Z');}};
 w.localStorage.setItem('e-school-auth-diary',JSON.stringify({authToken:'FAKE_TEST_TOKEN',person:{activeChild:'child-a'}}));
 const requests=[];let me=0;let signal;
 w.fetch=async(url,o)=>{requests.push({url,method:o.method,body:o.body,headers:o.headers});signal=o.signal;let status=200,data={};assert.equal(new URL(url).origin,'https://diary.e-schools.by');assert.equal(o.redirect,'error');
  if(url.endsWith('/admin/auth/me')){me++;if(name==='401'||name==='fallback'&&me===1)status=401;else data=name==='student'?{roles:['STUDENT'],profile_id:'child-a',school_id:'school',class_uuid:'class'}:{roles:['PARENT'],students_for_parent:[{profile_id:'child-a',school_id:'school',full_name:'Тест А'},{profile_id:'child-b',school_id:'school',full_name:'Тест Б'}]};}
  else if(url.endsWith('/week_activities'))data={week_activities:[{uuid:'week',start_ts:Date.parse('2026-08-31'),end_ts:Date.parse('2026-09-06')}]};
  else if(url.endsWith('/classes'))data={classes:[{uuid:'class'}]};
  else if(url.includes('/lessons?')){if(name==='network')throw new w.TypeError('Failed to fetch');data={days:[{date:'2026-08-31',slots:[{subject_title:'Math',lesson_mark:{mark:'1'}}]},{date:'2026-09-01',slots:[{subject_title:'Math',lesson_mark:{mark:'8'}},{subject_title:'Math',lesson_mark:{mark:'10'}},{subject_title:'Russian',lesson_mark:{mark:'н',comment:'<img src=https://evil.invalid/x>'}}]}]};if(name==='undated')delete data.days[1].date;}
  else throw Error('Unexpected request '+url);
  return {ok:status===200,status,json:async()=>data};
 };
 w.eval(source);await tick();const root=w.document.getElementById('diary-plus-analytics').shadowRoot;
 if(name==='401'){assert.match(root.querySelector('.message').textContent,/Сессия/);assert.equal(requests.length,2);}
 else if(name==='network'){assert.match(root.querySelector('.message').textContent,/Нет связи/);}
 else {assert.equal(root.querySelector('#overall').textContent,'9.00');assert.equal(root.querySelector('#subjects').textContent,'2');assert.equal(root.querySelectorAll('img').length,0);assert.equal(root.querySelector('#dateNote').hidden,name!=='undated');}
 if(name==='student')assert.equal(requests.filter(r=>r.method==='POST').length,0);
 if(name==='fallback'){assert.equal(requests[0].headers.Authorization,'FAKE_TEST_TOKEN');assert.equal(requests[1].headers.Authorization,undefined);}
 if(name==='parent'){
  root.querySelector('#child').value='child-b';root.querySelector('#child').dispatchEvent(new w.Event('change'));await tick();assert(requests.some(r=>r.url.includes('/students/child-b/lessons')));
  const before=requests.length;root.querySelector('#from').value='2026-10-01';root.querySelector('#to').value='2026-09-01';root.querySelector('.load').click();await tick();assert.equal(requests.length,before);assert.match(root.querySelector('.message').textContent,/корректный период/);
 }
 root.querySelector('.close').click();assert.equal(w.document.getElementById('diary-plus-analytics'),null);assert.equal(w.document.documentElement.style.overflow,'');assert.equal(signal.aborted,true);w.close();console.log('PASS',name);
}
(async()=>{for(const name of ['parent','student','401','fallback','network','undated'])await scenario(name);const d=new JSDOM('',{url:'https://example.com',runScripts:'outside-only'});d.window.eval(source);assert.equal(d.window.document.getElementById('diary-plus-analytics'),null);d.window.close();console.log('PASS non-diary origin');})().catch(e=>{console.error(e);process.exit(1);});
