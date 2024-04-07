package fr.vde.youtubedlgui.bean;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import fr.vde.youtubedlgui.Messages;
import fr.vde.youtubedlgui.utils.IconFactory;

public class ItemPanel extends JPanel {
   private static final long serialVersionUID = -1713633160574009667L;
   private JProgressBar progressBar;
   private JLabel labelProgress;
   private JLabel labelStatus;
   private JLabel labelName;
   private JLabel labelEta;
   private JLabel labelSpeed;
   private JLabel labelSize;
   private JButton btnPlay;
   private Font titleFont;
   private Font textFont;
   private static final int SIZE_WIDTH = 80;
   private static final int ETA_WIDTH = 80;
   private static final int SPEED_WIDTH = 80;
   private static final int PROGRESS_WIDTH = 150;
   private static final int STATUS_WIDTH = 120;
   private static final int HEIGHT = 20;
   private boolean isTitle;
   @SuppressWarnings("unused")
private static final Logger logger = Logger.getLogger("ItemPanel");
   private boolean isIndented;

   public ItemPanel() {
      this(false, false);
   }

   public ItemPanel(boolean title) {
      this(title, false);
   }

   public ItemPanel(boolean title, boolean indent) {
      this.titleFont = new Font("arial", 1, 14);
      this.textFont = new Font("arial", 0, 14);
      this.isTitle = title;
      this.isIndented = indent;
      this.initPanel();
   }

   private void initPanel() {
      this.setOpaque(true);
      this.setBackground(Color.WHITE);
      this.setLayout(new GridBagLayout());
      this.add(this.getLabelName(), new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, 10, 1, new Insets(2, this.isIndented ? 20 : 2, 2, 0), 0, 0));
      this.add(this.getLabelSize(), new GridBagConstraints(1, 0, 1, 1, 0.0D, 1.0D, 10, 1, new Insets(2, 2, 2, 0), 0, 0));
      if (this.isTitle) {
         this.add(this.getLabelProgress(), new GridBagConstraints(2, 0, 1, 1, 0.0D, 1.0D, 10, 1, new Insets(2, 2, 2, 0), 0, 0));
      } else {
         this.add(this.getProgressBar(), new GridBagConstraints(2, 0, 1, 1, 0.0D, 1.0D, 10, 1, new Insets(2, 2, 2, 0), 0, 0));
      }

      this.add(this.getLabelSpeed(), new GridBagConstraints(3, 0, 1, 1, 0.0D, 1.0D, 10, 1, new Insets(2, 2, 2, 0), 0, 0));
      this.add(this.getLabelEta(), new GridBagConstraints(4, 0, 1, 1, 0.0D, 1.0D, 10, 1, new Insets(2, 2, 2, 2), 0, 0));
      this.add(this.getLabelStatus(), new GridBagConstraints(5, 0, 1, 1, 0.0D, 1.0D, 10, 1, new Insets(2, 2, 2, 2), 0, 0));
      if (!this.isTitle) {
         this.add(this.getBtnPlay(), new GridBagConstraints(6, 0, 1, 1, 0.0D, 1.0D, 10, 1, new Insets(2, 2, 2, 2), 0, 0));
      } else {
         this.add(Box.createRigidArea(new Dimension(HEIGHT, HEIGHT)), new GridBagConstraints(6, 0, 1, 1, 0.0D, 1.0D, 10, 1, new Insets(2, 2, 2, 2), 0, 0));
      }

      if (this.isTitle) {
         this.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK));
      }

   }

   public JButton getBtnPlay() {
      if (this.btnPlay == null) {
         this.btnPlay = new JButton(IconFactory.getIconResource("player_play_16x16.png"));
         this.btnPlay.setMargin(new Insets(2, 2, 2, 2));
         Dimension dimension = new Dimension(HEIGHT, HEIGHT);
         this.btnPlay.setPreferredSize(dimension);
         this.btnPlay.setMinimumSize(dimension);
         this.btnPlay.setMaximumSize(dimension);
         this.btnPlay.setEnabled(false);
         this.btnPlay.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               try {
                  Desktop.getDesktop().open(new File(ItemPanel.this.getLabelName().getText()));
               } catch (IOException var3) {
               }

            }
         });
      }

      return this.btnPlay;
   }

   public JProgressBar getProgressBar() {
      if (this.progressBar == null) {
         this.progressBar = new JProgressBar(0, 100);
         Dimension dimension = new Dimension(PROGRESS_WIDTH, HEIGHT);
         this.progressBar.setPreferredSize(dimension);
         this.progressBar.setMinimumSize(dimension);
         this.progressBar.setMaximumSize(dimension);
         this.progressBar.setFont(this.textFont);
      }

      return this.progressBar;
   }

   public JLabel getLabelProgress() {
      if (this.labelProgress == null) {
         this.labelProgress = new JLabel();
         Dimension dimension = new Dimension(PROGRESS_WIDTH, HEIGHT);
         this.labelProgress.setPreferredSize(dimension);
         this.labelProgress.setMinimumSize(dimension);
         this.labelProgress.setMaximumSize(dimension);
         this.labelProgress.setText(Messages.getString("header.Progress"));
         this.labelProgress.setFont(this.titleFont);
         this.labelProgress.setVerticalAlignment(0);
      }

      return this.labelProgress;
   }

   public JLabel getLabelName() {
      if (this.labelName == null) {
         this.labelName = new JLabel();
         Dimension dimension = new Dimension(5, HEIGHT);
         this.labelName.setPreferredSize(dimension);
         this.labelName.setMinimumSize(dimension);
         if (this.isTitle) {
            this.labelName.setText(Messages.getString("header.Title"));
            this.labelName.setFont(this.titleFont);
         } else {
            this.labelName.setFont(this.textFont);
         }

         this.labelName.setVerticalAlignment(0);
      }

      return this.labelName;
   }

   public JLabel getLabelEta() {
      if (this.labelEta == null) {
         this.labelEta = new JLabel();
         Dimension dimension = new Dimension(ETA_WIDTH, HEIGHT);
         this.labelEta.setPreferredSize(dimension);
         this.labelEta.setMinimumSize(dimension);
         this.labelEta.setMaximumSize(dimension);
         if (this.isTitle) {
            this.labelEta.setText(Messages.getString("header.ETA"));
            this.labelEta.setFont(this.titleFont);
         } else {
            this.labelEta.setFont(this.textFont);
         }

         this.labelEta.setVerticalAlignment(0);
      }

      return this.labelEta;
   }

   public JLabel getLabelStatus() {
      if (this.labelStatus == null) {
         this.labelStatus = new JLabel();
         Dimension dimension = new Dimension(STATUS_WIDTH, HEIGHT);
         this.labelStatus.setPreferredSize(dimension);
         this.labelStatus.setMinimumSize(dimension);
         this.labelStatus.setMaximumSize(dimension);
         if (this.isTitle) {
            this.labelStatus.setText(Messages.getString("header.Status"));
            this.labelStatus.setFont(this.titleFont);
         } else {
            this.labelStatus.setText(Messages.getString("status.pending"));
            this.labelStatus.setFont(this.textFont);
         }

         this.labelStatus.setVerticalAlignment(0);
      }

      return this.labelStatus;
   }

   public JLabel getLabelSpeed() {
      if (this.labelSpeed == null) {
         this.labelSpeed = new JLabel();
         Dimension dimension = new Dimension(SPEED_WIDTH, HEIGHT);
         this.labelSpeed.setPreferredSize(dimension);
         this.labelSpeed.setMinimumSize(dimension);
         this.labelSpeed.setMaximumSize(dimension);
         if (this.isTitle) {
            this.labelSpeed.setText(Messages.getString("header.Speed"));
            this.labelSpeed.setFont(this.titleFont);
         } else {
            this.labelSpeed.setFont(this.textFont);
         }

         this.labelSpeed.setVerticalAlignment(0);
      }

      return this.labelSpeed;
   }

   public JLabel getLabelSize() {
      if (this.labelSize == null) {
         this.labelSize = new JLabel();
         Dimension dimension = new Dimension(SIZE_WIDTH, HEIGHT);
         this.labelSize.setPreferredSize(dimension);
         this.labelSize.setMinimumSize(dimension);
         this.labelSize.setMaximumSize(dimension);
         if (this.isTitle) {
            this.labelSize.setText(Messages.getString("header.Size"));
            this.labelSize.setFont(this.titleFont);
         } else {
            this.labelSize.setFont(this.textFont);
         }

         this.labelSize.setVerticalAlignment(0);
      }

      return this.labelSize;
   }
}
