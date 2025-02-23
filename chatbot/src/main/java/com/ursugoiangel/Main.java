package com.ursugoiangel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Main {
    private static void smoothScrollTo(JScrollPane scrollPane, int targetValue, int durationMillis) {
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        int startValue = verticalBar.getValue();
        int distance = targetValue - startValue;
        int frames = 30;
        int delay = durationMillis / frames;
        Timer timer = new Timer(delay, null);
        final int[] count = {0};
        timer.addActionListener(e -> {
            count[0]++;
            float progress = (float) count[0] / frames;
            int newValue = startValue + Math.round(distance * progress);
            verticalBar.setValue(newValue);
            if (count[0] >= frames) {
                verticalBar.setValue(targetValue);
                timer.stop();
            }
        });
        timer.start();
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Chatbot");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(640, 480);
            frame.setLayout(new BorderLayout());
    
            // Menu setup
            JMenuBar menuBar = new JMenuBar();
            JMenu menu = new JMenu("File");
            JMenuItem menuItem = new JMenuItem("Exit");
            menu.add(menuItem);
            menuBar.add(menu);
            frame.setJMenuBar(menuBar);
            menuItem.addActionListener(e -> System.exit(0));
    
            // Chat display area using a scroll pane
            JPanel chatPanel = new JPanel();
            chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
            JScrollPane scrollPane = new JScrollPane(chatPanel);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setAutoscrolls(true);
            frame.add(scrollPane, BorderLayout.CENTER);
    
            // Input panel with text field and send button
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
    
            // When send button is pressed, add the text field’s content to the queue and update scroll
            sendButton.addActionListener(e -> {
                String inputText = textField.getText();
                if (!inputText.isBlank()) {
                    try {
                        Chatbubble chatbubble = new Chatbubble(inputText, true);
                        // Wrap chatbubble with extra empty border space for the shadow
                        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                        wrapper.setOpaque(false);
                        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
                        wrapper.add(chatbubble);
                        chatPanel.add(wrapper);
                        chatPanel.revalidate();
                        chatPanel.repaint();
                        // Use smooth scrolling instead of an abrupt jump
                        SwingUtilities.invokeLater(() -> 
                            smoothScrollTo(scrollPane, scrollPane.getVerticalScrollBar().getMaximum(), 500)
                        );
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
                    return inputQueue.take();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    return "";
                }
            };
    
            // Consumer that appends output to the chat panel and scrolls to the bottom
            Consumer<String> outputConsumer = output -> SwingUtilities.invokeLater(() -> {
                Chatbubble chatbubble = new Chatbubble(output, false);
                // Wrap chatbubble with extra empty border space for the shadow
                JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
                wrapper.setOpaque(false);
                wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
                wrapper.add(chatbubble);
                chatPanel.add(wrapper);
                chatPanel.revalidate();
                chatPanel.repaint();
                // Call smooth scrolling to animate scroll to the bottom
                SwingUtilities.invokeLater(() -> 
                    smoothScrollTo(scrollPane, scrollPane.getVerticalScrollBar().getMaximum(), 500)
                );
            });
    
            Chatbot chatbot = new Chatbot();
    
            // Run respond on a separate thread because it uses blocking calls.
            new Thread(() -> {
                String finalResponse = chatbot.respond(outputConsumer, inputSupplier);
                outputConsumer.accept("Final suggestion: " + finalResponse);
            }).start();
        });
    }
}