package com.company;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class Main {

    public static void main(String[] args) {
        Screen window = new Screen(Mappy.DEFAULT_WIDTH, Mappy.DEFAULT_HEIGHT, true);

        createKeyBindings(window);

        Player player = new Player();

        while (true) {
            player.camera.drawFrameOn(window);
        }

    }

    private static void createKeyBindings(Screen s) {

        InputMap im = (s.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW));
        ActionMap am = s.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, false), Action.FORWARD);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false), Action.BACKWARD);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false), Action.TURN_LEFT);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, false), Action.TURN_RIGHT);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, true), Action.STOP_VERTICAL);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, true), Action.STOP_VERTICAL);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, true), Action.STOP_TURNING);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, true), Action.STOP_TURNING);

        am.put(Action.FORWARD, new InputManager(s, Action.FORWARD));
        am.put(Action.BACKWARD, new InputManager(s, Action.BACKWARD));
        am.put(Action.TURN_LEFT, new InputManager(s, Action.TURN_LEFT));
        am.put(Action.TURN_RIGHT, new InputManager(s, Action.TURN_RIGHT));
        am.put(Action.STOP_VERTICAL, new InputManager(s, Action.STOP_VERTICAL));
        am.put(Action.STOP_TURNING, new InputManager(s, Action.STOP_TURNING));
    }


}
