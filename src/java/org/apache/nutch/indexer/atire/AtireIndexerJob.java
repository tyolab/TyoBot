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

import java.util.Map;

import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.indexer.IndexerJob;
import org.apache.nutch.indexer.NutchIndexWriterFactory;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.ToolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Indexer for elasticsearch. Uses bulk operations with flushing in the
 * background, keeping track of elasticsearch responses by checking after every
 * flush. When a previous flush has not finished yet before the next bulk is
 * full, it will wait for it. This mechanism will keep the servers from
 * overloading.
 */
public class AtireIndexerJob extends IndexerJob {

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

  public void indexAtire(String batchId) throws Exception {
    run(ToolUtil.toArgMap(Nutch.ARG_BATCH, batchId));
  }

  public int run(String[] args) throws Exception {
    if (args.length == 1) {
      // ok
    } else if (args.length == 3 && "-crawlId".equals(args[1])) {
      getConf().set(Nutch.CRAWL_ID_KEY, args[2]);
    } else {
      System.err
          .println("Usage: (<batchId> | -all | -reindex) [-crawlId <id>]");
      return -1;
    }
    indexAtire(args[0]);
    return 0;
  }

  public static void main(String[] args) throws Exception {
    int res =
        ToolRunner
            .run(NutchConfiguration.create(), new AtireIndexerJob(), args);
    System.exit(res);
  }
}
