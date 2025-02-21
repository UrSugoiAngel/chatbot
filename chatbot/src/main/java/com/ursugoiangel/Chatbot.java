package com.ursugoiangel;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Chatbot {
    Chatbot() {

    }

    private String parseInput(String input, String[] options) {
        String keyword = "";
        String[] words = input.split(" ");
        for (String word : words) {
            for (String option : options) {
                if (word.equalsIgnoreCase(option)) {
                    keyword = option;
                    break;
                }
            }
        }
        return keyword;
    }

    public String respond(Consumer<String> consumer, Supplier<String> input_s) {
        String[] options = {"yes", "no"};
        int[] answers = new int[5];

        for (int questionNumber = 0; questionNumber < 5; questionNumber++) {
            boolean valid = false;
            while (!valid) {
                String input = null;
                String keyword;
                switch (questionNumber) {
                    case 0:
                        // First question
                        consumer.accept("Do you have any prior knowledge of unix/linux?");
                        input = input_s.get();
                        keyword = parseInput(input, options);
                        if (keyword.equals("yes")) {
                            answers[0] = 1;
                            valid = true;
                        } else if (keyword.equals("no")) {
                            answers[0] = 0;
                            valid = true;
                        } else {
                            consumer.accept("Invalid input. Please try again.");
                        }
                        break;
                    case 1:
                        // Second question
                        consumer.accept("Do you want to use it for school, work, or personal use?");
                        input = input_s.get();
                        String[] uses = {"school", "work", "personal"};
                        keyword = parseInput(input, uses);
                        if (keyword.equals("school")) {
                            answers[1] = 0;
                            valid = true;
                        } else if (keyword.equals("work")) {
                            answers[1] = 1;
                            valid = true;
                        } else if (keyword.equals("personal")) {
                            answers[1] = 2;
                            valid = true;
                        } else {
                            consumer.accept("Invalid input. Please try again.");
                        }
                        break;
                    case 2:
                        // Third question
                        consumer.accept("Do you want it to look like your current operating system?");
                        input = input_s.get();
                        keyword = parseInput(input, options);
                        if (keyword.equals("yes")) {
                            String[] os = {"windows", "mac", "macos"};
                            consumer.accept("Which operating system do you currently use?");
                            input = input_s.get();
                            keyword = parseInput(input, os);
                            if (keyword.equals("windows")) {
                                answers[2] = 0;
                                valid = true;
                            } else if (keyword.equals("mac") || keyword.equals("macos")) {
                                answers[2] = 1;
                                valid = true;
                            } else {
                                consumer.accept("Invalid input. Please try again.");
                            }
                        } else if (keyword.equals("no")) {
                            answers[2] = 2;
                            valid = true;
                        } else {
                            consumer.accept("Invalid input. Please try again.");
                        }
                        break;
                    case 3:
                        // Fourth question
                        consumer.accept("Do you want it to be customizable, possibly at the expense of ease of use?");
                        input = input_s.get();
                        keyword = parseInput(input, options);
                        if (keyword.equals("yes")) {
                            answers[3] = 1;
                            valid = true;
                        } else if (keyword.equals("no")) {
                            answers[3] = 0;
                            valid = true;
                        } else {
                            consumer.accept("Invalid input. Please try again.");
                        }
                        break;
                    case 4:
                        // Fifth question
                        consumer.accept("Is your pc 64-bit?");
                        input = input_s.get();
                        keyword = parseInput(input, options);
                        if (keyword.equals("yes")) {
                            answers[4] = 1;
                            valid = true;
                        } else if (keyword.equals("no")) {
                            answers[4] = 0;
                            valid = true;
                        } else {
                            consumer.accept("Invalid input. Please try again.");
                        }
                        break;
                    default:
                        consumer.accept("Invalid input. Please try again.");
                        break;
                }
            }
        }

        String response = "";
        // With prior knowledge
        if (answers[0] == 1) {
            // personal use
            if (answers[1] == 2) {
                // doesnt want an OS similar to current one
                if (answers[2] == 2) {
                    // customizability
                    if (answers[3] == 1) {
                        // 64-bit
                        if (answers[4] == 1) {
                            response = "You should try Arch Linux!";
                        } else {
                            response = "You should try Gentoo!";
                        }
                    } else {
                        response = "You should try Linux Mint!";
                    }
                } else if(answers[2] == 0) {
                    response = "You should try Zorin OS!";
                } else {
                    if(answers[3] == 1) {
                        response = "You should try Fedora!";
                    } else {
                        response = "You should try Linux Mint!";
                    }
                }
            } else if (answers[1] == 0) {
                // school
                if (answers[2] == 0) {
                    // customizability
                    if (answers[3] == 1) {
                        // 64-bit
                        if (answers[4] == 1) {
                            response = "You should try Fedora!";
                        } else {
                            response = "You should try CentOS!";
                        }
                    } else {
                        response = "You should try Debian!";
                    }
                } else {
                    response = "You should try openSUSE!";
                }
            } else if (answers[1] == 1) {
                // work
                if (answers[2] == 0) {
                    // customizability
                    if (answers[3] == 1) {
                        // 64-bit
                        if (answers[4] == 1) {
                            response = "You should try Fedora!";
                        } else {
                            response = "You should try CentOS!";
                        }
                    } else {
                        response = "You should try Debian!";
                    }
                } else {
                    response = "You should try openSUSE!";
                }
            }
        } else {
            // No prior knowledge (beginner-friendly recommendations)
            if (answers[1] == 2) { // Personal use
                if (answers[2] != 2) {
                    if(answers[2] == 0) {
                        response = "You should try Zorin OS!";
                    } else {
                        response = "You should try Elementary OS!";
                    }
                } else {
                    response = "You should try Linux Mint!";
                }
            } else if (answers[1] == 0) { // School
                response = "You should try Ubuntu LTS!";
            } else if (answers[1] == 1) { // Work
                response = "You should try Ubuntu LTS!";
            }
        }

        return response;
    }
}