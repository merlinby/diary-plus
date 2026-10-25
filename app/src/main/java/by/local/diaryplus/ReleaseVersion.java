package by.local.diaryplus;

import java.math.BigInteger;

/** Stable release tags use vMAJOR.MINOR.PATCH (the v is optional). */
final class ReleaseVersion implements Comparable<ReleaseVersion> {
 final String name;
 private final BigInteger[] parts;
 ReleaseVersion(String tag) {
  if(tag==null||!tag.matches("v?(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)"))
   throw new IllegalArgumentException("Unsupported release version");
  name=tag.startsWith("v")?tag.substring(1):tag;
  String[] values=name.split("\\.");
  parts=new BigInteger[]{new BigInteger(values[0]),new BigInteger(values[1]),new BigInteger(values[2])};
 }
 @Override public int compareTo(ReleaseVersion other) {
  for(int i=0;i<parts.length;i++){int result=parts[i].compareTo(other.parts[i]);if(result!=0)return result;}
  return 0;
 }
}
