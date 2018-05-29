package com.uowee.widget;

import com.uowee.utils.FileUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

public class ScriptPanel extends JPanel implements KeyListener {
    private static JTextArea jtextArea = null;

    public ScriptPanel(int width, int height) {
        this.setSize(width, height);
        this.setBackground(Color.WHITE);
        jtextArea = new JTextArea();
        jtextArea.setLineWrap(true);
        jtextArea.setSize(new Dimension(this.getWidth(), this.getHeight()));
        jtextArea.addKeyListener(this);
        add(jtextArea);
    }

    public void append(String str) {
        jtextArea.append(str);
        jtextArea.append("\n");
    }


    @Override
    public void keyPressed(KeyEvent e) {

        if (e.isControlDown() && e.VK_S == e.getKeyCode()) { //CTRL + S 保存
            if ("".equals(jtextArea.getText())) {
                return;
            }
            JFileChooser chooser = new JFileChooser();
            int retval = chooser.showSaveDialog(this);
            if (retval == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    String fileName = file.getAbsolutePath();
                    FileUtil.Write(fileName, jtextArea.getText().toString().trim());
                    jtextArea.setText("");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }

        }

        if (e.isControlDown() && e.VK_D == e.getKeyCode()) {                //CTRL+D  清空
            jtextArea.setText("");
            return;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }


    @Override
    public void keyReleased(KeyEvent e) {

    }
}
