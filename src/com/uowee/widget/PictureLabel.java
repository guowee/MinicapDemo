package com.uowee.widget;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class PictureLabel extends JLabel {

    public PictureLabel(int width, int height, Image image) {
        this.setSize(width, height);
        BufferedImage newimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        newimage.getGraphics().drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
        ImageIcon ic = new ImageIcon();
        ic.setImage(newimage);
        this.setIcon(ic);
        this.setVerticalTextPosition(JLabel.BOTTOM);    //设置文字显示在底部
        this.setHorizontalTextPosition(JLabel.CENTER);
    }
}
