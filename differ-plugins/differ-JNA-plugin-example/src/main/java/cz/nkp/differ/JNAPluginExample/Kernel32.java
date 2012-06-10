package cz.nkp.differ.JNAPluginExample;

import com.sun.jna.*;
import com.sun.jna.win32.*;

public interface Kernel32 extends StdCallLibrary
{
   public static class SYSTEMTIME extends Structure
   {
      public short wYear;
      public short wMonth;
      public short wDayOfWeek;
      public short wDay;
      public short wHour;
      public short wMinute;
      public short wSecond;
      public short wMilliseconds;
   }

   void GetLocalTime (SYSTEMTIME result);
}