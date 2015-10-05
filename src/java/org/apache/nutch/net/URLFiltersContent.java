package org.apache.nutch.net;

import org.apache.hadoop.conf.Configuration;

public class URLFiltersContent extends URLFilters {

  public URLFiltersContent(Configuration conf) {
    super(URLFilterContent.X_POINT_ID, URLFiltersContent.class, conf);
  }

}
