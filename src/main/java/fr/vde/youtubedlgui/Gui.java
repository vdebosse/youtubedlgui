package fr.vde.youtubedlgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.bridj.Pointer;
import org.bridj.Pointer.Releaser;
import org.bridj.cpp.com.COMRuntime;
import org.bridj.cpp.com.shell.ITaskbarList3;
import org.bridj.cpp.com.shell.ITaskbarList3.TbpFlag;
import org.bridj.jawt.JAWTUtils;

import fr.vde.youtubedlgui.bean.ItemPanel;
import fr.vde.youtubedlgui.model.PendingListModel;
import fr.vde.youtubedlgui.model.QueuedURL;
import fr.vde.youtubedlgui.renderer.QueuedUrlsRenderer;
import fr.vde.youtubedlgui.utils.IconFactory;

public class Gui extends JFrame implements WindowListener, Releaser {
    private static final long serialVersionUID = 49786141261076710L;
    private static final String DOWNLOAD_FOLDER_KEY = "downloadFolder";
    private static final String LAST_FILE_KEY = "lastFile";
    private static final String TASKLIST = "tasklist";
    private static final String KILL = "taskkill /IM ";
    private static final String CONFIG_PROPERTIES = "config.properties";
    private Process currentProcess;
    private JTextField tfUrl;
    private JTextField tfOutput;
    private JButton btnStartVideo;
    private JButton btnStartAudio;
    private JButton btnCancel;
    private JButton btnQuit;
    private JButton btnBrowse;
    private JButton btnPaste;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private JLabel lblUrl;
    private JPanel btnPanel;
    private JPanel progressPanel;
    private JLabel lblDestination;
    private JPanel content;
    private Component filler;
    private JScrollPane scrollPane;
    private JList<QueuedURL> pendingList;
    private JPanel listPanel;
    private ItemPanel currentItem;
    private ItemPanel playlistItem;
    private ITaskbarList3 taskBarList;
    private Pointer<Integer> hwnd;
    private PendingListModel pendingListModel;
    private boolean cancelled = false;
    private static final String LOGFILE = "error.log";
    public static final Logger logger = Logger.getLogger("Gui");
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private boolean running = false;
    private static final Pattern progressPattern = Pattern.compile("\\[download\\]\\s*(\\d*\\.\\d)%\\s*of\\s*(.*)\\s*at\\s*(.*)\\s*ETA\\s*(.*)");
    private static final Pattern filePattern = Pattern.compile("\\[.*\\]\\s*Destination:\\s*(.*)");
    private static final Pattern mergePattern = Pattern.compile("\\[.*\\]\\s*Merging formats into \"(.*)\"");
    private static final Pattern existingPattern = Pattern.compile("\\[.*\\]\\s*(.*) has already been downloaded and merged");
    private static final Pattern playlistPattern = Pattern.compile("\\[.*\\]\\s*Downloading playlist:\\s*(.*)");
    private static final Pattern playlistProgressPattern = Pattern.compile("\\[.*\\]\\s*Downloading video (.*) of (.*)");
    private static final Pattern endPattern = Pattern.compile("\\[download\\]\\s*100%.*");
    private JPanel bottomPanel;
    private JLabel lblQueue;

    public static void main(String[] args) {
        try {
            FileHandler fh = new FileHandler(LOGFILE);
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
            fh.setFormatter(new Formatter() {
                private Date date = new Date();
                private String lineSeparator = System.lineSeparator();

                public String format(LogRecord record) {
                    this.date.setTime(record.getMillis());
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(this.date);
                    stringBuilder.append(" | [");
                    stringBuilder.append(record.getLongThreadID());
                    stringBuilder.append("] ");
                    stringBuilder.append(record.getLevel());
                    stringBuilder.append(" | ");
                    stringBuilder.append(record.getLoggerName());
                    stringBuilder.append(" | ");
                    stringBuilder.append(record.getMessage());
                    if (record.getThrown() != null) {
                        stringBuilder.append(this.lineSeparator);

                        try {
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            record.getThrown().printStackTrace(pw);
                            pw.close();
                            stringBuilder.append(sw.toString());
                        } catch (Exception var5) {
                        }
                    }

                    stringBuilder.append(this.lineSeparator);
                    return stringBuilder.toString();
                }
            });
        } catch (SecurityException var4) {
            var4.printStackTrace();
        } catch (IOException var5) {
            var5.printStackTrace();
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception var3) {
            logger.log(Level.SEVERE, "Unable to set the UI", var3);
        }

        Gui gui = new Gui();
        gui.pack();
        gui.setLocationRelativeTo((Component) null);
        gui.setVisible(true);
    }

    public Gui() {
        this.initializeComponents();
        this.build();
        this.configure();
        this.addActions();
    }

    private void configure() {
        this.loadProperties();
        Font bigFont = new Font("arial", 0, 16);
        this.lblUrl.setFont(bigFont);
        this.lblDestination.setFont(bigFont);
        this.tfUrl.setFont(bigFont);
        this.tfUrl.setPreferredSize(new Dimension(500, 25));
        this.tfOutput.setFont(bigFont);
        this.tfOutput.setPreferredSize(new Dimension(500, 25));
        this.btnQuit.setFont(bigFont);
        this.btnCancel.setFont(bigFont);
        this.btnCancel.setEnabled(false);
        this.btnPaste.setFont(bigFont);
        this.btnStartAudio.setFont(bigFont);
        this.btnStartAudio.setHorizontalTextPosition(0);
        this.btnStartAudio.setVerticalTextPosition(3);
        this.btnStartVideo.setFont(bigFont);
        this.btnStartVideo.setHorizontalTextPosition(0);
        this.btnStartVideo.setVerticalTextPosition(3);
        this.btnBrowse.setFont(bigFont);
        this.scrollPane.setVerticalScrollBarPolicy(22);
        this.scrollPane.setPreferredSize(new Dimension(0, 100));
        this.scrollPane.setMinimumSize(new Dimension(0, 0));
        this.scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        this.pendingList.setCellRenderer(new QueuedUrlsRenderer());
        this.lblQueue.setFont(bigFont.deriveFont(1));
        this.lblQueue.setBackground(this.pendingList.getBackground());
        this.lblQueue.setOpaque(true);
        this.lblQueue.setBorder(this.scrollPane.getBorder());
        this.lblQueue.setHorizontalAlignment(0);
        this.setDefaultCloseOperation(0);
        this.addWindowListener(this);
        this.setTitle(Messages.getString("application.title"));
        this.setIconImage(IconFactory.getIconResource("download_16x16.png").getImage());
        this.setMinimumSize(new Dimension(800, 600));
        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                if (!Gui.this.running) {
                    QueuedURL pop = Gui.this.pendingListModel.pop();
                    if (pop != null) {
                        Gui.this.startProcess(pop);
                    }
                }

            }
        }, 0L, 500L, TimeUnit.MILLISECONDS);
    }

    private void addActions() {
        this.btnBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Gui.logger.info("btnBrowse:actionPerformed");
                JFileChooser jfs = new JFileChooser(Gui.this.tfOutput.getText());
                jfs.setFileSelectionMode(1);
                jfs.showOpenDialog(Gui.this);
                if (jfs.getSelectedFile() != null) {
                    Gui.this.tfOutput.setText(jfs.getSelectedFile().getAbsolutePath());
                }

            }
        });
        this.btnPaste.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Gui.logger.info("btnPaste:actionPerformed");
                DataFlavor[] flavors = Toolkit.getDefaultToolkit().getSystemClipboard().getAvailableDataFlavors();
                if (flavors != null) {
                    DataFlavor textFlavor = DataFlavor.stringFlavor;
                    if (textFlavor != null) {
                        try {
                            Object data = Toolkit.getDefaultToolkit().getSystemClipboard().getData(textFlavor);
                            if (data != null) {
                                Gui.this.tfUrl.setText(data.toString());
                            }
                        } catch (Exception var5) {
                            Gui.logger.log(Level.SEVERE, "Unable to get the data from the clipboard", var5);
                        }

                    }
                }
            }
        });
        this.btnQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Gui.this.doClose();
            }
        });
        this.btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Gui.logger.info("btnCancel:actionPerformed");
                Gui.this.btnCancel.setEnabled(false);
                if (Gui.this.currentProcess != null) {
                    Gui.this.cancelled = true;
                    Gui.this.currentProcess.destroy();

                    try {
                        if (Gui.isProcessRunning("ffmpeg.exe")) {
                            Gui.killProcess("ffmpeg.exe");
                        }
                    } catch (Exception var3) {
                        Gui.logger.log(Level.SEVERE, "Unable to kill ffmpeg process", var3);
                    }
                }

            }
        });
        this.btnStartAudio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Gui.logger.info("btnStartAudio:actionPerformed");
                Gui.this.executor.execute(new Runnable() {
                    public void run() {
                        QueuedURL url = new QueuedURL(Gui.this.tfUrl.getText().trim(), Gui.this.tfOutput.getText(), true);
                        Gui.this.pendingListModel.enqueue(url);
                    }
                });
            }
        });
        this.btnStartVideo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Gui.logger.info("btnStartVideo:actionPerformed");
                Gui.this.executor.execute(new Runnable() {
                    public void run() {
                        QueuedURL url = new QueuedURL(Gui.this.tfUrl.getText().trim(), Gui.this.tfOutput.getText(), false);
                        Gui.this.pendingListModel.enqueue(url);
                    }
                });
            }
        });
    }

    private void build() {
        JPanel buttonAndListPanel = new JPanel(new BorderLayout(5, 5));
        this.content.add(new JLabel(fr.vde.youtubedlgui.utils.IconFactory.getIconResource("agt_web_22x22.png")), new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, 17, 1, new Insets(10, 10, 10, 0), 0, 0));
        this.content.add(this.lblUrl, new GridBagConstraints(1, 0, 1, 1, 0.0D, 0.0D, 17, 1, new Insets(10, 10, 10, 10), 0, 0));
        this.content.add(this.tfUrl, new GridBagConstraints(2, 0, 1, 1, 1.0D, 0.0D, 17, 1, new Insets(10, 10, 10, 0), 0, 0));
        this.content.add(this.btnPaste, new GridBagConstraints(3, 0, 1, 1, 0.0D, 0.0D, 17, 1, new Insets(10, 0, 10, 10), 0, 0));
        this.content.add(new JLabel(IconFactory.getIconResource("filesave_22x22.png")), new GridBagConstraints(0, 1, 1, 1, 0.0D, 0.0D, 17, 1, new Insets(10, 10, 10, 0), 0, 0));
        this.content.add(this.lblDestination, new GridBagConstraints(1, 1, 1, 1, 0.0D, 0.0D, 17, 1, new Insets(10, 10, 10, 10), 0, 0));
        this.content.add(this.tfOutput, new GridBagConstraints(2, 1, 1, 1, 1.0D, 0.0D, 17, 1, new Insets(10, 10, 10, 0), 0, 0));
        this.content.add(this.btnBrowse, new GridBagConstraints(3, 1, 1, 1, 0.0D, 0.0D, 17, 1, new Insets(10, 0, 10, 10), 0, 0));
        this.content.add(buttonAndListPanel, new GridBagConstraints(0, 2, 4, 1, 1.0D, 0.0D, 10, 2, new Insets(10, 10, 10, 10), 0, 0));
        this.content.add(this.scrollPane, new GridBagConstraints(0, 3, 4, 1, 1.0D, 1.0D, 10, 1, new Insets(10, 10, 10, 10), 0, 0));
        this.content.add(this.bottomPanel, new GridBagConstraints(0, 4, 4, 1, 1.0D, 0.0D, 13, 0, new Insets(0, 10, 10, 10), 0, 0));
        this.btnPanel.add(this.btnStartVideo);
        this.btnPanel.add(this.btnStartAudio);
        buttonAndListPanel.add(this.btnPanel, "West");
        buttonAndListPanel.add(this.listPanel);
        this.listPanel.add(this.lblQueue, "North");
        this.listPanel.add(new JScrollPane(this.pendingList), "Center");
        this.bottomPanel.add(this.btnCancel);
        this.bottomPanel.add(this.btnQuit);
        this.scrollPane.setColumnHeaderView(new ItemPanel(true));
        this.setContentPane(this.content);
    }

    private void initializeComponents() {
        try {
            this.taskBarList = (ITaskbarList3) COMRuntime.newInstance(ITaskbarList3.class);
        } catch (ClassNotFoundException var2) {
            logger.log(Level.WARNING, "Error during instantiation of taskbar list", var2);
        }

        this.pendingListModel = new PendingListModel();
        this.pendingList = new JList<QueuedURL>(this.pendingListModel);
        this.content = new JPanel(new GridBagLayout());
        this.tfUrl = new JTextField();
        this.tfOutput = new JTextField(System.getenv((new JFileChooser()).getFileSystemView().getDefaultDirectory().toString()));
        this.btnBrowse = new JButton(Messages.getString("action.browse"));
        this.btnStartAudio = new JButton(Messages.getString("action.download.audio"), IconFactory.getIconResource("cdaudio_mount_64x64.png"));
        this.btnStartVideo = new JButton(Messages.getString("action.download.video"), IconFactory.getIconResource("xine_64x64.png"));
        this.btnCancel = new JButton(Messages.getString("action.cancel"), IconFactory.getIconResource("agt_stop_22x22.png"));
        this.btnPaste = new JButton(IconFactory.getIconResource("editpaste_22x22.png"));
        this.btnPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        this.listPanel = new JPanel(new BorderLayout(1, 1));
        this.lblQueue = new JLabel(Messages.getString("label.pending"));
        this.btnQuit = new JButton(Messages.getString("action.exit"), IconFactory.getIconResource("exit_22x22.png"));
        this.bottomPanel = new JPanel(new FlowLayout(4));
        this.progressPanel = new JPanel(new GridBagLayout());
        this.lblUrl = new JLabel(Messages.getString("label.url"));
        this.lblDestination = new JLabel(Messages.getString("label.destination"));
        this.filler = Box.createGlue();
        this.scrollPane = new JScrollPane(this.progressPanel);
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        long hwndVal = JAWTUtils.getNativePeerHandle(this);
        this.hwnd = Pointer.pointerToAddress(hwndVal, Integer.class, this);
        this.taskBarList.SetProgressState(this.hwnd, TbpFlag.TBPF_NOPROGRESS);
    }

    private void addProgressComponent(Component c) {
        this.progressPanel.remove(this.filler);
        this.progressPanel.add(c, new GridBagConstraints(0, this.progressPanel.getComponentCount(), 1, 1, 1.0D, 0.0D, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
        this.addGlue();
        this.content.validate();
        this.scrollPane.getVerticalScrollBar().setValue(this.scrollPane.getVerticalScrollBar().getMaximum());
    }

    private void addGlue() {
        this.progressPanel.add(this.filler, new GridBagConstraints(0, this.progressPanel.getComponentCount(), 1, 1, 1.0D, 1.0D, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
        this.progressPanel.revalidate();
        this.progressPanel.repaint();
    }

    private void startProcess(QueuedURL url) {
        logger.info("Entering startProcess");
        this.taskBarList.SetProgressState(this.hwnd, TbpFlag.TBPF_NORMAL);
        this.currentItem = null;
        this.playlistItem = null;
        this.cancelled = false;
        this.running = true;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Gui.this.btnCancel.setEnabled(true);
            }
        });
        List<String> args = new ArrayList<>();
        File file = new File("youtube-dlc.exe");
        String absolutePath = file.getAbsolutePath();
        args.add(absolutePath);
        args.add("-i");
        args.add("-o");
        args.add("\"" + (new File(url.getStorage(), "%(title)s.%(ext)s")).getAbsolutePath() + "\"");
        if (url.isAudioOnly()) {
            args.add("-x");
            args.add("--audio-quality=0");
            args.add("--audio-format=mp3");
        } else {
            args.add("--all-subs");
            args.add("--embed-subs");
        }

        args.add("\"" + url.getUrl() + "\"");
        ProcessBuilder builder = new ProcessBuilder(args);
        logger.info("Path to youtube-dlc: " + absolutePath);
        logger.info("Can read? " + file.canRead());
        logger.info("Can execute? " + file.canExecute());
        File output = new File(url.getStorage());
        logger.info("Output path: " + output.getAbsolutePath());
        logger.info("Can read? " + file.canRead());
        logger.info("Can write? " + file.canWrite());
        logger.info("Full command: " + args.toString());

        try {
            logger.info("Starting process builder");
            this.currentProcess = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.currentProcess.getInputStream()));
            String line = null;

            while ((line = reader.readLine()) != null) {
                this.parseLine(line);
            }

            reader.close();
            logger.info("End of process");
        } catch (IOException var9) {
            logger.log(Level.SEVERE, "Exception while running the process", var9);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (Gui.this.currentItem != null) {
                    if (Gui.this.cancelled) {
                        Gui.this.currentItem.getLabelStatus().setText(Messages.getString("status.cancelled"));
                    } else {
                        if (Gui.this.playlistItem != null) {
                            Gui.this.playlistItem.getLabelStatus().setText(Messages.getString("status.completed"));
                        }

                        Gui.this.currentItem.getLabelStatus().setText(Messages.getString("status.completed"));
                        Gui.this.currentItem.getBtnPlay().setEnabled(true);
                    }
                }

                Gui.this.btnCancel.setEnabled(false);
                Gui.this.taskBarList.SetProgressValue(Gui.this.hwnd, 0L, 100L);
                Gui.this.taskBarList.SetProgressState(Gui.this.hwnd, TbpFlag.TBPF_NOPROGRESS);
            }
        });
        this.currentProcess = null;
        this.running = false;
        logger.info("Exiting startProcess");
    }

    private void parseLine(final String line) {
        if (line.startsWith("[download]")) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Matcher matcher;
                    if ((matcher = Gui.playlistProgressPattern.matcher(line)).matches()) {
                        if (Gui.this.currentItem != null) {
                            Gui.this.currentItem.getLabelStatus().setText(Messages.getString("status.completed"));
                            Gui.this.currentItem.getBtnPlay().setEnabled(true);
                        }

                        Gui.this.currentItem = new ItemPanel(false, Gui.this.playlistItem != null);
                        int dx = (int) Double.parseDouble(matcher.group(1));
                        int max = (int) Double.parseDouble(matcher.group(2));
                        Gui.this.playlistItem.getProgressBar().setMaximum(max);
                        Gui.this.playlistItem.getProgressBar().setValue(dx);
                        Gui.this.playlistItem.getLabelSize().setText(dx + "/" + max);
                        Gui.this.addProgressComponent(Gui.this.currentItem);
                        if (Gui.this.progressPanel.getComponentCount() % 2 == 1) {
                            Gui.this.currentItem.setBackground(new Color(225, 225, 225));
                        }
                    } else if ((matcher = Gui.filePattern.matcher(line)).matches()) {
                        if (Gui.this.currentItem == null) {
                            Gui.this.currentItem = new ItemPanel(false, Gui.this.playlistItem != null);
                            Gui.this.addProgressComponent(Gui.this.currentItem);
                            if (Gui.this.progressPanel.getComponentCount() % 2 == 1) {
                                Gui.this.currentItem.setBackground(new Color(225, 225, 225));
                            }
                        }

                        Gui.this.currentItem.getLabelName().setText(matcher.group(1));
                    } else if ((matcher = Gui.playlistPattern.matcher(line)).matches()) {
                        if (Gui.this.playlistItem == null) {
                            Gui.this.playlistItem = new ItemPanel(false);
                            Gui.this.addProgressComponent(Gui.this.playlistItem);
                            if (Gui.this.progressPanel.getComponentCount() % 2 == 1) {
                                Gui.this.playlistItem.setBackground(new Color(225, 225, 225));
                            }
                        }

                        Gui.this.playlistItem.getLabelName().setText(Messages.getString("label.playlist") + " " + matcher.group(1));
                    } else if (Gui.endPattern.matcher(line).matches()) {
                        Gui.this.currentItem.getLabelStatus().setText(Messages.getString("status.completed"));
                        Gui.this.currentItem.getLabelEta().setText("");
                    } else if ((matcher = Gui.existingPattern.matcher(line)).matches()) {
                        if (Gui.this.currentItem == null) {
                            Gui.this.currentItem = new ItemPanel(false, Gui.this.playlistItem != null);
                            Gui.this.addProgressComponent(Gui.this.currentItem);
                            if (Gui.this.progressPanel.getComponentCount() % 2 == 1) {
                                Gui.this.currentItem.setBackground(new Color(225, 225, 225));
                            }
                        }

                        Gui.this.currentItem.getLabelStatus().setText(Messages.getString("status.completed"));
                        Gui.this.currentItem.getLabelName().setText(matcher.group(1));
                        Gui.this.currentItem.getLabelSize().setText(Gui.this.makeSizeString(Gui.this.currentItem.getLabelName().getText()));
                        Gui.this.currentItem.getProgressBar().setValue(100);
                    } else if ((matcher = Gui.progressPattern.matcher(line)).matches()) {
                        double d = Double.parseDouble(matcher.group(1));
                        Gui.this.currentItem.getLabelStatus().setText(Messages.getString("status.downloading"));
                        if (Gui.this.playlistItem != null) {
                            Gui.this.playlistItem.getLabelStatus().setText(Messages.getString("status.downloading"));
                        }

                        Gui.this.currentItem.getProgressBar().setValue((int) d);
                        Gui.this.taskBarList.SetProgressValue(Gui.this.hwnd, (long) Gui.this.currentItem.getProgressBar().getValue(), (long) Gui.this.currentItem.getProgressBar().getMaximum());
                        Gui.this.currentItem.getLabelSize().setText(matcher.group(2));
                        Gui.this.currentItem.getLabelSpeed().setText(matcher.group(3));
                        Gui.this.currentItem.getLabelEta().setText(matcher.group(4));
                    }

                }
            });
        } else if (line.startsWith("[ffmpeg]")) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Matcher matcher;
                    if ((matcher = Gui.filePattern.matcher(line)).matches()) {
                        if (Gui.this.currentItem == null) {
                            Gui.this.currentItem = new ItemPanel(false);
                            Gui.this.currentItem.getProgressBar().setValue(100);
                            Gui.this.taskBarList.SetProgressValue(Gui.this.hwnd, (long) Gui.this.currentItem.getProgressBar().getValue(), (long) Gui.this.currentItem.getProgressBar().getMaximum());
                            Gui.this.addProgressComponent(Gui.this.currentItem);
                            if (Gui.this.progressPanel.getComponentCount() % 2 == 1) {
                                Gui.this.currentItem.setBackground(new Color(225, 225, 225));
                            }
                        }

                        Gui.this.currentItem.getLabelStatus().setText(Messages.getString("status.converting"));
                        Gui.this.currentItem.getLabelName().setText(matcher.group(1));
                    } else if ((matcher = Gui.mergePattern.matcher(line)).matches()) {
                        Gui.this.currentItem.getLabelStatus().setText(Messages.getString("status.merging"));
                        Gui.this.currentItem.getLabelName().setText(matcher.group(1));
                    }

                }
            });
        } else if (line.startsWith("Deleting original file")) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Gui.this.currentItem.getLabelStatus().setText(Messages.getString("status.completed"));
                    Gui.this.currentItem.getLabelSize().setText(Gui.this.makeSizeString(Gui.this.currentItem.getLabelName().getText()));
                    Gui.this.currentItem.getBtnPlay().setEnabled(true);
                }
            });
        }

        logger.info(line);
    }

    public static boolean isProcessRunning(String serviceName) throws Exception {
        Process p = Runtime.getRuntime().exec(TASKLIST);
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(serviceName)) {
                return true;
            }
        }

        return false;
    }

    public static void killProcess(String serviceName) throws Exception {
        Runtime.getRuntime().exec(KILL + serviceName);
    }

    private void loadProperties() {
        logger.info("loadProperties()");
        Properties props = new Properties();

        try {
            FileInputStream fis = new FileInputStream(new File(CONFIG_PROPERTIES));
            props.load(fis);
            if (props.containsKey(DOWNLOAD_FOLDER_KEY)) {
                this.tfOutput.setText(props.getProperty(DOWNLOAD_FOLDER_KEY));
            }

            if (props.containsKey(LAST_FILE_KEY)) {
                this.tfUrl.setText(props.getProperty(LAST_FILE_KEY));
            }
        } catch (Exception var3) {
            logger.log(Level.SEVERE, "Unable to load the configuration", var3);
        }

    }

    private void saveProperties() {
        logger.info("saveProperties()");
        Properties props = new Properties();
        props.put("downloadFolder", this.tfOutput.getText());
        props.put("lastFile", this.tfUrl.getText());

        try {
            FileOutputStream fos = new FileOutputStream(new File("config.properties"));
            props.store(fos, "Youtube downloader GUI\nConfiguration file\nBy Vincent DEBOSSE");
        } catch (Exception var3) {
            logger.log(Level.SEVERE, "Unable to save the configuration", var3);
        }

    }

    private void saveAndExit() {
        logger.info("saveAndExit()");
        this.saveProperties();
        this.setVisible(false);
        this.dispose();
        System.exit(0);
    }

    private void doClose() {
        if (this.currentProcess == null) {
            this.saveAndExit();
        }

        int i = JOptionPane.showConfirmDialog(this, Messages.getString("dialog.exit.confirm"), Messages.getString("dialog.exit.title"), 2);
        if (i == 0) {
            this.currentProcess.destroy();
            this.saveAndExit();
        }

    }

    private String makeSizeString(String filename) {
        long size = (new File(this.currentItem.getLabelName().getText())).length();
        double printedSize = (double) size;
        String[] units = new String[]{"B", "KiB", "MiB", "GiB", "TiB", "PiB"};
        double factor = 1024.0D;
        DecimalFormat format = new DecimalFormat("0.00");
        format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));

        for (int i = 0; i < units.length; ++i) {
            if (printedSize < factor) {
                return format.format(printedSize) + units[i];
            }

            printedSize /= factor;
        }

        return format.format(printedSize * factor) + units[units.length - 1];
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        this.doClose();
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    public void release(Pointer<?> p) {
        p.release();
    }
}
