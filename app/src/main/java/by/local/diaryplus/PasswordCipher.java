package by.local.diaryplus;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;

final class PasswordCipher {
 static String encrypt(javax.crypto.SecretKey key,String identity,String password)throws Exception{
  Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key);cipher.updateAAD(identity.getBytes(StandardCharsets.UTF_8));
  return Base64.getEncoder().encodeToString(cipher.getIV())+":"+Base64.getEncoder().encodeToString(cipher.doFinal(password.getBytes(StandardCharsets.UTF_8)));
 }
 static String decrypt(javax.crypto.SecretKey key,String identity,String record)throws Exception{
  String[] parts=record.split(":",2);if(parts.length!=2)throw new java.io.IOException("Invalid encrypted record");
  Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,key,new GCMParameterSpec(128,Base64.getDecoder().decode(parts[0])));cipher.updateAAD(identity.getBytes(StandardCharsets.UTF_8));return new String(cipher.doFinal(Base64.getDecoder().decode(parts[1])),StandardCharsets.UTF_8);
 }
}
