package fr.vde.youtubedlgui.model;

public class QueuedURL {
   private String url;
   private String title;
   private boolean audioOnly;
   private String storage;

   public QueuedURL(String url, String storage, boolean audioOnly) {
      this.url = url;
      this.storage = storage;
      this.audioOnly = audioOnly;
   }

   @Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((url == null) ? 0 : url.hashCode());
	return result;
}

   @Override
public boolean equals(Object obj) {
	if (this == obj)
		return true;
	if (obj == null)
		return false;
	if (getClass() != obj.getClass())
		return false;
	QueuedURL other = (QueuedURL) obj;
	if (url == null) {
		if (other.url != null)
			return false;
	} else if (!url.equals(other.url))
		return false;
	return true;
}

   public String getUrl() {
      return this.url;
   }

   public String getTitle() {
      return this.title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public void setUrl(String url) {
      this.url = url;
   }

   public String getStorage() {
      return this.storage;
   }

   public void setStorage(String storage) {
      this.storage = storage;
   }

   public String toString() {
      return this.title == null ? this.url : this.title;
   }

   public boolean isAudioOnly() {
      return this.audioOnly;
   }

   public void setAudioOnly(boolean audioOnly) {
      this.audioOnly = audioOnly;
   }
}
