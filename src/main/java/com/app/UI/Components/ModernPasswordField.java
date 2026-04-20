package com.app.UI.Components;
import java.awt.*;

import static org.postgresql.jdbcurlresolver.PgPassParser.getPassword;

public class ModernPasswordField extends ModernFieldBase {

    public ModernPasswordField(String placeholder) {
        super(placeholder);
    }

    @Override
    protected String getTextContent() {
        return new String(getPassword());
    }

    @Override
    protected void paintPlaceholder(Graphics2D g2) {
        if (getPassword().length == 0) {
            g2.setColor(new Color(160, 160, 160));
            g2.setFont(getFont().deriveFont(Font.ITALIC));
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() + fm.getAscent()) / 2 - 2;
            g2.drawString(placeholder, PADDING_LR, y);
        }
    }
}
