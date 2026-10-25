package by.local.diaryplus;
import java.io.*;
import java.nio.file.Files;

public class ProfileStoreTest {
 public static void main(String[] args)throws Exception{
  File dir=Files.createTempDirectory("diary-profiles").toFile();
  check(ProfileStore.read(dir,"last-profile","1").equals("1"));
  ProfileStore.write(dir,"profile-name-1","Александр");
  ProfileStore.write(dir,"profile-name-2","Мария");
  check(ProfileStore.read(dir,"profile-name-1","").equals("Александр"));
  check(ProfileStore.read(dir,"profile-name-2","").equals("Мария"));
  ProfileStore.write(dir,"last-profile","2");
  check(ProfileStore.read(dir,"last-profile","1").equals("2"));
  ProfileStore.write(dir,"profile-name-1","Саша");
  check(ProfileStore.read(dir,"profile-name-2","").equals("Мария"));
  check(ProfileStore.read(dir,"last-profile","1").equals("2"));
  System.out.println("PASS independent profile labels, overwrite and persisted selection");
 }
 static void check(boolean value){if(!value)throw new AssertionError();}
}
