package fr.vde.youtubedlgui.model;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import javax.swing.DefaultListModel;

import fr.vde.youtubedlgui.Gui;
import fr.vde.youtubedlgui.utils.TitleExtractor;

public class PendingListModel extends DefaultListModel<QueuedURL>{
   private static final long serialVersionUID = 578906943668826481L;
   private static ExecutorService loader = Executors.newCachedThreadPool();

   public QueuedURL get(int index) {
      return (QueuedURL)super.get(index);
   }

   public void enqueue(final QueuedURL url) {
      if (!this.contains(url)) {
         this.addElement(url);
         loader.execute(new Runnable() {
            public void run() {
               try {
                  url.setTitle(TitleExtractor.getPageTitle(url.getUrl()));
                  int index = PendingListModel.this.indexOf(url);
                  if (index != -1) {
                     PendingListModel.this.fireContentsChanged(this, PendingListModel.this.indexOf(url), index);
                  }
               } catch (IOException var2) {
                  Gui.logger.log(Level.WARNING, "Could not extract the title from url " + url.getUrl());
               }

            }
         });
      } else {
         Gui.logger.log(Level.FINE, "URL already exists");
      }

   }

   public QueuedURL pop() {
      if (this.getSize() > 0) {
         QueuedURL element = (QueuedURL)this.getElementAt(0);
         this.removeElementAt(0);
         this.fireIntervalRemoved(this, 0, 0);
         return element;
      } else {
         return null;
      }
   }
}
