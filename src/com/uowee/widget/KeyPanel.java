package com.uowee.widget;

import com.uowee.constansts.KeyPanelEventType;
import com.uowee.observer.KeyPanelObserver;
import com.uowee.observer.KeyPanelSubject;
import com.uowee.utils.Command;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class KeyPanel extends JPanel implements MouseListener, KeyPanelSubject {
    public JLabel back = null;
    public JLabel home = null;
    public JLabel menu = null;
    private final String BACK_COMMAND = "adb shell input keyevent 4";
    private final String HOME_COMMAND = "adb shell input keyevent 3";
    private final String MENU_COMMAND = "adb shell input keyevent 82";
    private Command command;
    private List<KeyPanelObserver> observers = new ArrayList<KeyPanelObserver>();

    public KeyPanel(int width, int height) {
        this.command = new Command();
        this.setSize(width, height);
        back = new JLabel();
        back.setIcon(new ImageIcon(KeyPanel.class.getResource("/images/icon-back-hover.png")));
        home = new JLabel();
        home.setIcon(new ImageIcon(KeyPanel.class.getResource("/images/icon-home-hover.png")));
        menu = new JLabel();
        menu.setIcon(new ImageIcon(KeyPanel.class.getResource("/images/icon-list-hover.png")));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(Box.createVerticalStrut(10));
        add(back);
        this.add(Box.createVerticalStrut(10));
        add(home);
        this.add(Box.createVerticalStrut(10));
        add(menu);
        this.add(Box.createVerticalStrut(10));

        back.addMouseListener(this);
        home.addMouseListener(this);
        menu.addMouseListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        if (event.getSource() == back) {
            command.executeCommand(BACK_COMMAND, null);
            this.notifyObservers(KeyPanelEventType.BACK.getCode());
            return;
        }

        if (event.getSource() == home) {
            command.executeCommand(HOME_COMMAND, null);
            this.notifyObservers(KeyPanelEventType.HOME.getCode());
            return;
        }

        if (event.getSource() == menu) {
            command.executeCommand(MENU_COMMAND, null);
            this.notifyObservers(KeyPanelEventType.MENU.getCode());
            return;
        }
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {

    }

    @Override
    public void mouseExited(MouseEvent arg0) {

    }

    @Override
    public void mousePressed(MouseEvent arg0) {

    }

    @Override
    public void mouseReleased(MouseEvent arg0) {


    }

    @Override
    public void registerObserver(KeyPanelObserver o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(KeyPanelObserver o) {
        int index = observers.indexOf(o);
        if (index != -1) {
            observers.remove(o);
        }
    }

    @Override
    public void notifyObservers(String type) {
        for (KeyPanelObserver observer : observers) {
            observer.onItemClick(type);
        }
    }
}
