package com.uowee.main;

import com.android.ddmlib.*;
import com.uowee.bean.Configure;
import com.uowee.constansts.ConnectModule;
import com.uowee.constansts.ImageIconType;
import com.uowee.constansts.KeyPanelEventType;
import com.uowee.constansts.MouseEventType;
import com.uowee.device.ADB;
import com.uowee.minicap.MiniCapUtil;
import com.uowee.minitouch.MiniTouchUtil;
import com.uowee.observer.*;
import com.uowee.server.LogcatServer;
import com.uowee.server.PerformanceServer;
import com.uowee.utils.DeviceUtil;
import com.uowee.utils.FileUtil;
import com.uowee.widget.AboutFrame;
import com.uowee.widget.KeyPanel;
import com.uowee.widget.PictureLabel;
import com.uowee.widget.ScriptPanel;
import org.jfree.ui.tabbedui.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainUI extends JFrame implements ActionListener, MouseListener, MouseMotionListener, KeyPanelObserver, ScriptPanelObserver {
    private Toolkit toolkit = Toolkit.getDefaultToolkit();

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private JMenuBar menuBar = null;

    private JMenuItem connectIconItem = null;
    private JMenuItem saveIconItem = null;
    private JMenuItem aboutIconItem = null;

    private ScreenImagePanel screenImagePanel = null;
    private ScriptPanel scriptPanel = null;
    private PicturePanel picturePanel = null;
    private JScrollPane scrollPane = null;

    private JPanel screenPanel = null;
    private KeyPanel keyPanel = null;
    private Configure configure = null;
    private final static String CONFIG_FILE_NAME = "config.properties";

    private int width = 0;
    private int height = 0;
    private int picturePanelWidth = 0;
    private int picturePanelHeight = 0;
    private int picturePanel_click_x = 0;
    private int picturePanel_click_y = 0;

    private IDevice device;
    private MiniTouchUtil miniTouch;
    private boolean isDeviceFound = false;
    public static boolean isconnect = false;
    private PerformanceServer ps = null;
    private Thread logcatThread = null;
    private Thread performanceThread = null;

    private String type = "";
    private String element = "";    //查找到的元素
    private int mouse_down_x = 0;
    private int mouse_down_y = 0;
    private BufferedImage ScreenImage = null;

    public MainUI() {
        initConfigure();
        this.setResizable(false);
        Insets screenInsets = toolkit.getScreenInsets(this.getGraphicsConfiguration());
        final int pcBottomHeight = screenInsets.bottom;

        Dimension scrSize = toolkit.getScreenSize();
        width = scrSize.width / 4;
        height = scrSize.height - pcBottomHeight;

        picturePanelWidth = scrSize.width / 2;
        picturePanelHeight = scrSize.height - pcBottomHeight;

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

        scriptPanel = new ScriptPanel(width, height);
        JScrollPane jsep = new JScrollPane(scriptPanel);

        picturePanel = new PicturePanel();
        picturePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        picturePanel.setAutoscrolls(true);
        scrollPane = new JScrollPane(picturePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);


        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(
                groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addComponent(screenPanel, GroupLayout.DEFAULT_SIZE, width, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jsep, GroupLayout.DEFAULT_SIZE, width, Short.MAX_VALUE)
                                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, width * 2, Short.MAX_VALUE)
                                .addGap(2))
        );
        groupLayout.setVerticalGroup(
                groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(screenPanel, GroupLayout.DEFAULT_SIZE, height - 60, Short.MAX_VALUE)
                                        .addComponent(jsep, GroupLayout.DEFAULT_SIZE, height - 60, Short.MAX_VALUE)
                                        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, height - 60, Short.MAX_VALUE))
                                .addGap(5))
        );
        getContentPane().setLayout(groupLayout);
        this.setTitle("Android Tools_" + df.format(new Date()));
        this.setIconImage(toolkit.getImage(this.getClass().getResource("/logo.png")));
        this.setSize((int) scrSize.getWidth(), height);
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
            Thread.sleep(2000);
            screenImagePanel.startMiniTouch();
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
        if (e.getSource() == screenImagePanel) {
            try {
                BufferedImage image = new BufferedImage(screenPanel.getWidth(), screenPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = image.createGraphics();
                screenPanel.paint(g2);
                ScreenImage = image;    //截图ScreenPanel中Image
                int x = e.getX();
                int y = e.getY();
                mouse_down_x = x;
                mouse_down_y = y;
                type = MouseEventType.CLICK.getCode();
                //  recordClickResult(x, y);

                miniTouch.touchDown(new Point(e.getX(), e.getY()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!isconnect) {
            return;
        }
        if (e.getSource() == screenImagePanel) {
            int width = screenPanel.getWidth();
            int height = screenPanel.getHeight();
            int x = e.getX();
            int y = e.getY();
            BufferedImage newimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
            Graphics g = newimage.getGraphics();
            g.drawImage(ScreenImage, 0, 0, width, height, null);

            Graphics2D g2 = (Graphics2D) newimage.getGraphics();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
            String time = dateFormat.format(new Date());
            g2.setColor(Color.RED);
            Font font = new Font("黑体", Font.PLAIN, 25);
            g2.setFont(font);
//			g2.drawString(time, width/2-50,height/2);
            g2.setStroke(new BasicStroke(4));
            if (type.equals(MouseEventType.CLICK.getCode())) {
                scriptPanel.append(element);
                g2.drawOval(mouse_down_x - 25, mouse_down_y - 25, 50, 50);
            } else {

            }


            miniTouch.touchUp();
        }
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
        if (e.getSource() == screenImagePanel) {

            int x = e.getX();
            int y = e.getY();
            boolean ismoved = Math.abs(mouse_down_x - x) > 100 || Math.abs(mouse_down_y - y) > 100 ? true : false;
            if (!ismoved) {
                return;
            }
            BufferedImage image = new BufferedImage(screenPanel.getWidth(), screenPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            screenPanel.paint(g2);
            ScreenImage = image;    //截图ScreenPanel中Image
            type = MouseEventType.MOTION.getCode();
            miniTouch.touchMove(new Point(e.getX(), e.getY()));
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void onElementFound(String result) {

    }

    @Override
    public void onItemClick(String type) {
        if (!isconnect) {
            return;
        }
        int x = 0;
        if (type.equals(KeyPanelEventType.BACK.getCode())) {
            x = keyPanel.back.getX();
        } else if (type.equals(KeyPanelEventType.HOME.getCode())) {
            x = keyPanel.home.getX();
        } else {
            x = keyPanel.menu.getX();
        }
        int y = screenPanel.getHeight();
        int width = screenPanel.getWidth();
        int height = screenPanel.getHeight();
        BufferedImage image = new BufferedImage(screenPanel.getWidth(), screenPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        screenPanel.paint(g2);
        BufferedImage newimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        Graphics g = newimage.getGraphics();
        g.drawImage(image, 0, 0, width, height, null);

        Graphics2D g2D = (Graphics2D) newimage.getGraphics();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
        String time = dateFormat.format(new Date());
        g2D.setColor(Color.RED);
        Font font = new Font("黑体", Font.PLAIN, 25);
        g2D.setFont(font);
        g2D.setStroke(new BasicStroke(4));
        g2D.drawOval(x, y - 60, 50, 50);
    }

    class PicturePanel extends JPanel implements PicturePanelObserver {
        public PicturePanel() {

        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension dimension = null;
            if (this.getComponentCount() > 0) {
                int height = this.getComponent(0).getHeight(); //子控件的高度
                int total_height = 0;
                int count = getComponentCount();
                if (count % 2 == 0) {    //如果能被3整除
                    total_height = count / 2 * (height) + (count / 2 - 1) * 5;
                } else {
                    total_height = (count / 2 + 1) * (height) + (count / 2) * 5;
                }
                picturePanelHeight = total_height;
                dimension = new Dimension(this.getWidth(), total_height);
                scrollPane.getViewport().setViewPosition(new Point(this.getWidth(), total_height));//JscrollPanel自动滑动到底部
            } else {
                return super.getPreferredSize();
            }
            return dimension;
        }

        @Override
        public void onDrawStringMessage(String message) {
            System.out.println("receive message :" + message);
            Component pl = picturePanel.getComponentAt(new Point(picturePanel_click_x, picturePanel_click_y));
            System.out.println(pl.getClass().getName());
            if (!(pl instanceof PictureLabel)) {
                return;
            }
            System.out.println("order:" + getComponentZOrder(pl));
            PictureLabel pic = (PictureLabel) pl;
            ImageIcon icon = (ImageIcon) pic.getIcon();
            BufferedImage image = (BufferedImage) icon.getImage();
            Graphics2D g2 = (Graphics2D) image.getGraphics();
            Font font = new Font("黑体", Font.PLAIN, 15);
            g2.setFont(font);
            g2.setColor(Color.BLUE);
            char[] array = message.toCharArray();
            int width = pl.getWidth();
            int show_line = width / 15;//每行能显示多少个字符
            System.out.println(array.length);
            System.out.println("show_line:" + show_line);
            if (array.length > show_line) {
                int count = 0;
                if (array.length % show_line == 0) {    //能被整除
                    count = array.length / show_line;//要显示多少行才能显示完
                } else {
                    count = array.length / show_line + 1;
                }
                System.out.println("count:" + count);
                for (int i = 0; i < count; i++) {
                    if (i == count - 1) {                        //最后一行,endIndex为字符串剩余的长度
                        String line = message.substring(i * (show_line), array.length);
                        System.out.println(line);
                        g2.drawString(line, 0, pl.getHeight() - 20 * (count - i - 1));
                    } else {
                        String line = message.substring(i * (show_line), show_line * (i + 1));
                        System.out.println(line);
                        g2.drawString(line, 0, pl.getHeight() - 20 * (count - i - 1));
                    }
                }
            } else {
                g2.drawString(message, 0, pl.getHeight() - 25);
            }
            pl.repaint();
        }
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

        public void startMiniTouch() throws Exception {
            miniTouch = new MiniTouchUtil(device, this.getWidth(), this.getHeight());
            miniTouch.startMiniTouch();
        }

        public void changePointConvert(int widht, int height) {
            miniTouch.changePointConvert(widht, height);
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
