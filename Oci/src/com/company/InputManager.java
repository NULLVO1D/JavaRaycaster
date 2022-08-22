package com.company;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class InputManager extends AbstractAction {
    public Screen window;
    public Action action;

    public InputManager(Screen window, Action action) {
        this.window = window;
        this.action = action;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (action == Action.FORWARD || action == Action.BACKWARD
                || action == Action.STOP_VERTICAL) {
            window.actionVertical = action;
        }
        if (action == Action.TURN_LEFT || action == Action.TURN_RIGHT
                || action == Action.STOP_TURNING) {
            window.actionHorizontol = action;
        }
    }
}


