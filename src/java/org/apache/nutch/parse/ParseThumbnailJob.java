package org.apache.nutch.parse;

import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.NutchConfiguration;

public class ParseThumbnailJob extends ParserJob {

  static {
    ParserJob.FIELDS.add(WebPage.Field.THUMB_URL);
  }

  public static void main(String[] args) throws Exception {
    final int res =
        ToolRunner.run(NutchConfiguration.create(), new ParseThumbnailJob(),
            args);
    System.exit(res);
  }

}
