package by.local.diaryplus;
import android.content.Context;
import android.security.keystore.*;
import android.util.Base64;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;

final class CredentialsVault {
 private static final String ALIAS="diary-plus-password-key-v1";
 static boolean has(Context c,int slot){return new File(c.getFilesDir(),"password-"+slot).exists();}
 static boolean any(Context c){return has(c,1)||has(c,2);}
 private static javax.crypto.SecretKey key(boolean create)throws Exception{
  KeyStore ks=KeyStore.getInstance("AndroidKeyStore");ks.load(null);
  if(!ks.containsAlias(ALIAS)&&create){KeyGenerator generator=KeyGenerator.getInstance("AES","AndroidKeyStore");
   KeyGenParameterSpec.Builder builder=new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setKeySize(256).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setUserAuthenticationRequired(true);
   if(android.os.Build.VERSION.SDK_INT>=30)builder.setUserAuthenticationParameters(30,KeyProperties.AUTH_BIOMETRIC_STRONG|KeyProperties.AUTH_DEVICE_CREDENTIAL);else builder.setUserAuthenticationValidityDurationSeconds(30);
   generator.init(builder.build());generator.generateKey();
  }
  javax.crypto.SecretKey k=(javax.crypto.SecretKey)ks.getKey(ALIAS,null);if(k==null)throw new java.security.KeyStoreException("Missing key");return k;
 }
 static void save(Context c,int slot,String login,String password)throws Exception{
  ProfileStore.write(c.getFilesDir(),"password-"+slot,PasswordCipher.encrypt(key(true),slot+":"+login,password));
 }
 static String load(Context c,int slot,String login)throws Exception{
  if(!has(c,slot))return "";return PasswordCipher.decrypt(key(false),slot+":"+login,ProfileStore.read(c.getFilesDir(),"password-"+slot,""));
 }

 static void delete(Context c,int slot)throws IOException{File file=new File(c.getFilesDir(),"password-"+slot);if(file.exists()&&!file.delete())throw new IOException("Cannot delete password");}
}
