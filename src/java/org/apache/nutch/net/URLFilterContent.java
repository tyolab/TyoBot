package org.apache.nutch.net;

/*
 * The content URL filter
 * 
 * which means only the urls that provide real content for consumption 
 */

public interface URLFilterContent extends URLFilter {

  public final static String X_POINT_ID = URLFilterContent.class.getName();

}
