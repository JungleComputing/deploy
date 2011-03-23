package ibis.deploy.gui;

/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import ibis.deploy.gui.misc.Utils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;

/**
 * Component to be used as tabComponent; Contains a JLabel to show the text and
 * a JButton to close the tab it belongs to
 */
public class DetachableTabComponent extends JPanel {

    private static final long serialVersionUID = 1L;

    private final DetachableTab tab;
    private final JButton button;

    public DetachableTabComponent(final DetachableTab tab, Icon icon,
            String title) {

        // unset default FlowLayout' gaps
        super(new FlowLayout(FlowLayout.CENTER, 0, 2));
        this.tab = tab;
        setOpaque(false);

        add(new JLabel(icon));

        JLabel label = new JLabel(title);
        label.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));

        add(label);
        // tab button
        button = new TabButton();
        add(button);

        // add more space to the top of the component
        // setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    }

    private class TabButton extends JButton implements ActionListener {

        private static final long serialVersionUID = 1L;

        public TabButton() {
            Icon activeIcon = Utils.createImageIcon("images/detach-arrow.png", null);
            Icon rolloverIcon = Utils.createImageIcon("images/detach-arrow-inverted.png", null);

            setRolloverIcon(rolloverIcon);

            setIcon(activeIcon);
            setToolTipText("Detach this tab");
            // Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            // Make it transparent
            setBackground(Color.LIGHT_GRAY);
            setContentAreaFilled(false);
            // No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            // setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            // Making nice rollover effect
            // we use the same listener for all buttons
            // addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
            // Close the proper tab by clicking the button
            addActionListener(this);

            // addMouseListener(new MouseAdapter() {
            // public void mouseEntered(MouseEvent e) {
            // button.setIcon(rolloverIcon);
            // }
            //
            // public void mouseExited(MouseEvent e) {
            // button.setEnabled(false);
            // button.setIcon(activeIcon);
            // }
            // });
        }

        public void actionPerformed(ActionEvent e) {
            // button.setEnabled(false);
            // button.setIcon(activeIcon);
            tab.detach();
        }

        // we don't want to update UI for this button
        public void updateUI() {
        }
    }

}
