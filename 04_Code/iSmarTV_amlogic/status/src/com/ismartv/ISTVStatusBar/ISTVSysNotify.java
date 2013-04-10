package com.ismartv.ISTVStatusBar;

public class ISTVSysNotify{
	
		private String id;
   		 private String title;
		private String subtitle;
		private String link;
		private String summary;
		private String updated;
		//private DateTime updated;
		private String category;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

   public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }	

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }	
}