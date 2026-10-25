const {JSDOM}=require('jsdom'),fs=require('fs'),assert=require('node:assert/strict');
const script=fs.readFileSync(require('path').join(__dirname,'../app/src/main/assets/login.js'),'utf8');
function page(html,url='https://diary.e-schools.by/'){const d=new JSDOM(html,{url,runScripts:'outside-only'});d.window.HTMLElement.prototype.getClientRects=function(){return this.type==='hidden'?[]:[{}]};return d;}
function close(d){d.window.__diaryPlusLoginObserver?.disconnect();d.window.close();}
function inject(d,user,password=''){return d.window.eval(script.replace('__SAVED_LOGIN_JSON__',JSON.stringify({username:user,password})));}
(async()=>{
 let inspection=page('<p>Дневник уже открыт</p>');
 const inspect=d=>d.window.eval(script.replace('__SAVED_LOGIN_JSON__','{"inspect":true}'));
 assert.equal(inspect(inspection),null);close(inspection);
 inspection=page('<form><input name="login" value="user-a"><input type="password" value="filled"></form>');
 assert.equal(inspect(inspection).passwordNeeded,false);close(inspection);
 inspection=page('<form><input name="login"><input type="password"></form>');
 assert.equal(inspect(inspection).passwordNeeded,true);assert.equal(inspection.window.document.querySelector('[type=password]').value,'');close(inspection);
 console.log('PASS inspect: no password access for open diary or already filled form; empty login form needs password');

 let d=page('<form><input name="login"><input type="password"></form>');let submitted=false;d.window.document.querySelector('form').onsubmit=()=>submitted=true;inject(d,'user-a','secret');assert.equal(d.window.document.querySelector('[name=login]').value,'user-a');assert.equal(d.window.document.querySelector('[type=password]').value,'secret');assert(!submitted);close(d);console.log('PASS username/password fill, no automatic submission');
 d=page('<form><input name="login" value="user-b"><input type="password"></form>');inject(d,'user-a','secret');assert.equal(d.window.document.querySelector('[type=password]').value,'');close(d);console.log('PASS no password for mismatching username');
 d=page('<input name="login"><input name="search">');inject(d,'user-a','secret');assert.equal(d.window.document.querySelector('[name=login]').value,'');close(d);console.log('PASS no change to non-login forms');
 d=page('<form><input name="login"><input type="password"></form>','https://example.com/');inject(d,'user-a','secret');assert.equal(d.window.document.querySelector('[type=password]').value,'');close(d);console.log('PASS no credentials on external origin');
 d=page('');inject(d,'user-a','secret');d.window.document.body.innerHTML='<form><input autocomplete="username"><input type="password"></form>';await new Promise(r=>setTimeout(r,1));assert.equal(d.window.document.querySelector('[type=password]').value,'secret');close(d);console.log('PASS dynamically rendered login form');
 d=page('<form><input name="login"><input type="password" value="typed"></form>');inject(d,'user-a','secret');assert.equal(d.window.document.querySelector('[type=password]').value,'typed');close(d);console.log('PASS preserve user-entered password');
 d=page('');inject(d,'user-a','secret');d.window.eval(script.replace('__SAVED_LOGIN_JSON__','null'));d.window.document.body.innerHTML='<form><input name="login"><input type="password"></form>';await new Promise(r=>setTimeout(r,1));assert.equal(d.window.document.querySelector('[type=password]').value,'');close(d);console.log('PASS clear credential observer on lock');
})().catch(e=>{console.error(e);process.exit(1)});
