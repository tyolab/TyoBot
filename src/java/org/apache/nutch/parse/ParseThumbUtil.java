package org.apache.nutch.parse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Map;

import org.apache.avro.util.Utf8;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.StringUtils;
import org.apache.nutch.crawl.CrawlStatus;
import org.apache.nutch.fetcher.FetcherJob;
import org.apache.nutch.net.URLFilterException;
import org.apache.nutch.net.URLFiltersContent;
import org.apache.nutch.net.URLFiltersList;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.storage.Mark;
import org.apache.nutch.storage.ParseStatus;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.TableUtil;
import org.apache.nutch.util.URLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParseThumbUtil extends ParseUtil {

  /* our log stream */
  public static final Logger LOG = LoggerFactory.getLogger(ParseUtil.class);

  // private Configuration conf;

  // private URLNormalizers normalizers;

  private URLFiltersContent filtersContent;

  public ParseThumbUtil(Configuration conf) {
    super(conf);
    setConf(conf);

    checkFetchStatus = false;
  }

  @Override
  public void setConf(Configuration conf) {
    super.setConf(conf);

    // normalizers = new URLNormalizers(conf, URLNormalizers.SCOPE_OUTLINK);
    filtersContent = new URLFiltersContent(conf);

  }

  public void getOutlinkArray(String key, WebPage page) {
    String url = TableUtil.unreverseUrl(key);

    Parse parse;
    try {
      parse = parse(url, page);
    } catch (ParserNotFound e) {
      // do not print stacktrace for the fact that some types are not mapped.
      LOG.warn("No suitable parser found: " + e.getMessage());
      return;
    } catch (final Exception e) {
      LOG.warn("Error parsing: " + url + ": "
          + StringUtils.stringifyException(e));
      return;
    }

    if (parse == null) {
      return;
    }

    final byte[] signature = sig.calculate(page);

    org.apache.nutch.storage.ParseStatus pstatus = parse.getParseStatus();
    page.setParseStatus(pstatus);
    if (ParseStatusUtils.isSuccess(pstatus)) {
      if (pstatus.getMinorCode() == ParseStatusCodes.SUCCESS_REDIRECT) {
        String newUrl = ParseStatusUtils.getMessage(pstatus);
        int refreshTime = Integer.parseInt(ParseStatusUtils.getArg(pstatus, 1));
        try {
          newUrl = normalizers.normalize(newUrl, URLNormalizers.SCOPE_FETCHER);
          if (newUrl == null) {
            LOG.warn("redirect normalized to null " + url);
            return;
          }
          try {
            newUrl = filters.filter(newUrl);
          } catch (URLFilterException e) {
            return;
          }
          if (newUrl == null) {
            LOG.warn("redirect filtered to null " + url);
            return;
          }
        } catch (MalformedURLException e) {
          LOG.warn("malformed url exception parsing redirect " + url);
          return;
        }
        page.putToOutlinks(new Utf8(newUrl), new Utf8());
        page.putToMetadata(FetcherJob.REDIRECT_DISCOVERED, TableUtil.YES_VAL);
        if (newUrl == null || newUrl.equals(url)) {
          String reprUrl =
              URLUtil.chooseRepr(url, newUrl,
                  refreshTime < FetcherJob.PERM_REFRESH_TIME);
          if (reprUrl == null) {
            LOG.warn("reprUrl==null for " + url);
            return;
          } else {
            page.setReprUrl(new Utf8(reprUrl));
          }
        }
      } else {
        page.setText(new Utf8(parse.getText()));
        page.setTitle(new Utf8(parse.getTitle()));
        ByteBuffer prevSig = page.getSignature();
        if (prevSig != null) {
          page.setPrevSignature(prevSig);
        }
        page.setSignature(ByteBuffer.wrap(signature));
        if (page.getOutlinks() != null) {
          // page.getOutlinks().clear();
          page.clearOutlinks();
        }
        final Outlink[] outlinks = parse.getOutlinks();
        final int count = 0;
        String fromHost;
        if (ignoreExternalLinks) {
          try {
            fromHost = new URL(url).getHost().toLowerCase();
          } catch (final MalformedURLException e) {
            fromHost = null;
          }
        } else {
          fromHost = null;
        }
        /*
         * don't need to limit the outlinks number
         */
        for (int i = 0; /* count < maxOutlinks && */i < outlinks.length; i++) {
          String toUrl = outlinks[i].getToUrl();
          String normailizedUrl = toUrl;
          try {
            normailizedUrl =
                normalizers.normalize(toUrl, URLNormalizers.SCOPE_OUTLINK);
            toUrl = filters.filter(normailizedUrl);
          } catch (MalformedURLException e2) {
            continue;
          } catch (URLFilterException e) {
            continue;
          }
          if (toUrl == null) {
            if (normailizedUrl != null)
              LOG.info("skipping " + normailizedUrl);
            continue;
          } else {
            // LOG.info("found link: " + toUrl);
            System.out.println("found link: " + toUrl);
          }

          Utf8 utf8ToUrl = new Utf8(toUrl);
          if (page.getFromOutlinks(utf8ToUrl) != null) {
            // skip duplicate outlinks
            continue;
          }
          String toHost;
          if (ignoreExternalLinks) {
            try {
              toHost = new URL(toUrl).getHost().toLowerCase();
            } catch (final MalformedURLException e) {
              toHost = null;
            }
            if (toHost == null || !toHost.equals(fromHost)) { // external links
              continue; // skip it
            }
          }

          page.putToOutlinks(utf8ToUrl, new Utf8(outlinks[i].getAnchor()));

          if (maxOutlinks > 0 && count > maxOutlinks)
            break;
        }
        Utf8 fetchMark = Mark.FETCH_MARK.checkMark(page);
        if (fetchMark != null) {
          Mark.PARSE_MARK.putMark(page, fetchMark);
        }
      }
    }
  }

  public void process(Mapper<String, WebPage, String, WebPage>.Context context,
      String key, WebPage page) throws IOException, InterruptedException {
    String url = TableUtil.unreverseUrl(key);
    // byte status = (byte) page.getStatus();
    // if (status != CrawlStatus.STATUS_FETCHED) {
    // // if (LOG.isDebugEnabled()) {
    // LOG.info("Skipping " + url + " as status is: "
    // + CrawlStatus.getName(status));
    // // }
    // return;
    // }

    org.apache.nutch.storage.ParseStatus pstatus = page.getParseStatus();
    Map<Utf8, Utf8> links = page.getOutlinks();

    if (links == null || links.size() == 0) {
      super.process(key, page);
      pstatus = page.getParseStatus();
      if (pstatus != null) {
        context.getCounter("ParserStatus",
            ParseStatusCodes.majorCodes[pstatus.getMajorCode()]).increment(1);
      }
      context.write(key, page);
    }

    links = page.getOutlinks();

    if (links != null && links.size() > 0) {
      System.out.println("Url: " + url + ", Status: "
          + (pstatus != null ? pstatus.getMajorCode() : -100)
          + ", Link Number: " + (null != links ? links.size() : 0));

    }
  }

}
