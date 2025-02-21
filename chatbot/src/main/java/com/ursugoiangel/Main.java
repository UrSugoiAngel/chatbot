package com.ursugoiangel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Chatbot");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(640, 480);
            frame.setLayout(new BorderLayout());

            // Menu
            JMenuBar menuBar = new JMenuBar();
            JMenu menu = new JMenu("File");
            JMenuItem menuItem = new JMenuItem("Exit");
            menu.add(menuItem);
            menuBar.add(menu);
            frame.setJMenuBar(menuBar);
            menuItem.addActionListener(e -> System.exit(0));

            // Chat display area
            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            frame.add(scrollPane, BorderLayout.CENTER);

            // Input panel with textField and send button
            JPanel inputPanel = new JPanel(new BorderLayout());
            JTextField textField = new JTextField();
            JButton sendButton = new JButton("Send");
            inputPanel.add(textField, BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);
            frame.add(inputPanel, BorderLayout.SOUTH);

            frame.setVisible(true);

            textField.requestFocus();

            // BlockingQueue to synchronize user inputs
            BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();

            // When send button is pressed, add the textField's content to the queue
            sendButton.addActionListener(e -> {
                String inputText = textField.getText();
                if (!inputText.isBlank()) {
                    try {
                        textArea.append(inputText + "\n");
                        inputQueue.put(inputText);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    textField.setText("");
                }
            });

            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendButton.doClick();
                }
                return false;
            });

            // Supplier that waits for input from the BlockingQueue
            Supplier<String> inputSupplier = () -> {
                try {
                    String input = inputQueue.take();
                    return input;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    return "";
                }
            };

            // Consumer that appends output to the textArea on the EDT
            Consumer<String> outputConsumer = output -> SwingUtilities.invokeLater(() -> {
                textArea.append(output + "\n");
            });

            Chatbot chatbot = new Chatbot();

            // Run respond on a separate thread since it contains blocking calls.
            new Thread(() -> {
                String finalResponse = chatbot.respond(outputConsumer, inputSupplier);
                outputConsumer.accept("Final suggestion: " + finalResponse);
            }).start();
        });
    }
}