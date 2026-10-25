package by.local.diaryplus;
import java.io.*;
import java.nio.charset.StandardCharsets;

// Profile labels, usernames and last selected slot. No passwords, tokens or cookies.
final class ProfileStore {
 static String read(File dir,String key,String fallback){
  try(FileInputStream in=new FileInputStream(new File(dir,key))){byte[] b=new byte[4096];int n=in.read(b);return n>0?new String(b,0,n,StandardCharsets.UTF_8):fallback;}catch(IOException e){return fallback;}
 }
 static void write(File dir,String key,String value)throws IOException{
  File tmp=File.createTempFile(key,".tmp",dir);
  try{try(FileOutputStream out=new FileOutputStream(tmp)){out.write(value.getBytes(StandardCharsets.UTF_8));out.getFD().sync();}if(!tmp.renameTo(new File(dir,key)))throw new IOException("Cannot save profile");}finally{tmp.delete();}
 }
}
