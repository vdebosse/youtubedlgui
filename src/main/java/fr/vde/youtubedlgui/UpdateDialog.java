package fr.vde.youtubedlgui;

import java.awt.Font;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class UpdateDialog extends JDialog {
    private static final long serialVersionUID = -2799857755676539874L;

    public UpdateDialog(Frame owner, boolean modal) {
        super(owner, modal);
        Font bigFont = new Font("arial", Font.PLAIN, 16);
        this.setUndecorated(true);
        JLabel label = new JLabel(Messages.getString("dialog.updating"));
        label.setFont(bigFont);
        label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        this.setContentPane(label);
        this.pack();
        this.setLocationRelativeTo(owner);
    }
}
