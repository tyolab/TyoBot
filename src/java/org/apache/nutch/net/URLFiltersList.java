package org.apache.nutch.net;

import org.apache.hadoop.conf.Configuration;

public class URLFiltersList extends URLFilters {

  public URLFiltersList(Configuration conf) {
    super(URLFilterList.X_POINT_ID, URLFiltersList.class, conf);
  }

}
