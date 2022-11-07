package com.scu.utils;

public class Logger extends BaseLogger
{
private static Logger mSingleton = null;


   // Create the logger singleton
   // If you don't want to use the default process and application names with the
   // ODK then change the values before making the first call to getLogger.
   public static Logger getLogger()
   {
      if(mSingleton == null)
      {
         mSingleton = new Logger();

      }
      return mSingleton;
   }

   public static void logInfo(String msg)
   {
      getLogger().log(MEDIUM, msg);
   }   

   public static void logError(Exception ex, String msg)
   {
      getLogger().log(MEDIUM, ex.getMessage());
      getLogger().log(MEDIUM, msg);
   }   
   
   
   private Logger()
   {
   }



   public String logSpecific(int a_iLevel, String msg)
   {

   String caller;
      if (a_iLevel > m_iLogLevel)
      {
        return "";
      }
      caller = getCaller(this.getClass().getName());
      logSpecific(a_iLevel, caller, msg );
      return  msg;
   }

   public String getCaller(String callee)
   {
   String locn = "";
   Throwable st = new Throwable();
   StackTraceElement [] stels = null;
   String sthis = callee;
      
     stels = st.getStackTrace();
     
     // To use callee correctly should search down the stack until
     // callee is found then search further down to find who
     // called the callee...

     // The caller should be the third element because
     // [0] line for the exception
     // [1] method of exception
     // [2] method of caller

     if(stels != null)
     {
        try
        {
           for(int i = 1; i < stels.length; i++)
           {
              if(sthis.compareTo(stels[i].getClassName()) != 0)
              {
                 int lidx;
                 locn = stels[i].getClassName();
                 lidx = locn.lastIndexOf(".");
                 if(lidx > 0)
                    locn = locn.substring(lidx+1);
                 //locn = stels[i].getClassName() + "." + stels[i].getMethodName();
                 locn += "." + stels[i].getMethodName();
                 break;
              }
           }
        }
        catch(Exception ex)
        {
           ex.printStackTrace();
        }
     }
     return locn;
   }
}
