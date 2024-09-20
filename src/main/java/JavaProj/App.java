package JavaProj;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {

    // Class for storing each question and the corresponding answer
    static class AnswerRecord {
        String question;
        String Answer;

        public AnswerRecord(String question, String Answer) {
            this.question = question;
            this.Answer = Answer;
        }
    }

    public static void main(String[] args) {
        String jsonFilePath = "Questions.json";

        ObjectMapper objectMapper = new ObjectMapper();

        // Store answers to output later into file
        List<AnswerRecord> Answers = new ArrayList<>();

        // Counters for political groups
        int democratCount = 0;
        int republicanCount = 0;
        int libertarianCount = 0;
        int communistCount = 0;

        try {
            JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));

            JsonNode questionsNode = rootNode.get("questions");

            @SuppressWarnings("resource")
            Scanner scanner = new Scanner(System.in);

            for (int i = 0; i < questionsNode.size(); i++) {
                JsonNode questionNode = questionsNode.get(i);

                // Print the question
                String questionText = questionNode.get("question").asText();
                System.out.println(questionText);

                // Print the answers
                JsonNode answersNode = questionNode.get("answers");
                System.out.println("A: " + answersNode.get("A").asText());
                System.out.println("B: " + answersNode.get("B").asText());
                System.out.println("C: " + answersNode.get("C").asText());
                System.out.println("D: " + answersNode.get("D").asText());

                System.out.print("Your answer (A/B/C/D): ");
                String userAnswer = scanner.nextLine().trim().toUpperCase();

                // Validate input
                while (!userAnswer.matches("[A-D]")) {
                    System.out.print("Invalid answer. Please enter A, B, C, or D: ");
                    userAnswer = scanner.nextLine().trim().toUpperCase();
                }

                Answers.add(new AnswerRecord(questionText, answersNode.get(userAnswer).asText()));

                // Increment political group with each answer
                switch (userAnswer) {
                    case "A":
                        democratCount++;
                        break;
                    case "B":
                        republicanCount++;
                        break;
                    case "C":
                        libertarianCount++;
                        break;
                    case "D":
                        communistCount++;
                        break;
                }

                /* 
                    In statistics, an 80% or above threshold is enough to prove a fact, if after 75% of the 
                    questions you have a score of 80% in any political group, this is enough evidence to 
                    your undying allegiance to a political party, no need to continue, we can make a 
                    prediction.
                */ 
                if ((i + 1) / (float)questionsNode.size() >= 0.75) {
                    int totalAnswers = i + 1;
                    double democratPercentage = (double) democratCount / totalAnswers * 100;
                    double republicanPercentage = (double) republicanCount / totalAnswers * 100;
                    double libertarianPercentage = (double) libertarianCount / totalAnswers * 100;
                    double communistPercentage = (double) communistCount / totalAnswers * 100;
                
                    String prediction = null;
                
                    if (democratPercentage >= 80) {
                        prediction = "Democrat";
                    } else if (republicanPercentage >= 80) {
                        prediction = "Republican";
                    } else if (libertarianPercentage >= 80) {
                        prediction = "Libertarian";
                    } else if (communistPercentage >= 80) {
                        prediction = "Communist";
                    }
                
                    if (prediction != null) {
                        // Spacing from the questions and answers
                        System.out.println();

                        System.out.println("With a score in a political party at or over 80%, a prediction can be made: You are a " + prediction);
                
                        saveAnswersToJson(Answers, democratPercentage, republicanPercentage, libertarianPercentage, communistPercentage);
                        return;
                    }
                }
                // Adding a space
                System.out.println();
            }

            scanner.close();

            // Calculate the percentage for each political group after all questions
            int totalQuestions = Answers.size();
            double democratPercentage = (double) democratCount / totalQuestions * 100;
            double republicanPercentage = (double) republicanCount / totalQuestions * 100;
            double libertarianPercentage = (double) libertarianCount / totalQuestions * 100;
            double communistPercentage = (double) communistCount / totalQuestions * 100;

            System.out.println("Here's the percentage of your political affiliation based on your answers: ");
            System.out.printf("Democrat: %.2f%%\n", democratPercentage);
            System.out.printf("Republican: %.2f%%\n", republicanPercentage);
            System.out.printf("Libertarian: %.2f%%\n", libertarianPercentage);
            System.out.printf("Communist: %.2f%%\n", communistPercentage);

            saveAnswersToJson(Answers, democratPercentage, republicanPercentage, libertarianPercentage, communistPercentage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to save the user's answers and percentages to a JSON file
    private static void saveAnswersToJson(List<AnswerRecord> userAnswers, double democratPercentage, double republicanPercentage, double libertarianPercentage, double communistPercentage) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = objectMapper.createObjectNode();
        int i = 1;

        for (AnswerRecord answerRecord : userAnswers) {
            ObjectNode answerNode = rootNode.putObject("Question " + i);
            answerNode.put("question", answerRecord.question);
            answerNode.put("Answer", answerRecord.Answer);
            i++;
        }

        // Add percentages to the JSON file
        ObjectNode percentagesNode = rootNode.putObject("Percentages");
        percentagesNode.put("Democrat", democratPercentage);
        percentagesNode.put("Republican", republicanPercentage);
        percentagesNode.put("Libertarian", libertarianPercentage);
        percentagesNode.put("Communist", communistPercentage);

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("Answers.json"), rootNode);
            System.out.println("User answers and percentages saved to Answers.json");
            // Another space for distinction in CLI
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
