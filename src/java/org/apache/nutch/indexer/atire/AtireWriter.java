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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.indexer.NutchIndexWriter;
import org.apache.nutch.util.TableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.tyo.search.search4m.Search4MIndexer;
import au.com.tyo.search.search4m.Search4MService;

public class AtireWriter implements NutchIndexWriter {

  public static Logger LOG = LoggerFactory.getLogger(AtireWriter.class);

  private static String[] META = { "keywords", "description" };

  private static String[] FILEDS = { "site", "id", "title" };

  private static final int DEFAULT_MAX_BULK_DOCS = 500;
  private static final int DEFAULT_MAX_BULK_LENGTH = 5001001; // ~5MB

  // private Client client;
  // private Node node;
  // private String defaultIndex;
  //
  // private BulkRequestBuilder bulk;
  // private ListenableActionFuture<BulkResponse> execute;
  private int maxBulkDocs;
  private int maxBulkLength;
  private long indexedDocs = 0;
  private int bulkDocs = 0;
  private int bulkLength = 0;
  
  Search4MService service;
  
  Search4MIndexer indexer;

  @Override
  public void write(NutchDocument doc) throws IOException {
    String id = TableUtil.reverseUrl(doc.getFieldValue("url"));
    String type = doc.getDocumentMeta().get("type");
    if (type == null)
      type = "doc";
    // IndexRequestBuilder request = client.prepareIndex(defaultIndex, type,
    // id);

    Map<String, Object> source = new HashMap<String, Object>();

    // Loop through all fields of this doc
    /*
     * S: site MK: meta keywords MD: meta description T: title word TF: title
     * full
     */
    AtireWebPage page = new AtireWebPage();
    
    for (String fieldName : doc.getFieldNames()) {
      if (fieldName.equals("site")) {
        // as in Atire, the dot will be considered as punctuation, it will break words
        // so we just remove all dots
        String site = TableUtil.reverseHost(doc.getFieldValue(fieldName));
        String[] sa = site.split("\\.");
        ArrayList<String> dsa = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();
        for (String s : sa) {
          sb.append(s);
          dsa.add(sb.toString());
        }
        sb.setLength(0);
        for (int i = 0; i < dsa.size(); ++i) {
          if (i > 0)
            sb.append(" ");
          sb.append(dsa.get(i));
        }
        page.setSite(sb.toString());
      } else if (fieldName.equals("title")) {
        page.setTitle(doc.getFieldValue(fieldName));
      } else if (fieldName.equals("id")) {
        page.setId(indexedDocs);
      } else if (fieldName.equals("meta_keywords")) {
        page.setKeywords(doc.getFieldValue(fieldName));
      } else if (fieldName.equals("meta_description")) {
        page.setDesc(doc.getFieldValue(fieldName));
      } else if (fieldName.equals("text")) {
        page.setText(doc.getFieldValue(fieldName));
      } else if (fieldName.equals("url")) {
        page.setUrl(doc.getFieldValue(fieldName));
      } else if (fieldName.equals("thumbnail_url")) {
        page.setThumbnailUrl(doc.getFieldValue(fieldName));
      }
//      if (doc.getFieldValues(fieldName).size() > 1) {
//        source.put(fieldName, doc.getFieldValues(fieldName));
//        // Loop through the values to keep track of the size of this document
//        for (String value : doc.getFieldValues(fieldName)) {
//          bulkLength += value.length();
//        }
//      } else {
//        source.put(fieldName, doc.getFieldValue(fieldName));
//        bulkLength += doc.getFieldValue(fieldName).length();
//      }
    }
    // request.setSource(source);

    if (null != page.getThumbnailUrl()) {
    // Add this indexing request to a bulk request
    // bulk.add(request);
      indexedDocs++;
      bulkDocs++;
  
  //    if (bulkDocs >= maxBulkDocs || bulkLength >= maxBulkLength) {
  //      LOG.info("Processing bulk request [docs = " + bulkDocs + ", length = "
  //          + bulkLength + ", total docs = " + indexedDocs
  //          + ", last doc in bulk = '" + id + "']");
  //      // Flush the bulk of indexing requests
  //      // processExecute(true);
  //
  //    }
      
      indexer.index(Long.toString(page.getId()), page.toXml(), page.toJSON());
    }
  }

  @Override
  public void close() throws IOException {
    // Flush pending requests
    LOG.info("Processing remaining requests [docs = " + bulkDocs
        + ", length = " + bulkLength + ", total docs = " + indexedDocs + "]");
    indexer.finish();
    
    indexer.destroy();
    
    service.setIndexer(null);
  }

  @Override
  public void open(TaskAttemptContext job) throws IOException {

    maxBulkDocs =
        job.getConfiguration().getInt(AtireConstants.MAX_BULK_DOCS,
            DEFAULT_MAX_BULK_DOCS);
    maxBulkLength =
        job.getConfiguration().getInt(AtireConstants.MAX_BULK_LENGTH,
            DEFAULT_MAX_BULK_LENGTH);
    
    service = new Search4MService();
    
    indexer = service.createIndexer();

    indexer.initialize("index.db", "SITE:KEYWORDS", "-Cz -rtrec:tag:DOC:TITLE");

  }

  // public static String stripNonCharCodepoints(String input) {
  // StringBuilder retval = new StringBuilder();
  // char ch;
  //
  // for (int i = 0; i < input.length(); i++) {
  // ch = input.charAt(i);
  //
  // // Strip all non-characters
  // //
  // http://unicode.org/cldr/utility/list-unicodeset.jsp?a=[:Noncharacter_Code_Point=True:]
  // // and non-printable control characters except tabulator, new line and
  // // carriage return
  // if (ch % 0x10000 != 0xffff && // 0xffff - 0x10ffff range step 0x10000
  // ch % 0x10000 != 0xfffe && // 0xfffe - 0x10fffe range
  // (ch <= 0xfdd0 || ch >= 0xfdef) && // 0xfdd0 - 0xfdef
  // (ch > 0x1F || ch == 0x9 || ch == 0xa || ch == 0xd)) {
  //
  // retval.append(ch);
  // }
  // }
  //
  // return retval.toString();
  // }
}
