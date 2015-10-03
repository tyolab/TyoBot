package org.apache.nutch.net;

/*
 * The list URL filter
 * 
 * which means only the urls that provide a list of urls
 */

public interface URLFilterList extends URLFilter {

  public final static String X_POINT_ID = URLFilterList.class.getName();

}
