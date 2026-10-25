(function (credentials) {
 const savedLogin=typeof credentials==='string'?credentials:(credentials&&credentials.username)||'';
 const savedPassword=typeof credentials==='object'&&credentials?credentials.password||'':'';
 'use strict';
 if(location.origin !== 'https://diary.e-schools.by')return null;
 function candidate(){
  const visible=e=>!e.disabled&&!e.readOnly&&e.getAttribute('aria-hidden')!=='true'&&e.type!=='hidden'&&e.getClientRects().length>0;
  const fields=[...document.querySelectorAll('input')].filter(e=>['text','email','tel','number'].includes(e.type)&&visible(e));
  const named=fields.filter(e=>e.autocomplete==='username'||/^(username|login|user_login|user_name|email|identifier)$/i.test(e.name)||/^(username|login|user_login|user_name|email|identifier)$/i.test(e.id));
  // Require a login form, so search/profile fields are never changed.
  const passwords=[...document.querySelectorAll('input[type="password"]')].filter(visible);
  const password=passwords.length===1?passwords[0]:null;
  if(!password)return null;
  const group=password.form||password.parentElement?.parentElement;
  const inGroup=fields.filter(e=>group&&group.contains(e));
  const matched=named.filter(e=>inGroup.includes(e));
  return matched.length===1?matched[0]:inGroup.length===1?inGroup[0]:null;
 }
 function fill(){const input=candidate();if(!input||!savedLogin)return false;
  const setter=Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value').set;
  const set=(field,value)=>{setter.call(field,value);field.dispatchEvent(new Event('input',{bubbles:true}));field.dispatchEvent(new Event('change',{bubbles:true}));};
  if(!input.value)set(input,savedLogin);
  // Never insert this profile's password for a different username.
  if(input.value!==savedLogin)return false;
  const group=input.form||input.parentElement?.parentElement;
  const password=group&&group.querySelector('input[type="password"]');
  if(password&&!password.value&&!password.disabled&&!password.readOnly&&savedPassword)set(password,savedPassword);
  return true;
 }

 if(credentials&&credentials.inspect){const input=candidate();if(!input)return null;const group=input.form||input.parentElement?.parentElement;const password=group&&group.querySelector('input[type="password"]');return {username:input.value,passwordNeeded:!!password&&!password.value};}
 // Replacing an observer also cancels any old pending login suggestion.
 if(window.__diaryPlusLoginObserver){window.__diaryPlusLoginObserver.disconnect();window.__diaryPlusLoginObserver=null;}
 if(!savedLogin){const input=candidate();return input?input.value:null;}
 fill();
 // SPA login forms may appear after navigation without a full page reload.
 const observer=new MutationObserver(()=>fill());observer.observe(document.documentElement,{childList:true,subtree:true});window.__diaryPlusLoginObserver=observer;

 return null;
})(__SAVED_LOGIN_JSON__)
