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
import org.apache.hadoop.mapreduce.Mapper.Context;
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
  private URLFiltersList filtersList;

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
    filtersList = new URLFiltersList(conf);
  }

  public URLFiltersList getFiltersList() {
    return filtersList;
  }

  public void setFiltersList(URLFiltersList filtersList) {
    this.filtersList = filtersList;
  }

  public Outlink[] getOutlinkArray(String key, WebPage page) {
    String url = TableUtil.unreverseUrl(key);

    Parse parse;
    try {
      parse = parse(url, page);
    } catch (ParserNotFound e) {
      // do not print stacktrace for the fact that some types are not mapped.
      LOG.warn("No suitable parser found: " + e.getMessage());
      return null;
    } catch (final Exception e) {
      LOG.warn("Error parsing: " + url + ": "
          + StringUtils.stringifyException(e));
      return null;
    }

    if (parse == null) {
      return null;
    }

    // final byte[] signature = sig.calculate(page);

    // org.apache.nutch.storage.ParseStatus pstatus = parse.getParseStatus();
    // page.setParseStatus(pstatus);
    // if (ParseStatusUtils.isSuccess(pstatus)) {
    // if (pstatus.getMinorCode() == ParseStatusCodes.SUCCESS_REDIRECT) {
    // String newUrl = ParseStatusUtils.getMessage(pstatus);
    // int refreshTime = Integer.parseInt(ParseStatusUtils.getArg(pstatus, 1));
    // try {
    // newUrl = normalizers.normalize(newUrl, URLNormalizers.SCOPE_FETCHER);
    // if (newUrl == null) {
    // LOG.warn("redirect normalized to null " + url);
    // return null;
    // }
    // try {
    // newUrl = filters.filter(newUrl);
    // } catch (URLFilterException e) {
    // return null;
    // }
    // if (newUrl == null) {
    // LOG.warn("redirect filtered to null " + url);
    // return null;
    // }
    // } catch (MalformedURLException e) {
    // LOG.warn("malformed url exception parsing redirect " + url);
    // return null;
    // }
    // page.putToOutlinks(new Utf8(newUrl), new Utf8());
    // page.putToMetadata(FetcherJob.REDIRECT_DISCOVERED, TableUtil.YES_VAL);
    // if (newUrl == null || newUrl.equals(url)) {
    // String reprUrl =
    // URLUtil.chooseRepr(url, newUrl,
    // refreshTime < FetcherJob.PERM_REFRESH_TIME);
    // if (reprUrl == null) {
    // LOG.warn("reprUrl==null for " + url);
    // } else {
    // page.setReprUrl(new Utf8(reprUrl));
    // }
    // }
    // }
    // return null;
    // }

    // page.setText(new Utf8(parse.getText()));
    // page.setTitle(new Utf8(parse.getTitle()));
    // ByteBuffer prevSig = page.getSignature();
    // if (prevSig != null) {
    // page.setPrevSignature(prevSig);
    // }
    // page.setSignature(ByteBuffer.wrap(signature));
    // if (page.getOutlinks() != null) {
    // // page.getOutlinks().clear();
    // page.clearOutlinks();
    // }
    return parse.getOutlinks();

    // final int count = 0;
    // String fromHost;
    // if (ignoreExternalLinks) {
    // try {
    // fromHost = new URL(url).getHost().toLowerCase();
    // } catch (final MalformedURLException e) {
    // fromHost = null;
    // }
    // } else {
    // fromHost = null;
    // }
    // /*
    // * don't need to limit the outlinks number
    // */
    // for (int i = 0; /* count < maxOutlinks && */i < outlinks.length; i++) {
    // String toUrl = outlinks[i].getToUrl();
    // String normailizedUrl = toUrl;
    // try {
    // normailizedUrl =
    // normalizers.normalize(toUrl, URLNormalizers.SCOPE_OUTLINK);
    // toUrl = filters.filter(normailizedUrl);
    // } catch (MalformedURLException e2) {
    // continue;
    // } catch (URLFilterException e) {
    // continue;
    // }
    // if (toUrl == null) {
    // if (normailizedUrl != null)
    // LOG.info("skipping " + normailizedUrl);
    // continue;
    // } else {
    // // LOG.info("found link: " + toUrl);
    // System.out.println("found link: " + toUrl);
    // }
    //
    // Utf8 utf8ToUrl = new Utf8(toUrl);
    // if (page.getFromOutlinks(utf8ToUrl) != null) {
    // // skip duplicate outlinks
    // continue;
    // }
    // String toHost;
    // if (ignoreExternalLinks) {
    // try {
    // toHost = new URL(toUrl).getHost().toLowerCase();
    // } catch (final MalformedURLException e) {
    // toHost = null;
    // }
    // if (toHost == null || !toHost.equals(fromHost)) { // external links
    // continue; // skip it
    // }
    // }
    //
    // page.putToOutlinks(utf8ToUrl, new Utf8(outlinks[i].getAnchor()));
    //
    // if (maxOutlinks > 0 && count > maxOutlinks)
    // break;
    // }
    //
    // }
    // }
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
    // Map<Utf8, Utf8> links = page.getOutlinks();
    Outlink[] outlinks = null; // we have to have the links in order

    // if (links == null || links.size() == 0)
    outlinks = getOutlinkArray(key, page);

    if (outlinks != null && outlinks.length > 0) {
      System.out.println("Url: " + url + ", Status: "
          + (pstatus != null ? pstatus.getMajorCode() : -100)
          + ", Link Number: " + (null != outlinks ? outlinks.length : 0));

      for (int i = 0; i < outlinks.length; i++) {
        Outlink outlink = outlinks[i];
        String toUrl = outlink.getToUrl();
        String normailizedUrl = toUrl;
        try {
          normailizedUrl =
              normalizers.normalize(toUrl, URLNormalizers.SCOPE_OUTLINK);
          toUrl = filtersContent.filter(normailizedUrl);
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

          /*
           * if the first link is the content link, the thumbnail must be the
           * next one
           */
          int imageIndex = i + 1;

          break;
        }
      }

    }
  }

}
