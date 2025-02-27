package com.ursugoiangel;

import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
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
        // array to allow modification in static method
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

    private static void setUIFont(FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Chatbot");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(640, 480);
            frame.setLayout(new BorderLayout());

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
    
            // Menu setup
            JMenuBar menuBar = new JMenuBar();
            JMenu menu = new JMenu("File");
            JMenuItem menuItem = new JMenuItem("Exit");
            JMenuItem scaryMenuItem = new JMenuItem("Scary Mode");
            menu.add(scaryMenuItem);
            menu.add(menuItem);
            menuBar.add(menu);
            frame.setJMenuBar(menuBar);
            menuItem.addActionListener(e -> System.exit(0));
            scaryMenuItem.addActionListener(e -> {
                try {
                    // Load the image from resources folder using the class loader
                    URL imageUrl = Main.class.getClassLoader().getResource("cool_skeleton.jpg");
                    if (imageUrl == null) {
                        System.err.println("Could not find cool_skeleton.jpg in resources");
                        return;
                    }
                    
                    final Image backgroundImage = ImageIO.read(imageUrl);
                    // Create a new content pane with the background image
                    JPanel contentPane = new JPanel(new BorderLayout()) {
                        @Override 
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                        }
                    };
                    
                    // Keep the original layout and components
                    contentPane.add(scrollPane, BorderLayout.CENTER);
                    contentPane.add(inputPanel, BorderLayout.SOUTH);

                    contentPane.setOpaque(false);
                    chatPanel.setOpaque(false);
                    scrollPane.setOpaque(false);
                    scrollPane.getViewport().setOpaque(false);
                    
                    frame.setContentPane(contentPane);

                    // Set the font to a scary font
                    Font scaryFont = new Font("Papyrus", Font.PLAIN, 12);
                    
                    // Set default font for all Swing components
                    setUIFont(new FontUIResource(scaryFont));        
                    // Update existing components
                    SwingUtilities.updateComponentTreeUI(frame);

                    frame.setTitle("Sick as hell and super scary chatbot :skullemoji:");

                    frame.setAlwaysOnTop(true);
                    frame.setAutoRequestFocus(true);

                    // Play scary music
                    try {
                        URL audioUrl = Main.class.getClassLoader().getResource("toccata.wav");
                        if (audioUrl == null) {
                            System.err.println("Could not find toccata.wav in resources");
                        } else {
                            // Create an audio input stream from the URL
                            javax.sound.sampled.AudioInputStream audioIn = 
                                javax.sound.sampled.AudioSystem.getAudioInputStream(audioUrl);
                            
                            // Get a sound clip resource
                            javax.sound.sampled.Clip clip = 
                                javax.sound.sampled.AudioSystem.getClip();
                            
                            // Open audio clip and load samples from the audio input stream
                            clip.open(audioIn);
                            
                            // Start playing the clip (in a loop if desired)
                            clip.loop(javax.sound.sampled.Clip.LOOP_CONTINUOUSLY);
                        }
                    } catch (javax.sound.sampled.UnsupportedAudioFileException | 
                            LineUnavailableException | 
                            IOException audioEx) {
                        System.err.println("Error playing audio: " + audioEx.getMessage());
                        audioEx.printStackTrace();
                    }


                    frame.revalidate();
                    frame.repaint();
                    
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, 
                        "Could not load the scary image: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
    
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