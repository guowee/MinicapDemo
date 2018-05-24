package com.uowee.main;

import com.android.ddmlib.*;
import com.uowee.bean.Configure;
import com.uowee.constansts.ConnectModule;
import com.uowee.constansts.ImageIconType;
import com.uowee.device.ADB;
import com.uowee.minicap.MiniCapUtil;
import com.uowee.observer.AndroidConnectObserver;
import com.uowee.observer.AndroidScreenObserver;
import com.uowee.server.LogcatServer;
import com.uowee.server.PerformanceServer;
import com.uowee.utils.DeviceUtil;
import com.uowee.utils.FileUtil;
import com.uowee.widget.AboutFrame;
import com.uowee.widget.KeyPanel;
import org.jfree.ui.tabbedui.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainUI extends JFrame implements ActionListener, MouseListener, MouseMotionListener {
    private Toolkit toolkit = Toolkit.getDefaultToolkit();

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private JMenuBar menuBar = null;

    private JMenuItem connectIconItem = null;
    private JMenuItem saveIconItem = null;
    private JMenuItem aboutIconItem = null;

    private ScreenImagePanel screenImagePanel = null;
    private JPanel screenPanel = null;
    private KeyPanel keyPanel = null;
    private Configure configure = null;
    private final static String CONFIG_FILE_NAME = "config.properties";

    private int width = 0;
    private int height = 0;

    private IDevice device;
    private boolean isDeviceFound = false;
    public static boolean isconnect = false;
    private PerformanceServer ps = null;
    private Thread logcatThread = null;
    private Thread performanceThread = null;

    public MainUI() {
        initConfigure();
        this.setResizable(false);
        Insets screenInsets = toolkit.getScreenInsets(this.getGraphicsConfiguration());
        final int pcBottomHeight = screenInsets.bottom;

        Dimension scrSize = toolkit.getScreenSize();
        width = scrSize.width / 4;
        height = scrSize.height - pcBottomHeight;


        connectIconItem = new JMenuItem();
        saveIconItem = new JMenuItem();
        aboutIconItem = new JMenuItem();

        ImageIcon connectIcon = new ImageIcon(getClass().getClassLoader().getResource("images/connect.png"));
        ImageIcon saveIcon = new ImageIcon(getClass().getClassLoader().getResource("images/save.png"));
        ImageIcon aboutIcon = new ImageIcon(getClass().getClassLoader().getResource("images/about.png"));

        connectIconItem.setIcon(connectIcon);
        connectIconItem.setToolTipText("Connect");
        connectIconItem.addActionListener(this);

        saveIconItem.setIcon(saveIcon);
        saveIconItem.setToolTipText("Save");
        saveIconItem.addActionListener(this);

        aboutIconItem.setIcon(aboutIcon);
        aboutIconItem.setToolTipText("About");
        aboutIconItem.addActionListener(this);


        JPanel jpanel = new JPanel();
        jpanel.add(connectIconItem);
        jpanel.add(saveIconItem);
        jpanel.add(aboutIconItem);
        jpanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        menuBar = new JMenuBar();
        menuBar.add(jpanel);
        this.setJMenuBar(menuBar);


        screenImagePanel = new ScreenImagePanel();
        screenImagePanel.setVisible(true);
        screenImagePanel.setPreferredSize(new Dimension(width, height - 150));
        screenImagePanel.addMouseListener(this);
        screenImagePanel.addMouseMotionListener(this);

        keyPanel = new KeyPanel(width, 20);


        screenPanel = new JPanel();
        screenPanel.setSize(width, height - 60);
        screenPanel.setLayout(new VerticalLayout());
        screenPanel.add(screenImagePanel);
        screenPanel.add(keyPanel);
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(
                groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addComponent(screenPanel, GroupLayout.DEFAULT_SIZE, width, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGap(2))
        );
        groupLayout.setVerticalGroup(
                groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(screenPanel, GroupLayout.DEFAULT_SIZE, height - 60, Short.MAX_VALUE)
                                        .addGap(2)))
        );
        getContentPane().setLayout(groupLayout);
        this.setTitle("Android Tools_" + df.format(new Date()));
        this.setIconImage(toolkit.getImage(this.getClass().getResource("/logo.png")));
        this.setSize((int) scrSize.getWidth() / 4, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setLocationRelativeTo(null);

    }

    public void initConfigure() {
        configure = new Configure();
        String logFilter = FileUtil.getConfigValue(CONFIG_FILE_NAME, "LogFilter");
        String connectModule = FileUtil.getConfigValue(CONFIG_FILE_NAME, "Connect");


        if (ConnectModule.WIFI_MODULE.getCode().equals(connectModule)) {
            configure.setModule(connectModule);
        } else {
            configure.setModule(ConnectModule.USB_MODULE.getCode());
        }


        configure.setLogFilter(logFilter);
    }

    public static void main(String[] args) {

        new MainUI();
    }

    /**
     * ActionListener
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == connectIconItem) {
            screenImagePanel.setIcon(ImageIconType.CONNECTING.getCode());
            connectIconItem.setEnabled(false);
            startADB();

        } else if (e.getSource() == saveIconItem) {
            JOptionPane.showMessageDialog(null, "save", "Save", JOptionPane.ERROR_MESSAGE);
        } else if (e.getSource() == aboutIconItem) {
            AboutFrame aboutFrame = new AboutFrame();
            aboutFrame.show();
        }
    }

    public void startADB() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String module = configure.getModule();
                if (ConnectModule.WIFI_MODULE.getCode().equals(module)) {
                    startByModule(ConnectModule.WIFI_MODULE.getCode());
                } else {
                    startByModule(ConnectModule.USB_MODULE.getCode());
                }
            }
        }).start();
    }

    public void startByModule(String module) {
        try {
            DeviceUtil d = new DeviceUtil();
            d.startDeviceModule(module);

            ADB adb = new ADB(module);
            device = adb.getDevice();
            isDeviceFound = true;

            screenImagePanel.setDevice(device);
            Thread.sleep(2000);
            screenImagePanel.startMiniCap();
            startMonitor();
            isconnect = true;

        } catch (Exception e1) {
            screenImagePanel.setIcon(ImageIconType.CONNECTFAILED.getCode());//启动中出现任何的异常
            connectIconItem.setEnabled(true);
            isconnect = false;
            stopMonitor();
        }
    }


    public void startMonitor() {
        try {
            FileUtil.DeleteFolder("TestResult");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (logcatThread == null) {
            logcatThread = new Thread(new LogcatServer(device.getSerialNumber(), configure.getLogFilter()));
            logcatThread.start();
        }

        if (performanceThread == null) {
            ps = new PerformanceServer(device, configure.getLogFilter());
            performanceThread = new Thread(ps);
            performanceThread.start();
        }
    }

    public void stopMonitor() {
        if (logcatThread != null && logcatThread.isAlive()) {
            logcatThread.stop();
            logcatThread = null;
        }

        if (performanceThread != null && performanceThread.isAlive()) {
            performanceThread.stop();
            performanceThread = null;
            stopLogcat();
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
        String fileName = "TestResult_" + dateFormat.format(new Date()) + ".zip";
        FileUtil.compress("TestResult", fileName);
    }


    public void stopLogcat() {
        CollectingOutputReceiver output = new CollectingOutputReceiver();
        String command = "ps|grep logcat";
        String killLogcat = "kill %s";
        try {
            device.executeShellCommand(command, output, 0);
            String[] array = output.getOutput().split("\\s+");
            device.executeShellCommand(String.format(killLogcat, array[1]), output, 0);
        } catch (TimeoutException | AdbCommandRejectedException
                | ShellCommandUnresponsiveException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * MouseListener
     *
     * @param e
     */
    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    /**
     * MouseMotionListener
     *
     * @param e
     */
    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    class ScreenImagePanel extends JPanel implements AndroidScreenObserver, AndroidConnectObserver {
        private BufferedImage image = null;
        private JLabel label;
        private ImageIcon bg = null;
        private ImageIcon connect_failed = null;
        private ImageIcon connecting = null;
        private IDevice device = null;
        private MiniCapUtil minicap = null;

        public ScreenImagePanel() {
            initImageIcon();
            label = new JLabel();
            label.setSize(new Dimension(width - 20, height));
            label.setIcon(bg);
            setLayout(new FlowLayout());
            add(label);
        }


        public void initImageIcon() {
            ImageIcon tmp = new ImageIcon(MainUI.class.getResource("/images/bg.png"));
            Image image = tmp.getImage().getScaledInstance(width - 20, height - 150, Image.SCALE_DEFAULT);
            bg = new ImageIcon(image);

            tmp = new ImageIcon(MainUI.class.getResource("/images/connect_failed.png"));
            image = tmp.getImage().getScaledInstance(width - 20, height - 150, Image.SCALE_DEFAULT);
            connect_failed = new ImageIcon(image);

            tmp = new ImageIcon(MainUI.class.getResource("/images/connecting.gif"));
            image = tmp.getImage().getScaledInstance(width - 20, height - 150, Image.SCALE_DEFAULT);
            connecting = new ImageIcon(image);
        }

        public void setIcon(String type) {
            if (ImageIconType.WAITFORCONNECT.getCode().equals(type)) {
                label.setIcon(bg);
            } else if (ImageIconType.CONNECTING.getCode().equals(type)) {
                label.setIcon(connecting);
            } else if (ImageIconType.CONNECTFAILED.getCode().equals(type)) {
                label.setIcon(connect_failed);
            }
        }

        public void setDevice(IDevice device) {
            this.device = device;
        }

        public void startMiniCap() throws Exception {
            minicap = new MiniCapUtil(device);
            minicap.registerScreenObserver(this);
            minicap.registerConnectObserver(this);
            minicap.startScreenListener();
        }


        @Override
        public void onDisConnect() {
            isconnect = false;
            image = null;
            setIcon(ImageIconType.WAITFORCONNECT.getCode());
            connectIconItem.setEnabled(true);
            stopMonitor();
            stopLogcat();
            JOptionPane.showMessageDialog(null, "屏幕同步中断,请检查连接后重试", "错误", JOptionPane.CLOSED_OPTION);
        }

        @Override
        public void frameImageChange(Image image) {
            this.image = (BufferedImage) image;
            this.repaint();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (!isconnect) {
                return;
            }
            try {
                if (image == null)
                    return;
                g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
                g.dispose();
                image.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
