package com.company;

import javax.swing.*;
import java.awt.*;

public class Screen extends JFrame {

    final Color BACK_COLOR = Color.BLACK;
    public Rect[] verLines;
    public int pixelWidth;
    int resX;
    int resY;
    ContentPane innerContent;

    Action actionVertical = Action.NONE;
    Action actionHorizontol = Action.NONE;

    public Screen(int resX, int resY, boolean debug) {
        super("Oci");

        this.resX = resX;
        this.resY = resY;

        pixelWidth = Mappy.DEFAULT_WIDTH / resX;
        getContentPane().setLayout(new BorderLayout());
        this.setBounds(0, 0, Mappy.DEFAULT_WIDTH, Mappy.DEFAULT_HEIGHT);

        innerContent = new ContentPane(resX);
        innerContent.setBackground(BACK_COLOR);
        innerContent.setVisible(true);
        this.setContentPane(innerContent);

        setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
    }

    public InputMap getInputMap(int whenInFocusedWindow) {
        return super.rootPane.getInputMap(whenInFocusedWindow);
    }

    public ActionMap getActionMap() {
        return super.rootPane.getActionMap();
    }

    public class ContentPane extends JPanel {

        int resX;

        public ContentPane(int resX) {
            verLines = new Rect[resX];
            this.resX = resX;

            definePixels();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            // draw pixels here


            for (int x = 0; x < this.resX; x++) {
                Rect line = verLines[x];
                //Draw "pixel" as rect
                g.setColor(line.color);
                g.fillRect(line.x, line.y, line.width, line.height);
            }


        }

        private void definePixels() {
            for (int x = 0; x < resX; x++) {
                verLines[x] = new Rect(x * pixelWidth, 0, pixelWidth, resY, BACK_COLOR);
            }
        }

        public void resetPixels() {
            for (int x = 0; x < resX; x++) {
                verLines[x].setTo(x * pixelWidth, 0, pixelWidth, resY, BACK_COLOR);
            }
        }
    }

}