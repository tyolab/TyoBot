package org.apache.nutch.parse;

import java.io.IOException;

import org.apache.avro.util.Utf8;
import org.apache.gora.mapreduce.GoraMapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.crawl.GeneratorJob;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.net.URLFilterException;
import org.apache.nutch.net.URLFiltersList;
import org.apache.nutch.storage.Mark;
import org.apache.nutch.storage.ParseStatus;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;
import org.apache.nutch.util.TableUtil;

public class ParseThumbnailJob extends ParserJob {

  static {
    ParserJob.FIELDS.add(WebPage.Field.THUMB_URL);
    ParserJob.mapperClass = ParseThumbnailMapper.class;
  }

  public static class ParseThumbnailMapper extends ParserMapper {

    private ParseThumbUtil parseThumbUtil;

    private URLFiltersList filtersList;

    @Override
    public void setup(Context context) throws IOException {
      super.setup(context);

      Configuration conf = context.getConfiguration();
      parseThumbUtil = new ParseThumbUtil(conf);

      filtersList = parseThumbUtil.getFiltersList();
    }

    @Override
    public void map(String key, WebPage page, Context context)
        throws IOException, InterruptedException {
      Utf8 mark = Mark.FETCH_MARK.checkMark(page);
      String unreverseKey = TableUtil.unreverseUrl(key);

      String newUrl = null;
      try {
        newUrl = filtersList.filter(unreverseKey);
      } catch (URLFilterException e) {
        LOG.warn("error filtering " + newUrl);
        return;
      }

      if (newUrl != null) {

        if (batchId.equals(REPARSE)) {
          LOG.debug("Reparsing " + unreverseKey);
        } else {
          if (!batchId.toString().equals(Nutch.ALL_BATCH_ID_STR)
              && !NutchJob.shouldProcess(mark, batchId)) {
            LOG.info("Skipping " + TableUtil.unreverseUrl(key)
                + "; different batch id (" + mark + ")");
            return;
          }
          Utf8 parseMark = Mark.PARSETHUMBNAIL_MARK.checkMark(page);
          if (shouldResume && parseMark != null) {
            if (force) {
              LOG.info("Forced parsing " + unreverseKey + "; already parsed");
            } else {
              LOG.info("Skipping " + unreverseKey + "; already parsed");
              return;
            }
          } else {
            LOG.info("Finding thumbnail link for " + unreverseKey);
          }
        }

        if (skipTruncated && isTruncated(unreverseKey, page)) {
          return;
        }

        parseThumbUtil.process(context, key, page);
        // ParseStatus pstatus = page.getParseStatus();
        // if (pstatus != null) {
        // context.getCounter("ParserStatus",
        // ParseStatusCodes.majorCodes[pstatus.getMajorCode()]).increment(1);
        // }
        //
        // context.write(key, page);
      }
    }
  }

  public ParseThumbnailJob() {

  }

  public ParseThumbnailJob(Configuration conf) {
    super(conf);
  }

  public static void main(String[] args) throws Exception {
    final int res =
        ToolRunner.run(NutchConfiguration.create(), new ParseThumbnailJob(),
            args);
    System.exit(res);
  }

}
