package org.apache.nutch.indexer.atire;

/*
 * Atire document serves as an indexable XML by Atire 
 */
public abstract class AtireDocument {
  /*
   * ymd - year month day
   * 
   * special tags, desc, folder
   */
  protected static String XML_TEMPLATE = "" + "<doc>\n"
      + "\t<title>%s</title>\n"
      + "\t<docid>%s</docid>\n" 
      + "\t<site>%s</site>\n"
      + "\t<desc>%s</desc>\n"
      + "\t<keywords>%s</keywords>\n"
      + "\t%s\n"
      // + ""
      // + ""
      + "</doc>\n";

  protected static String CONTENT_TAG = "text";

  public static void setContentTag(String tag) {
    CONTENT_TAG = tag;
  }
  
  public abstract String toXml();

}
