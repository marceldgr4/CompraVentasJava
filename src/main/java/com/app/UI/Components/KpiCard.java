package com.app.UI.Components;

import javax.swing.*;
import java.awt.*;

/** Tarjeta KPI con título, icono, valor y color de acento. */
public class KpiCard extends JPanel {
    private final JLabel valueLabel;
    private final Color accentColor;

    KpiCard(String title, String icon, Color accentColor) {
        this.accentColor = accentColor;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        JPanel accentPanel = new JPanel();
        accentPanel.setBackground(accentColor);
        accentPanel.setPreferredSize(new Dimension(5,0));
        add(accentPanel, BorderLayout.WEST);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createCompoundBorder(0,12,0,0));

        JLabel iconLabelPanel = new JLabel(icon);
        iconLabelPanel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        iconLabelPanel.setForeground(accentColor);

        JLabel titleLabelPanel = new JLabel(title);
        titleLabelPanel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabelPanel.setForeground(Color.GRAY);

        valueLabel = new JLabel("...");
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueLabel.setForeground(new  Color(30,42,74));

        content.add(iconLabelPanel);
        content.add(Box.createVerticalStrut(4));
        content.add(valueLabel);
        content.add(Box.createVerticalStrut(2));
        content.add(titleLabelPanel);
        add(content,BorderLayout.CENTER);

    }
    void setValueLabel(String value){
        valueLabel.setText(value);
        valueLabel.setForeground(new  Color(30,42,74));
    }
    void setLoading(){
        valueLabel.setText("Loading...");
        valueLabel.setForeground(Color.GRAY);

    }
    void setError(){
        valueLabel.setText("Error");
        valueLabel.setForeground(Color.RED);
    }

}
