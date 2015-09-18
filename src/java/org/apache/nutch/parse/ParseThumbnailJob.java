package org.apache.nutch.parse;

import java.io.IOException;

import org.apache.avro.util.Utf8;
import org.apache.gora.mapreduce.GoraMapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.crawl.GeneratorJob;
import org.apache.nutch.metadata.Nutch;
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

  public static class ParseThumbnailMapper extends
      GoraMapper<String, WebPage, String, WebPage> {
    private ParseUtil parseUtil;

    private boolean shouldResume;

    private boolean force;

    private Utf8 batchId;

    private boolean skipTruncated;

    @Override
    public void setup(Context context) throws IOException {
      Configuration conf = context.getConfiguration();
      parseUtil = new ParseUtil(conf);
      shouldResume = conf.getBoolean(RESUME_KEY, false);
      force = conf.getBoolean(FORCE_KEY, false);
      batchId =
          new Utf8(conf.get(GeneratorJob.BATCH_ID, Nutch.ALL_BATCH_ID_STR));
      skipTruncated = conf.getBoolean(SKIP_TRUNCATED, true);
    }

    @Override
    public void map(String key, WebPage page, Context context)
        throws IOException, InterruptedException {
      Utf8 mark = Mark.FETCH_MARK.checkMark(page);
      String unreverseKey = TableUtil.unreverseUrl(key);
      if (batchId.equals(REPARSE)) {
        LOG.debug("Reparsing " + unreverseKey);
      } else {
        if (!batchId.toString().equals(Nutch.ALL_BATCH_ID_STR)
            && !NutchJob.shouldProcess(mark, batchId)) {
          LOG.info("Skipping " + TableUtil.unreverseUrl(key)
              + "; different batch id (" + mark + ")");
          return;
        }
        Utf8 parseMark = Mark.PARSE_MARK.checkMark(page);
        if (shouldResume && parseMark != null) {
          if (force) {
            LOG.info("Forced parsing " + unreverseKey + "; already parsed");
          } else {
            LOG.info("Skipping " + unreverseKey + "; already parsed");
            return;
          }
        } else {
          LOG.info("Parsing " + unreverseKey);
        }
      }

      if (skipTruncated && isTruncated(unreverseKey, page)) {
        return;
      }

      parseUtil.process(key, page);
      ParseStatus pstatus = page.getParseStatus();
      if (pstatus != null) {
        context.getCounter("ParserStatus",
            ParseStatusCodes.majorCodes[pstatus.getMajorCode()]).increment(1);
      }

      context.write(key, page);
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
