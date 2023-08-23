package com.bk.modelcreator;

import javafx.scene.control.TextArea;
import javafx.application.Platform;

public class ModifiedTextArea {
    private TextArea textArea;
    public ModifiedTextArea(TextArea textArea) {
        this.textArea = textArea;
    }
    public void appendText(String text) {
        Platform.runLater(() -> {
            textArea.appendText(text + System.lineSeparator());
        });
    }
}
