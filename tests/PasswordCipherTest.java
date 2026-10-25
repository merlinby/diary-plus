package by.local.diaryplus;
import javax.crypto.*;
public class PasswordCipherTest {
 public static void main(String[] args)throws Exception{
  KeyGenerator g=KeyGenerator.getInstance("AES");g.init(256);javax.crypto.SecretKey key=g.generateKey();String p="Тестовый-пароль-123";
  String a=PasswordCipher.encrypt(key,"1:login",p),b=PasswordCipher.encrypt(key,"1:login",p);
  if(a.equals(b)||a.contains(p)||!PasswordCipher.decrypt(key,"1:login",a).equals(p))throw new AssertionError();
  for(String identity:new String[]{"2:login","1:other"}){try{PasswordCipher.decrypt(key,identity,a);throw new AssertionError();}catch(AEADBadTagException expected){}}
  String altered=a.substring(0,a.length()-4)+"AAAA";try{PasswordCipher.decrypt(key,"1:login",altered);throw new AssertionError();}catch(AEADBadTagException expected){}
  System.out.println("PASS AES-GCM roundtrip, random IV, tamper rejection, profile/login binding");
 }
}
