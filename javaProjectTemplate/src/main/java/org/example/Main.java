//=======================================\\
//==========| IMPORT PACKAGES |==========\\
//=======================================\\
package org.example;

import java.sql.*;
import java.util.HashMap;
import javax.lang.model.type.UnknownTypeException;
import javax.swing.*;
import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;

//===================================\\
//==========| MY SOLUTION |==========\\
//===================================\\
class SQL {
    public static void loadCurrentEmployees() {
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/employeeManager")) {
            String SQL = "SELECT employees.id, employees.firstname, employees.lastname, employees.position, employees.age, employees.pay, employees.payyear, employees.creation, raiting.number FROM employees INNER JOIN raiting ON employees.id = raiting.id";
            PreparedStatement prepareStmnt = conn.prepareStatement(SQL);
            ResultSet response = prepareStmnt.executeQuery();
            while (response.next()) {
                HashMap<String, String> newEmployee = new HashMap<String, String>();
                newEmployee.put("firstName", response.getString("firstname"));
                newEmployee.put("lastName", response.getString("lastname"));
                newEmployee.put("position", response.getString("position"));
                newEmployee.put("age", response.getString("age"));
                newEmployee.put("pay", response.getString("pay"));
                newEmployee.put("yearPay", response.getString("payyear"));
                newEmployee.put("createdDate", response.getString("creation"));
                newEmployee.put("rate", response.getString("number"));
                Main.database.allEmployees.put(response.getString("id"), newEmployee);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void loadEmployeeReviews() {
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/employeeManager")) {
            String SQL = "SELECT id, context FROM reviews";
            PreparedStatement prepareStmnt = conn.prepareStatement(SQL);
            ResultSet response = prepareStmnt.executeQuery();
            while (response.next()) {
                PFS.log(response.getString("id"));
                PFS.log(response.getString("context"));
                Main.database.employeeReviews.put(response.getString("id"), response.getString("context"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void loadAllProjects() {
        for (String i : Main.database.allEmployees.keySet()) {
            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/employeeManager")) {
                String SQL = "SELECT id, title, description FROM projects WHERE id="+i;
                PreparedStatement prepareStmnt = conn.prepareStatement(SQL);
                ResultSet response = prepareStmnt.executeQuery();
                ArrayList<HashMap<String, String>> allProjects = new ArrayList<HashMap<String, String>>();
                while (response.next()) {
                    HashMap<String, String> newProject = new HashMap<String, String>();
                    newProject.put("title", response.getString("title"));
                    newProject.put("description", response.getString("description"));
                    allProjects.add(newProject);
                }
                Main.database.projects.put(i, allProjects);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static void loadEmployeePayouts() {
        for (String i : Main.database.allEmployees.keySet()) {
            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/employeeManager")) {
                String SQL = "SELECT id, amount, date FROM previouspay WHERE id="+i;
                PreparedStatement prepareStmnt = conn.prepareStatement(SQL);
                ResultSet response = prepareStmnt.executeQuery();
                ArrayList<HashMap<String, String>> allPayouts = new ArrayList<HashMap<String, String>>();
                while (response.next()) {
                    HashMap<String, String> newPayout = new HashMap<String, String>();
                    newPayout.put("id", response.getString("id"));
                    newPayout.put("amount", response.getString("amount"));
                    newPayout.put("date", response.getString("date"));
                    allPayouts.add(newPayout);
                }
                Main.database.payouts.put(i, allPayouts);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static void loadAllVaidPasswords() {
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/employeeManager")) {
            String SQL = "SELECT code FROM accesscodes";
            PreparedStatement prepareStmnt = conn.prepareStatement(SQL);
            ResultSet response = prepareStmnt.executeQuery();
            while (response.next()) {
                Main.database.allCodes.add(response.getString("code"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}

public class Main {
    public class database {
        public static ArrayList<String> allCodes = new ArrayList<String>();
        public static HashMap<String, HashMap<String, String>> allEmployees = new HashMap<String, HashMap<String, String>>();
        public static HashMap<String, String> employeeReviews = new HashMap<String, String>();
        public static HashMap<String, ArrayList<HashMap<String, String>>> projects = new HashMap<String, ArrayList<HashMap<String, String>>>();
        public static HashMap<String, ArrayList<HashMap<String, String>>> payouts = new HashMap<String, ArrayList<HashMap<String, String>>>();
        public static boolean firstRun = true;
    }

    public static void main(String[] args) {
        SQL.loadCurrentEmployees();
        SQL.loadAllProjects();
        SQL.loadEmployeeReviews();
        SQL.loadEmployeePayouts();
        SQL.loadAllVaidPasswords();
        Main.login();
    }

    public static void login() {
        boolean passCodeCheck = false;
        PFS.clearConsole();
        PFS.log("Thank you for using PFS Employee Manager. User authentication is required.");
        while (passCodeCheck == false) {
            String givenPassword = PFS.inputStr("Please confirm a password.");
            for (int i = 0; i < database.allCodes.size(); i++) {
                if (givenPassword.equals(database.allCodes.get(i))) {
                    Main.showOptions();
                    passCodeCheck = true;
                    return;

                }
            }
            PFS.log("Incorrect password!");
        }
    }

    public static void showOptions() {
        PFS.clearConsole();
        if (database.firstRun == true) {
            database.firstRun = false;
            PFS.log("Welcome to PFS user manager.");
        }
        String[] allOptions = { "A", "S", "R", "C", "P", "L", "E", "V", "I", "H" };
        String inputQuestion = "[A]-Add employee | [S]-Show all employees | [R]-Remove a Employee | [E]-Edit a Employee | [C]-Close window";
        String inputQuestionExpanded = "[V]-View employee review | [I]-View employee projects | [H]-View Pay History";
        String inputManagement = "[L]-Log out | [P]-Reset Password";
        String userInput = PFS.inputObj(inputQuestion + "\n" + inputQuestionExpanded + "\n" + inputManagement,
                allOptions);
        PFS.clearConsole();
        if (userInput == "A") {
            Main.addEmployee();
        } else if (userInput == "S") {
            Main.showEmployees();
        } else if (userInput == "C") {
            PFS.log("Ending...");
            PFS.inputClose();
        } else if (userInput == "R") {
            Main.removeEmployee();
        } else if (userInput == "P") {
            Main.addPassword();
        } else if (userInput == "L") {
            database.firstRun = true;
            PFS.log("Logging Out...");
            PFS.inputStr("PRESS [ENTER] KEY TO CONTINUE");
            Main.login();
        } else if (userInput == "E") {
            Main.editUser();
        } else if (userInput == "V") {
            Main.viewReview();
        } else if (userInput == "I") {
            Main.viewProjects();
        } else if (userInput == "H") {
            Main.viewPayHistory();
        }
    }

    public static void addEmployee() {
        PFS.log("=== Creating a new employee ===");
        HashMap<String, String> newEmployee = new HashMap<String, String>();

        String firstName = PFS.inputStr("What is their first name?");
        newEmployee.put("firstName", firstName);

        String lastName = PFS.inputStr("What is their last name?");
        newEmployee.put("lastName", lastName);

        String position = PFS.inputStr("What is their position in the company?");
        newEmployee.put("position", position);

        String age = PFS.inputStr("What is the their age?");
        newEmployee.put("age", age);

        String pay = PFS.inputStr("What is their pay per hour?");
        newEmployee.put("pay", pay);

        String yearPay = PFS.inputStr("What is their yearly pay?");
        newEmployee.put("yearPay", yearPay);

        LocalDate localDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");
        String createdDate = localDate.format(formatter);
        newEmployee.put("createdDate", createdDate);

        Random randomNumberCreator = new Random();
        String userId1 = Integer.toString(randomNumberCreator.nextInt(9));
        String userId2 = Integer.toString(randomNumberCreator.nextInt(9));
        String userId3 = Integer.toString(randomNumberCreator.nextInt(9));
        String userId4 = Integer.toString(randomNumberCreator.nextInt(9));
        String userId = userId1 + userId2 + userId3 + userId4;

        database.allEmployees.put(userId, newEmployee);
        PFS.log("Created new employee...");
        PFS.inputStr("PRESS [ENTER] KEY TO CONTINUE");
        Main.showOptions();
    }

    public static void showEmployees() {
        Utils.showAllEmployeeDetails();
        PFS.log("==========");
        PFS.inputStr("PRESS [ENTER] KEY TO CONTINUE");
        Main.showOptions();
    }

    public static void removeEmployee() {
        Utils.showAllEmployeeDetails();
        PFS.log("==========");
        String removeNumber = PFS.inputStr("Enter the employee number you want to remove. Enter 0 to cancel");
        if (removeNumber != "0") {
            if (database.allEmployees.containsKey(removeNumber)) {
                PFS.log("Removed employee...");
                database.allEmployees.remove(removeNumber);
            } else {
                PFS.log("There was no employee by that number");
            }
        } else {
            PFS.log("Canceled action...");
        }
        PFS.inputStr("PRESS [ENTER] KEY TO CONTINUE");
        Main.showOptions();
    }

    public static void addPassword() {
        boolean passCodeCheck = false;
        while (passCodeCheck == false) {
            String givenPassword = PFS.inputStr("Please confirm a current password");
            for (int i = 0; i < database.allCodes.size(); i++) {
                if (givenPassword.equals(database.allCodes.get(i))) {
                    String newPassword = PFS.inputStr("What is the password you would like to add?");
                    Main.database.allCodes.add(newPassword);
                    PFS.log("Your password has been added");
                    passCodeCheck = true;
                }
            }
            PFS.log("Incorrect password!");
        }
        PFS.inputStr("PRESS [ENTER] KEY TO CONTINUE");
        Main.showOptions();
    }

    public static void editUser() {
        Utils.showAllEmployeeDetails();
        PFS.log("==========");
        String employeeSelection = PFS.inputStr("Enter the employee number you would like to edit. Enter 0 to cancel");
        if (employeeSelection != "0") {
            if (database.allEmployees.containsKey(employeeSelection)) {
                HashMap<String, String> currentEmployee = database.allEmployees.get(employeeSelection);
                String[] allOptions = { "F", "L", "P", "Y", "A", "E" };
                String inputQuestion = "What would you like to edit about this employee?";
                String inputOptions = "[F]-First Name | [L]-Last Name | [P]-Hourly Pay | [Y]-Yearly Pay | [A]-Age | [E]-Employee Position";
                String userInput = PFS.inputObj(inputQuestion + "\n" + inputOptions, allOptions);
                if (userInput.equals("F")) {
                    String newFirstName = PFS.inputStr("What is the new first name?");
                    currentEmployee.replace("firstName", newFirstName);
                    database.allEmployees.replace(employeeSelection, currentEmployee);
                } else if (userInput.equals("L")) {
                    String newFirstName = PFS.inputStr("What is the new last name?");
                    currentEmployee.replace("lastName", newFirstName);
                    database.allEmployees.replace(employeeSelection, currentEmployee);
                } else if (userInput.equals("P")) {
                    String newFirstName = PFS.inputStr("What is the new hourly pay?");
                    currentEmployee.replace("pay", newFirstName);
                    database.allEmployees.replace(employeeSelection, currentEmployee);
                } else if (userInput.equals("Y")) {
                    String newFirstName = PFS.inputStr("What is the new yearly pay?");
                    currentEmployee.replace("yearPay", newFirstName);
                    database.allEmployees.replace(employeeSelection, currentEmployee);
                } else if (userInput.equals("A")) {
                    String newFirstName = PFS.inputStr("What is the new age?");
                    currentEmployee.replace("age", newFirstName);
                    database.allEmployees.replace(employeeSelection, currentEmployee);
                } else if (userInput.equals("E")) {
                    String newFirstName = PFS.inputStr("What is the new position?");
                    currentEmployee.replace("position", newFirstName);
                    database.allEmployees.replace(employeeSelection, currentEmployee);
                }
            } else {
                PFS.log("There was no employee by that number");
            }
        } else {
            PFS.log("Canceled action...");
        }
        PFS.inputStr("PRESS [ENTER] KEY TO CONTINUE");
        Main.showOptions();
    }

    public static void viewReview() {
        Utils.showAllEmployeeDetails();
        PFS.log("==========");
        String employeeSelection = PFS.inputStr("Enter the employee number you would like to edit. Enter 0 to cancel");
        if (employeeSelection != "0") {
            if (database.allEmployees.containsKey(employeeSelection)) {
                String review = database.employeeReviews.get(employeeSelection);
                if (review == null) {
                    PFS.log("===Employee Review===");
                    PFS.log("This employee has not been reviewed.");
                } else {
                    PFS.log("===Employee Review===");
                    PFS.log(review);
                }
            } else {
                PFS.log("There was no employee by that number");
            }
        } else {
            PFS.log("Canceled action...");
        }
        PFS.inputStr("PRESS [ENTER] KEY TO CONTINUE");
        Main.showOptions();
    }

    public static void viewProjects() {
        Utils.showAllEmployeeDetails();
        PFS.log("==========");
        String employeeSelection = PFS.inputStr("Enter the employee number you would like to view. Enter 0 to cancel");
        if (employeeSelection != "0") {
            if (database.allEmployees.containsKey(employeeSelection)) {
                ArrayList<HashMap<String, String>> fullList = Main.database.projects.get(employeeSelection);
                if (fullList != null) {
                    for (int i = 0; i < fullList.size(); i++) {
                        HashMap<String, String> curProject = fullList.get(i);
                        PFS.log("| " + curProject.get("title") + " |");
                        int numberInterval = i + 1;
                        PFS.log(numberInterval + ") " + curProject.get("description"));
                    }
                } else {
                    PFS.log("They do not have any tasks");
                }
            } else {
                PFS.log("There was no employee by that number");
            }
        } else {
            PFS.log("Canceled action...");
        }
        PFS.inputStr("PRESS [ENTER] KEY TO CONTINUE");
        Main.showOptions();
    }

    public static void viewPayHistory() {
        Utils.showAllEmployeeDetails();
        PFS.log("==========");
        String employeeSelection = PFS.inputStr("Enter the employee number you would view their pay history. Enter 0 to cancel");
        if (employeeSelection != "0") {
            if (database.allEmployees.containsKey(employeeSelection)) {
                ArrayList<HashMap<String, String>> fullList = Main.database.payouts.get(employeeSelection);
                if (fullList != null) {
                    for (int i = 0; i < fullList.size(); i++) {
                        HashMap<String, String> curProject = fullList.get(i);
                        PFS.log("Amount: $" + curProject.get("amount") + " | Date: " + curProject.get("date"));
                    }
                } else {
                    PFS.log("They have not been paid");
                }
            } else {
                PFS.log("There was no employee by that number");
            }
        } else {
            PFS.log("Canceled action...");
        }
        PFS.inputStr("PRESS [ENTER] KEY TO CONTINUE");
        Main.showOptions();
    }
}

class Utils {
    static void showAllEmployeeDetails() {
        for (String i : Main.database.allEmployees.keySet()) {
            PFS.log("==========");
            PFS.log("Employee Number: " + i);
            PFS.log("First Name: " + Main.database.allEmployees.get(i).get("firstName"));
            PFS.log("Last Name: " + Main.database.allEmployees.get(i).get("lastName"));
            PFS.log("Position: " + Main.database.allEmployees.get(i).get("position"));
            PFS.log("Age: " + Main.database.allEmployees.get(i).get("age"));
            PFS.log("Hourly Pay: $" + Main.database.allEmployees.get(i).get("pay"));
            PFS.log("Yearly Pay: $" + Main.database.allEmployees.get(i).get("yearPay"));
            PFS.log("Created Date: " + Main.database.allEmployees.get(i).get("createdDate"));
            PFS.log("Rate: " + Main.database.allEmployees.get(i).get("rate"));
        }
    }
}

// =========================================\\
// ==========| MY PREBUILT CLASS |==========\\
// =========================================\\
class PFS {
    // ==========| CONFIG |==========\\
    public class config {
        public static Scanner userInput = new Scanner(System.in);
        public static boolean debugMode = false;
        public static boolean shoOptions = false;
        public static boolean reShowQuestion = true;
        public static boolean showInvalidErrorCode = true;
    }

    // ==========| CLEAR CONSOLE |==========\\
    static void clearConsole() {
        for (int i = 0; 30 > i; i++) {
            System.out.println();
        }
    }

    // ==========| INPUT METHODS |==========\\
    static String inputStr(String question) {
        System.out.println(question);
        String userOutput = config.userInput.nextLine();
        return userOutput;
    }

    static String inputStrLower(String question) {
        System.out.println(question);
        String userOutput = config.userInput.nextLine();
        String returnAnswer = userOutput.toLowerCase();
        return returnAnswer;
    }

    static Float inputFloat(String question) {
        float returnAnswer = 0.00f;
        Boolean validateInput = false;
        while (validateInput.equals(false)) {
            try {
                System.out.println(question);
                String userOutput = config.userInput.nextLine();
                returnAnswer = Float.parseFloat(userOutput);
                validateInput = true;
            } catch (Exception e) {
                System.out.println("Please provide a number with a decimal");
            }
        }
        return returnAnswer;
    }

    static Integer inputInt(String question) {
        int returnAnswer = 0;
        Boolean validateInput = false;
        while (validateInput.equals(false)) {
            try {
                System.out.println(question);
                String userOutput = config.userInput.nextLine();
                returnAnswer = Integer.parseInt(userOutput);
                validateInput = true;
            } catch (Exception e) {
                System.out.println("Please provide a number.");
            }
        }
        return returnAnswer;
    }

    static String inputObj(String question, String[] options) {
        Boolean checkingValue = false;
        Boolean firstPass = true;
        String foundValue = "";
        while (checkingValue.equals(false)) {
            if (firstPass.equals(true)) {
                firstPass = false;
                System.out.println(question);
                if (config.shoOptions == true) {
                    System.out.println(Arrays.toString(options));
                }
            }
            String userOutput = config.userInput.nextLine();
            String returnAnswer = userOutput.toLowerCase();
            for (int i = 0; options.length > i; i++) {
                String lowerAnswer = options[i].toLowerCase();
                if (lowerAnswer.equals(returnAnswer)) {
                    foundValue = options[i];
                }
            }
            if (foundValue.equals("")) {
                if (config.showInvalidErrorCode == true) {
                    System.out.println("Invalid Option");
                }
                if (config.reShowQuestion == true) {
                    System.out.println(question);
                } else {
                    System.out.println("Not a valid option. Please choose from the given list.");
                    System.out.println(Arrays.toString(options));
                }
            } else {
                checkingValue = true;
            }
        }
        return foundValue;
    }

    static String inputArray(String question, ArrayList<String> options) {
        Boolean checkingValue = false;
        Boolean firstPass = true;
        String foundValue = "";
        while (checkingValue.equals(false)) {
            if (firstPass.equals(true)) {
                firstPass = false;
                System.out.println(question);
            }
            String userOutput = config.userInput.nextLine();
            String returnAnswer = userOutput.toLowerCase();
            for (int i = 0; i < options.size(); i++) {
                if (options.get(i).toLowerCase().equals(returnAnswer.toLowerCase())) {
                    foundValue = options.get(i);
                }
            }
            if (foundValue.equals("")) {
                if (config.showInvalidErrorCode == true) {
                    System.out.println("Invalid Option");
                }
                if (config.reShowQuestion == true) {
                    System.out.println(question);
                }
            } else {
                checkingValue = true;
            }
        }
        return foundValue;
    }

    static void inputClose() {
        config.userInput.close();
    }

    // ==========| FILE METHODS |==========\\
    static void createFile(String fileName) {
        try {
            File currentFile = new File(fileName + ".txt");
            if (currentFile.createNewFile()) {
                if (config.debugMode == true) {
                    System.out.println("File created: " + currentFile.getName());
                }
            } else {
                if (config.debugMode == true) {
                    System.out.println("File already exists.");
                }
            }
        } catch (IOException element) {
            if (config.debugMode == true) {
                System.out.println("An error occurred.");
            }
            element.printStackTrace();
        }
    }

    static void deleteFile(String fileName) {
        File currentFile = new File(fileName + ".txt");
        if (currentFile.delete()) {
            if (config.debugMode == true) {
                System.out.println("Deleted the file: " + currentFile.getName());
            }
        } else {
            if (config.debugMode == true) {
                System.out.println("Failed to delete the file.");
            }
        }
    }

    static void replaceFile(String fileName, String fileContent) {
        try {
            FileWriter myWriter = new FileWriter(fileName + ".txt");
            myWriter.write(fileContent);
            myWriter.close();
            if (config.debugMode == true) {
                System.out.println("Successfully wrote to the file.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    static void addFile(String fileName, String fileContent) {
        try {
            FileWriter fileSystem = new FileWriter(fileName + ".txt", true);
            BufferedWriter fileWright = new BufferedWriter(fileSystem);
            fileWright.write(fileContent);
            fileWright.newLine();
            fileWright.close();
            if (config.debugMode == true) {
                System.out.println("Successfully wrote to the file.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    static String readFile(String fileName) {
        String fileContent = "";
        try {
            File myObj = new File(fileName + ".txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (fileContent.equals("")) {
                    fileContent = data;
                } else {
                    fileContent = fileContent + "\n" + data;
                }
            }
            myReader.close();
        } catch (FileNotFoundException element) {
            System.out.println("An error occurred.");
            element.printStackTrace();
        }
        return fileContent;
    }

    static ArrayList<String> readFileObject(String fileName) {
        ArrayList<String> newObject = new ArrayList<String>();
        try {
            File myObj = new File(fileName + ".txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                newObject.add(data);
            }
            myReader.close();
        } catch (FileNotFoundException element) {
            if (config.debugMode == true) {
                System.out.println("An error occurred.");
            }
            element.printStackTrace();
        }
        return newObject;
    }

    // ==========| LOG METHODS |==========\\
    static Boolean log(String message) {
        System.out.println(message);
        return true;
    }

    static Boolean logNumber(int number) {
        System.out.println(number);
        return true;
    }

    static Boolean logObject(ArrayList<String> content) {
        System.out.println(content);
        return true;
    }

    static Boolean logFullList(ArrayList<String> content) {
        for (int i = 0; i < content.size(); i++) {
            System.out.println(content.get(i));
        }
        return true;
    }

    // ==========| FIND/SEARCH METHODS |==========\\
    static String findFullList(ArrayList<String> content, String listItem) {
        for (int i = 0; i < content.size(); i++) {
            if (listItem.toLowerCase().equals(content.get(i).toLowerCase())) {
                return content.get(i);
            }
        }
        return "";
    }
}