package by.local.diaryplus;

public class ReleaseVersionTest {
 public static void main(String[] args) {
  String[][] newer={{"0.3.2","0.3.1"},{"v0.10.0","0.9.9"},{"1.0.0","0.99.99"},{"0.3.10","0.3.9"},{"999999999999.0.0","1.0.0"}};
  for(String[] pair:newer){
   if(new ReleaseVersion(pair[0]).compareTo(new ReleaseVersion(pair[1]))<=0)throw new AssertionError(pair[0]);
   if(new ReleaseVersion(pair[1]).compareTo(new ReleaseVersion(pair[0]))>=0)throw new AssertionError(pair[1]);
  }
  if(new ReleaseVersion("v0.3.1").compareTo(new ReleaseVersion("0.3.1"))!=0)throw new AssertionError();
  for(String invalid:new String[]{"","latest","0.3","0.3.2-beta","0.3.2+build","01.3.2","0.-1.2","0.3.2/other"}){
   try{new ReleaseVersion(invalid);throw new AssertionError(invalid);}catch(IllegalArgumentException expected){}
  }
  System.out.println("PASS release version numeric ordering, equal/older versions and invalid tags");
 }
}
