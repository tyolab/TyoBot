/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nutch.indexer.atire;

import java.io.IOException;
import java.util.Map;

import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.indexer.IndexerJob;
import org.apache.nutch.indexer.NutchIndexWriterFactory;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.net.URLFilterException;
import org.apache.nutch.net.URLFiltersContent;
import org.apache.nutch.parse.ParseUtil;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.TableUtil;
import org.apache.nutch.util.ToolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Indexer for atire search. Uses bulk operations with flushing in the
 * background, keeping track of atire search responses by checking after every
 * flush. When a previous flush has not finished yet before the next bulk is
 * full, it will wait for it. This mechanism will keep the servers from
 * overloading.
 */
public class AtireIndexerJob extends IndexerJob {

  static {
    LOG = LoggerFactory.getLogger(AtireIndexerJob.class);
    IndexerJob.indexerMapperCls = AtireMapper.class;
    
    System.loadLibrary("atire_jni");
  }

  public static class AtireMapper extends IndexerMapper {

    private URLFiltersContent filtersContent;

    @Override
    public void setup(Context context) throws IOException {
      super.setup(context);

      filtersContent = new URLFiltersContent(context.getConfiguration());
    }

    @Override
    public void map(String key, WebPage page, Context context)
        throws IOException, InterruptedException {
      String url = TableUtil.unreverseUrl(key);

      String toUrl = null;
      try {
        toUrl = filtersContent.filter(url);
      } catch (URLFilterException e) {
        e.printStackTrace();
      }

      if (null != toUrl)
        super.map(key, page, context);
    }

  }

  public static Logger LOG = LoggerFactory.getLogger(AtireIndexerJob.class);

  @Override
  public Map<String, Object> run(Map<String, Object> args) throws Exception {
    LOG.info("Starting");

    NutchIndexWriterFactory.addClassToConf(getConf(), AtireWriter.class);
    String batchId = (String) args.get(Nutch.ARG_BATCH);
    String clusterName = (String) args.get(AtireConstants.CLUSTER);

    // getConf().set(AtireConstants.CLUSTER, clusterName);

    currentJob =
        createIndexJob(getConf(), "atire-index [" + clusterName + "]", batchId);

    currentJob.waitForCompletion(true);
    ToolUtil.recordJobStatus(null, currentJob, results);

    LOG.info("Done");
    return results;
  }

  public void indexAtire(String batchId, boolean withThumbPageOnly)
      throws Exception {
    run(ToolUtil.toArgMap(Nutch.ARG_BATCH, batchId,
        AtireConstants.WITH_THUMBNAIL_PAGE_ONLY, withThumbPageOnly));
  }

  public int run(String[] args) throws Exception {
    if (args.length == 1) {
      // ok
    } else if (args.length == 3 && "-crawlId".equals(args[1])) {
      getConf().set(Nutch.CRAWL_ID_KEY, args[2]);
    } else {
      System.err
          .println("Usage: (<batchId> | -all | -reindex) [-withThumbPageOnly] [-crawlId <id>]");
      return -1;
    }

    boolean withThumbPageOnly = false;

    for (int i = 0; i < args.length; ++i) {

    }
    indexAtire(args[0], withThumbPageOnly);
    return 0;
  }

  public static void main(String[] args) throws Exception {
    try {
      System.loadLibrary("atire_jni");
    } catch (Exception ex) {
      System.exit(-1);
    }

    int res =
        ToolRunner
            .run(NutchConfiguration.create(), new AtireIndexerJob(), args);
    System.exit(res);
  }
}
