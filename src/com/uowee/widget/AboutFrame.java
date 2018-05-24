package com.uowee.widget;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

public class AboutFrame extends JFrame {
    public AboutFrame() {

        this.setTitle("About");
        this.setResizable(false);
        this.setSize(300, 200);
        this.setLocationRelativeTo(null);

        JLabel lblNewLabel = new JLabel("Copyright © 2018 WEE. All Rights Reserved.");
        JLabel lblNewLabel_1 = new JLabel("Android Tools  V1.0.0");

        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(
                groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addGap(20)
                                .addComponent(lblNewLabel)
                                .addContainerGap(50, Short.MAX_VALUE))
                        .addGroup(groupLayout.createSequentialGroup()
                                .addGap(80)
                                .addComponent(lblNewLabel_1)
                                .addContainerGap(100, Short.MAX_VALUE))
        );
        groupLayout.setVerticalGroup(
                groupLayout.createParallelGroup(Alignment.TRAILING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addGap(40)
                                .addComponent(lblNewLabel_1)
                                .addPreferredGap(ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
                                .addComponent(lblNewLabel)
                                .addContainerGap())
        );
        getContentPane().setLayout(groupLayout);
    }
}
