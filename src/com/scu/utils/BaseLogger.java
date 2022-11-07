package com.scu.utils;

import java.io.PrintWriter;
import java.io.File;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.MessageFormat;
import java.io.FileOutputStream;

public class BaseLogger {

  // loglevels
  //
  public static int ALWAYS = 0;
  public static int HIGH = 1;
  public static int MEDIUM = 2;
  public static int LOW = 3;
  public static int VLOW = 4;

   protected int m_iLogLevel;
  private int m_iMaxLogSize;
  private boolean m_bIsInit;
  private String m_strLogFileName;
  private PrintWriter m_pwLogFile = null;

  public BaseLogger()
  {
    m_iLogLevel = 4;
    // Default is to only output to stdout. Setting the PrintWriter
    // to stdout causes each message to appear twice.
    //FileOutputStream fos = new FileOutputStream( FileDescriptor.out );
    //m_pwLogFile = new PrintWriter( fos );
    m_iMaxLogSize = 300000;
    m_bIsInit = false;
  }

  /*****************************************************************************
   *                                                                           *
   * Function SNLTrc()						       *
   *                                                                           *
   * Description : Constructor for the SNLTrc class                          *
   *                                                                           *
   * Scope : Static Lib                                                        *
   *                                                                           *
   *****************************************************************************/
  public BaseLogger( int a_iLogLevel, int a_iMaxLogSize ) {
    m_iLogLevel = a_iLogLevel;
    //FileOutputStream fos = new FileOutputStream( FileDescriptor.out );
    //m_pwLogFile = new PrintWriter( fos );
    m_iMaxLogSize = a_iMaxLogSize;
    m_bIsInit = false;
  }



  /*****************************************************************************
   *                                                                           *
   * Function initLog()                                                        *
   *                                                                           *
   * Description : Initialises the log file                                    *
   *                                                                           *
   * @return int : LOG_OK if success (0)                                       *
   *               LOG_NOK if failure (-1)                                     *
   *                                                                           *
   *****************************************************************************/
  public int initLog( String a_strLogFileName )
  {
    // Test if the log isn't initialized twice
    if (m_bIsInit)
    {
      return 0;
    }

    if(a_strLogFileName == null)
       return 0;

    if ( a_strLogFileName.length() > 0 )
    {
      m_strLogFileName = a_strLogFileName;
    }

    // Create/open log file
    FileOutputStream fos;
    try
    {
      fos = new FileOutputStream( m_strLogFileName, true );
    }
    catch (Exception e)
    {
      System.err.println(
          "Error in BaseLogger.InitLog. Failed to create File with arg [" +
          m_strLogFileName + "]. Error: " + e.toString());
      return -1;
    }

    m_pwLogFile = new PrintWriter(fos);
    m_bIsInit = true;

    return 0;
  }


  /*****************************************************************************
   *                                                                           *
   * Function resetLog()                                                       *
   *                                                                           *
   * Description : Resets the log file                                         *
   *                                                                           *
   * @return int : LOG_OK if success (0)                                       *
   *               LOG_NOK if failure (-1)                                     *
   *                                                                           *
   *****************************************************************************/
  public synchronized int resetLog() {
    int iRet;
    File f = new File(m_strLogFileName);

    // If it's not a valid file : create a new one
    //
    if (!f.exists()) {
      m_bIsInit = false;
      iRet = initLog( "" );
      return iRet;
    }

    // Check size
    //
    if (f.length() > m_iMaxLogSize) {
      closeLog();
      m_bIsInit = false;
      iRet = initLog( "" );
      return iRet;
    }

    // If we get here, we're using a log file that's still ok, so do nothing
    //
    return 0;
  }


  /*****************************************************************************
   *                                                                           *
   * Function log()                                                            *
   *                                                                           *
   * Description :                                                             *
   *		Writes a message to the specified log file if the log level is       *
   *		set appropriately.                                                   *
   *		"printf" like parameters can be specified in the Format parameter    *
   *      providing that sufficient suitable parameters are also supplied.     *
   *                                                                           *
   * @param [IN]int level : Logging level of the info to be logged             *
   * @param [IN]String a_strFormat : format string                             *
   * @param [IN]Object[] a_aroFormatArgs : format arguments                    *
   *                                                                           *
   * @return void                                                              *
   *                                                                           *
   *****************************************************************************/
  public int log(int a_iLevel, String a_strFormat, Object[] a_aroFormatArgs) {
    return log(a_iLevel, MessageFormat.format(a_strFormat, a_aroFormatArgs));
  }


  /*****************************************************************************
     *                                                                           *
     * Function logSpecific()                                                    *
     *                                                                           *
     * Description :                                                             *
     *		Writes a message to the specified log file if the log level is       *
     *		set appropriately.                                                   *
     *		"printf" like parameters can be specified in the Format parameter    *
     *      providing that sufficient suitable parameters are also supplied.     *
     *                                                                           *
     * @param [IN]int a_iLevel : Logging level of the info to be logged          *
     * @param [IN]String a_strFormat : format string                             *
     * @param [IN]Object[] a_aroFormatArgs : format arguments                    *
     *                                                                           *
     * @return void                                                              *
     *                                                                           *
     *****************************************************************************/
    public int logSpecific(int a_iLevel, String a_strLocation,
                    String a_strFormat ) {
      Object[] aroFormatArgs = { a_strLocation, a_strFormat };
      return log(a_iLevel, " {0}: {1}", aroFormatArgs);
    }

  /*****************************************************************************
   *                                                                           *
   * Function logSpecific()                                                    *
   *                                                                           *
   * Description :                                                             *
   *		Writes a message to the specified log file if the log level is       *
   *		set appropriately.                                                   *
   *		"printf" like parameters can be specified in the Format parameter    *
   *      providing that sufficient suitable parameters are also supplied.     *
   *                                                                           *
   * @param [IN]int a_iLevel : Logging level of the info to be logged          *
   * @param [IN]String a_strFormat : format string                             *
   * @param [IN]Object[] a_aroFormatArgs : format arguments                    *
   *                                                                           *
   * @return void                                                              *
   *                                                                           *
   *****************************************************************************/
  public int logSpecific(int a_iLevel, String a_strLocation,
                  String a_strFormat, Object[] a_aroFormatArgs) {
    Object[] aroFormatArgs = {a_strLocation,
        MessageFormat.format(a_strFormat, a_aroFormatArgs)};
    return log(a_iLevel, " {0}: {1}", aroFormatArgs);
  }


  /*****************************************************************************
   *                                                                           *
   * Function log()                                                            *
   *                                                                           *
   * Description :                                                             *
   *		Writes the specified message to the log file according to the        *
   *		current log level setting.                                           *
   *                                                                           *
   * @param [IN]int a_iLevel : Logging level of the info to be logged          *
   * @param [IN]String a_strMsg : The message to be logged                     *
   *                                                                           *
   * @return int : LOG_OK if success (0)                                       *
   *               LOG_NOK if failure (-1)                                     *
   *                                                                           *
   *****************************************************************************/
  public synchronized int log(int a_iLevel, String a_strMsg)
  {
    if (a_iLevel > m_iLogLevel)
    {
      return 0;
    }

    if (a_strMsg.length() == 0)
    {
      return -1;
    }

    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
    Date d = new Date();
    if(m_pwLogFile != null)
    {
       m_pwLogFile.write(sdf.format(d));
       m_pwLogFile.write(a_strMsg);
       if (!a_strMsg.endsWith("\n"))
          m_pwLogFile.write("\n");
       m_pwLogFile.flush();
    }
    else
    {
       System.out.print(sdf.format(d));
    }
    // This is a nuisance when the logging is already writing to stdout. Trouble is
    // there is no way to determine if the PrintWriter is already using stdout.

    System.out.println( a_strMsg );

    return 0;
  }


  /*****************************************************************************
   *                                                                           *
   * Function setLogLevel()                                                       *
   *                                                                           *
   * Description : Sets the logging level                                      *
   *                                                                           *
   * @param [IN]int a_iLevel : New logging level                                 *
   *                                                                           *
   * @return void                                                              *
   *                                                                           *
   *****************************************************************************/
  public void setLogLevel(int a_iLevel)
  {
    if ( ( a_iLevel >= ALWAYS ) && ( a_iLevel <= VLOW ) )
      m_iLogLevel = a_iLevel;
  }

  public void setLogLevel(String sLevel)
  {
     try
     {
        setLogLevel(Integer.parseInt(sLevel));
     }
     catch(Exception ex) {}

  }

  public void setLogFile(String logfile)
  {
     closeLog();
     m_bIsInit = false;
     initLog(logfile);
  }

  /*****************************************************************************
   *                                                                           *
   * Function closeLog()                                                       *
   *                                                                           *
   * Description : Closes the log file                                         *
   *                                                                           *
   * @return int : LOG_OK if success (0)                                        *
   *               LOG_NOK if failure (-1)                                      *
   *                                                                           *
   *****************************************************************************/
  int closeLog()
  {
    try
    {
       if(m_pwLogFile != null)
          m_pwLogFile.close();
    } catch ( Exception e )
    {
      System.err.println( "Error in BaseLogger.CloseLog(). PrintWriter::close failed. Error: " + e.toString() );
    }

    return 0;
  }


}
