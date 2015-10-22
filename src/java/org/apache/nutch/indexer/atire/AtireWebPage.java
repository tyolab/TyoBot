package org.apache.nutch.indexer.atire;

public class AtireWebPage extends AtireDocument {
  
  private long id;
  
  private String title;
  
  private String desc;
  
  private String keywords;
  
  private String site;
  
  private String text;
  
  public AtireWebPage() {
    
  }

  public long getId() {
    return id;
  }

  public void setId(long indexedDocs) {
    this.id = indexedDocs;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public String getKeywords() {
    return keywords;
  }

  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }

  public String getSite() {
    return site;
  }

  public void setSite(String site) {
    this.site = site;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
  
  public String toXml() {
//  StringBuffer sb = new StringBuffer();

  String text = String.format("<%s>%s</%s>", CONTENT_TAG, null != this.getText() ? this.getText() : "", CONTENT_TAG);
  
  
  return String.format(XML_TEMPLATE, this.getTitle(), String.valueOf(this.getId()), this.getSite(), 
      this.getDesc(), this.getKeywords(), text);
}
}
