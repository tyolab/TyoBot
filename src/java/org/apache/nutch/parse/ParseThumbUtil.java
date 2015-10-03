package org.apache.nutch.parse;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.nutch.net.URLFiltersList;
import org.apache.nutch.net.URLNormalizers;

public class ParseThumbUtil extends Configured {

  private Configuration conf;

  private URLNormalizers normalizers;

  private URLFiltersList filters;

  public ParseThumbUtil(Configuration conf) {
    super(conf);
    setConf(conf);
  }

  @Override
  public Configuration getConf() {
    return conf;
  }

  @Override
  public void setConf(Configuration conf) {
    this.conf = conf;

    normalizers = new URLNormalizers(conf, URLNormalizers.SCOPE_OUTLINK);

    filters = new URLFiltersList(conf);
  }

}
