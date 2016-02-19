package com.gsa.signingtool.app;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class Gui {
    protected static JFrame frame = 
            new JFrame("GSA Document Signing Tool 2.0");
    protected static Logger logger = Logger.getAnonymousLogger();
    protected static String revocationStatus = "Not Checked";
    protected static String serialNum = null;
    protected static String notAfterDate = null;
    protected File file = null;
    protected SigningTool pkcs7sign = null;
    protected SigningToolVerifier pkcs7unpack = null;
    protected static DateFormat dateFormat = 
            new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    protected static Date date = new Date();
    protected static DateFormat logDate = 
            new SimpleDateFormat("yyyy-MM-dd"); 
    protected static int counter = 0;
    protected JFileChooser fileChooser = new JFileChooser();
    protected static boolean errors = false;
    private volatile boolean done = false;
    protected static int progressValue = 0;
    protected static SwingWorker<Boolean, String> worker;
    protected static FileHandler fh;
    
    private javax.swing.JButton browseButton;
    private javax.swing.JMenuItem closeApp;
    private javax.swing.JLabel fileDestLabel;
    private javax.swing.JTextField fileDestination;
    private javax.swing.JTextField fileInput;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem aboutMenu;
    private javax.swing.JScrollPane statusScrollPane;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenuItem openFile;
    private javax.swing.JMenu optionsMenu;
    private javax.swing.JPasswordField pin;
    private javax.swing.JLabel pinLabel;
    protected static javax.swing.JProgressBar progress;
    protected static javax.swing.JCheckBoxMenuItem revocationCheckBox;
    protected javax.swing.JButton signButton;
    protected javax.swing.JButton verifyButton;
    protected static javax.swing.JTextArea status;
    private javax.swing.JLabel statusLabel;
    private GroupLayout layout;
    private JPanel pane;
            
                      
    public Component createComponents(JFrame frame) {

        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        browseButton = new javax.swing.JButton();
        fileInput = new javax.swing.JTextField();
        fileDestLabel = new javax.swing.JLabel();
        fileDestination = new javax.swing.JTextField();
        signButton = new javax.swing.JButton();
        verifyButton = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();
        statusScrollPane = new javax.swing.JScrollPane();
        status = new javax.swing.JTextArea();
        pin = new javax.swing.JPasswordField();
        pinLabel = new javax.swing.JLabel();
        progress = new javax.swing.JProgressBar();
        menuBar = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        openFile = new javax.swing.JMenuItem();
        closeApp = new javax.swing.JMenuItem();
        optionsMenu = new javax.swing.JMenu();
        revocationCheckBox = new javax.swing.JCheckBoxMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenu = new javax.swing.JMenuItem();
        pane = new JPanel();
        layout = new GroupLayout(pane);
        
        // Settings for progress status bar
        progress.setIndeterminate(false);
        progress.setStringPainted(true);
        
        browseButton.setMnemonic('o');
        browseButton.setText("Open");
        browseButton.setToolTipText("Click to select a file to sign");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        fileInput.setEditable(false);
        fileInput.setText("Please select a file to sign or unpack");
        fileInput.setToolTipText("Click the open button to select a file");

        fileDestLabel.setText("File Destination:");
        fileDestLabel.setToolTipText("File Destination:");

        fileDestination.setEditable(false);
        fileDestination.setText("No file selected");
        fileDestination.setToolTipText("This is the file destination of "
                + "the file you would like signed or unpacked.");

        signButton.setMnemonic('s');
        signButton.setText("Sign");
        signButton.setToolTipText("Click to sign file selected");
        signButton.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {
               progress.setValue(0);
               status.setText(dateFormat.format(new Date()) 
                       + " - Applying Signature\n");
               progress.setValue(8);
               worker = createWorker(status, progress);
               worker.execute();
           }
        });
        
        verifyButton.setMnemonic('u');
        verifyButton.setText("Unpack");
        verifyButton.setToolTipText("Click to unpack file selected");
        verifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                progress.setValue(0);
                status.setText(dateFormat.format(new Date()) 
                        + " - Unpacking File\n");
                progress.setValue(8);
                worker = createVerifyWorker(status, progress);
                worker.execute();
            }
        });
        
        statusLabel.setText("Status:");

        status.setEditable(false);
        status.setColumns(20);
        status.setFont(new java.awt.Font("Tahoma", 0, 11));
        status.setRows(5);
        status.setAutoscrolls(false);
        status.setText(dateFormat.format(date) 
                + " - Select a file, enter your PIN #, and then Click the Sign button.\n");
        
        statusScrollPane.setViewportView(status);

        pin.setToolTipText("Please Enter your card's PIN Number");
        pin.setName("pin"); // NOI18N

        pinLabel.setText("Enter PIN:");
        pinLabel.setToolTipText("Enter PIN Label");

        menuFile.setMnemonic('f');
        menuFile.setText("File");

        openFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openFile.setIcon(new javax.swing.ImageIcon(getClass().getResource(
                "/com/gsa/signingtool/images/open.png"))); // NOI18N
        openFile.setMnemonic('o');
        openFile.setText("Open");
        openFile.setToolTipText("Open a file to sign");
        openFile.setInheritsPopupMenu(true);
        openFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFileActionPerformed(evt);
            }
        });
        menuFile.add(openFile);

        closeApp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        closeApp.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/com/gsa/signingtool/images/close.png")));
        closeApp.setText("Close");
        closeApp.setToolTipText("Close Application");
        closeApp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.exit(0);
            }
        });
        menuFile.add(closeApp);
        closeApp.getAccessibleContext().setAccessibleParent(menuBar);

        optionsMenu.setMnemonic('p');
        optionsMenu.setText("Options");
        optionsMenu.setToolTipText("Options Menu Item");
        revocationCheckBox.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        revocationCheckBox.setSelected(true);
        revocationCheckBox.setText("Enable Revocation Checking");
        revocationCheckBox.setToolTipText("Click to toggle revocation checking");
        optionsMenu.add(revocationCheckBox);

        helpMenu.setMnemonic('h');
        helpMenu.setText("Help");
        helpMenu.setToolTipText("Help Menu Item");

        aboutMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        aboutMenu.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/com/gsa/signingtool/images/info.png"))); // NOI18N
        aboutMenu.setText("About");
        aboutMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenu);

        
        // add menuBar items and menuBar to the frame
        menuBar.add(menuFile);
        menuBar.add(optionsMenu);
        menuBar.add(helpMenu);
        
        frame.setJMenuBar(menuBar);

        browseButton.getAccessibleContext().setAccessibleName("Open Button");
        browseButton.getAccessibleContext().setAccessibleDescription(
                "Click button to open a file to sign");
        browseButton.getAccessibleContext().setAccessibleParent(frame);
        fileInput.getAccessibleContext().setAccessibleName("File selected");
        fileInput.getAccessibleContext().setAccessibleDescription(
                "Please select a file to sign");
        fileInput.getAccessibleContext().setAccessibleParent(frame);
        fileDestLabel.getAccessibleContext().setAccessibleParent(frame);
        fileDestination.getAccessibleContext().setAccessibleName(
                "File Destination");
        signButton.getAccessibleContext().setAccessibleName("Sign Button");
        signButton.getAccessibleContext().setAccessibleDescription(
                "Click button to sign a file");
        signButton.getAccessibleContext().setAccessibleParent(frame);
        verifyButton.getAccessibleContext().setAccessibleDescription(
                "Click button to unpack a file");
        verifyButton.getAccessibleContext().setAccessibleName(
                "Unpack Button");
        verifyButton.getAccessibleContext().setAccessibleParent(frame);
        pin.getAccessibleContext().setAccessibleName("PIN Number");
        pin.getAccessibleContext().setAccessibleDescription(
                "Text field for entering your PIN Number");
        pin.getAccessibleContext().setAccessibleParent(frame);
        pinLabel.getAccessibleContext().setAccessibleName("PIN Label");
        pinLabel.getAccessibleContext().setAccessibleParent(frame);
        status.getAccessibleContext().setAccessibleName("Status");
        status.getAccessibleContext().setAccessibleParent(frame);
        status.getAccessibleContext().setAccessibleDescription(
                "Scroll pane that shows the status of the signing tool");

        // Set application default parameters
        frame.getAccessibleContext().setAccessibleDescription(
                "GSA Document Signing Tool");
        frame.setIconImage(new ImageIcon(getClass().getResource(
                "/com/gsa/signingtool/images/icon.png")).getImage());
        
        pane.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(
                        javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(signButton, javax.swing.GroupLayout.DEFAULT_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(verifyButton, javax.swing.GroupLayout.DEFAULT_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fileDestLabel, javax.swing.GroupLayout.Alignment.TRAILING,
                            javax.swing.GroupLayout.DEFAULT_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(statusLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pinLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(browseButton, javax.swing.GroupLayout.Alignment.TRAILING,
                            javax.swing.GroupLayout.PREFERRED_SIZE, 77,
                            javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(progress, javax.swing.GroupLayout.PREFERRED_SIZE,
                            450, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(pin, javax.swing.GroupLayout.PREFERRED_SIZE,
                                100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(fileInput)
                        .addComponent(fileDestination, javax.swing.GroupLayout.PREFERRED_SIZE,
                                450, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(statusScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE,
                            450, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileInput, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE,
                            javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fileDestLabel)
                    .addComponent(fileDestination, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE,
                            javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pinLabel)
                    .addComponent(pin, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE,
                            javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(statusLabel)
                    .addComponent(progress, javax.swing.GroupLayout.PREFERRED_SIZE, 23,
                            javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(signButton)
                            .addGap(18,18,18)
                        .addComponent(verifyButton)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(statusScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE,
                            174, Short.MAX_VALUE))
                 .addContainerGap()
            ));   
        frame.pack();  
        return pane;
    }                      
    
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {                                             
        int returnVal = fileChooser.showOpenDialog(frame);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    fileInput.setText(file.getAbsolutePath());
                    fileDestination.setText(file.getAbsolutePath().substring(
                            0,file.getAbsolutePath().lastIndexOf(File.separator)));
                }
    }                                            
   
    private void openFileActionPerformed(java.awt.event.ActionEvent evt) {                                         
        int returnVal = fileChooser.showOpenDialog(frame);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    fileInput.setText(file.getAbsolutePath());
                    fileDestination.setText(file.getAbsolutePath().substring(
                            0,file.getAbsolutePath().lastIndexOf(File.separator)));
                }
    }                                                                         

    private void aboutMenuActionPerformed(java.awt.event.ActionEvent evt) {                                           
        JOptionPane.showMessageDialog(frame, "The GSA Document Signing Tool "
                + "is a free tool provided\n"
                + " by the General Services Administration (GSA). The primary\n"
                + " use of this tool is intended for users to be able to\n"
                + " digitally sign a file using their PIV card and submit\n"
                + " this file to the Federal Register.\n\n"
                + " Please use the contact us form on idmanagement.gov\n"
                + " to submit any questions regarding this tool.\n\n",
                "About the PKCS#7 Signing Tool", 1);
        
    }                                                                        
   
    public SwingWorker<Boolean, String> createVerifyWorker(
            final javax.swing.JTextArea status, final JProgressBar progress) {
        return new SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (file == null) {
                    logger.severe("No file was selected");
                    String updateText = (dateFormat.format(new Date()) 
                            + " - No file selected. Please select a file to sign.\n");
                    errors=true;
                    progress.setValue(100);
                    publish(updateText);
                } else {
                    errors=false;
                    pkcs7unpack = new SigningToolVerifier(file);
                }
                return true;
            }
            
            @Override
            protected void process(List<String> chunks) {
                super.process(chunks);
                for (String chunk : chunks) {
                    status.append(chunk);
                    progress.setValue(progress.getValue() + 1);
                }
            }
            
            @Override
            protected void done() {
                try {
                    Boolean ack = get();
                    if (Boolean.TRUE.equals(ack)) {
                        if (errors == false) {
                            logger.info("File was verified/unpacked successfully"
                                    + " with no errors.");
                            status.append(dateFormat.format(new Date()) 
                                    + " - File has been successfully unpacked.");
                        } else {
                            logger.severe("Errors were found while "
                                    + "unpacking action was running");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                  }
            }
        };
    }
    public SwingWorker<Boolean, String> createWorker(
            final javax.swing.JTextArea status, final JProgressBar progress) {
        return new SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                revocationStatus = "Not Checked";
                if (pin.getPassword().length ==0) {
                    logger.severe("PIN is empty");
                    String updateText = dateFormat.format(new Date()) 
                            + " - PIN field is empty. Please enter a valid PIN #.\n";
                    errors=true;
                    progress.setValue(100);
                    publish(updateText);

                } else if(file == null) {
                    logger.severe("No file was selected");
                    String updateText = (dateFormat.format(new Date()) 
                            + " - No file selected. Please select a file to sign.\n");
                    errors=true;
                    progress.setValue(100);
                    publish(updateText);
                    
                } else {
                    errors=false;
                    pkcs7sign = new SigningTool(file, pin.getPassword());
                }
                return true;
            }
            
            @Override
            protected void process(List<String> chunks) {
                super.process(chunks);
                for (String chunk : chunks) {
                    status.append(chunk);
                    progress.setValue(progress.getValue() + 1);
                }
            }
                
            @Override
            protected void done() {
                try {
                    Boolean ack = get();
                    if (Boolean.TRUE.equals(ack)) {
                        if (errors == false) {
                            logger.info("File was signed successfully with no errors.");

                            status.append(dateFormat.format(new Date()) 
                                    + " - Signing Cert Expiration Date: " 
                            + notAfterDate 
                            + "\n" + dateFormat.format(new Date()) 
                                    + " - Signing Cert Revocation Status: " 
                            + revocationStatus 
                            + "\n"
                            + dateFormat.format(new Date()) 
                                    + " - File has been successfully signed.");
                        } else {
                            logger.severe("Errors were found when "
                                    + "signing action was completed");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    
                }
  
                }
            };
        }
    public static void main(String args[]) {
        

        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    //javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Gui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Gui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Gui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Gui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        try {
            fh = new FileHandler("logs\\log-" + logDate.format(new Date()) 
                    + ".txt", true);
            logger.addHandler(fh);
            fh.setFormatter(new SimpleFormatter());
            logger.setLevel(Level.FINEST);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                
                        Gui gui = new Gui();
                        Component content = gui.createComponents(frame);
                        frame.getContentPane().add(content, BorderLayout.CENTER);

                        frame.addWindowListener(new WindowAdapter() {
                                public void windowClosing(WindowEvent e) {
                                        fh.close();
                                        System.exit(0);
                                }
                        });
                        frame.pack();
                        frame.setLocationRelativeTo(null);
                        frame.setVisible(true);
                    }
                });
        } catch(Exception e) {
            e.printStackTrace();
        }
        logger.info("Launching the GSA Document Signing Tool Application...");
    }
    
}
