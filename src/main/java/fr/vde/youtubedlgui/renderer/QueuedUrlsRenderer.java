package fr.vde.youtubedlgui.renderer;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;

import fr.vde.youtubedlgui.model.QueuedURL;
import fr.vde.youtubedlgui.utils.IconFactory;

public class QueuedUrlsRenderer extends DefaultListCellRenderer {
   private static final long serialVersionUID = -2692639885455386523L;
   private Icon audioIcon = IconFactory.getIconResource("cdaudio_mount_16x16.png");
   private Icon videoIcon = IconFactory.getIconResource("xine_16x16.png");

   public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (value instanceof QueuedURL) {
         if (((QueuedURL)value).isAudioOnly()) {
            ((JLabel)component).setIcon(this.audioIcon);
         } else {
            ((JLabel)component).setIcon(this.videoIcon);
         }
      }

      return component;
   }
}
