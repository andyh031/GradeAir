// Basic console UI of GradeAir application
//TODO View weighting scheme
//TODO initialize weighting scheme junk, throwing exceptions

package ui;

import model.Course;
import model.MarkEntry;
import model.Weighting;
import model.Student;
import persistence.JsonReader;
import persistence.JsonWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Scanner;

import static java.util.Objects.isNull;

public class GradeAir {

    private static final DecimalFormat df = new DecimalFormat("0.00");

    private static final String ADD_MARK = "add mark";
    private static final String SORT = "sort";
    private static final String ALPHABETICAL = "alphabetical";
    private static final String GRADE = "grade";
    private static final String COURSE_NAME = "course name";
    private static final String SUBJECT = "subject";
    private static final String TEACHER = "teacher";
    private static final String ADD_COURSE = "add course";
    private static final String REMOVE_COURSE = "remove course";
    private static final String VIEW_COURSE = "view course";
    private static final String EDIT_ACCOUNT = "edit account";
    private static final String FIRST_NAME_FIELD = "first name";
    private static final String LAST_NAME_FIELD = "last name";
    private static final String VIEW_ACCOUNT = "view account";
    private static final String QUIT = "quit";
    private static final String SAVE = "save";
    private static final String LOAD = "load";

    private static final String JSON_STORE = "./data/student.json";
    private Scanner scanner;
    private Student student;
    private boolean runProgram;
    private JsonWriter jsonWriter;
    private JsonReader jsonReader;

    //EFFECTS: launch GradeAir
    public GradeAir() {
        scanner = new Scanner(System.in);
        runProgram = true;
        jsonWriter = new JsonWriter(JSON_STORE);
        jsonReader = new JsonReader(JSON_STORE);
    }

    // EFFECTS: login screen: if there is account data, asks if user would like to load it. If not, create new account.
    public void login() {
        System.out.println("Welcome to GradeAir\n");
        try {
            jsonReader.read();
            System.out.println("Looks like you already have an account. Would you like to load it? y or n?");
            String str = makePrettyUserInput(scanner.nextLine());
            if (str.equals("y")) {
                loadStudent();
                homepage();
            } else if (str.equals("n")) {
                createAccount();
            } else {
                login();
            }
        } catch (IOException e) {
            createAccount();
        }
    }

    //MODIFIES: Student
    //EFFECTS: create a new account on GradeAir
    public void createAccount() {
        System.out.println("Let's get started! Type in your information below:");
        System.out.print("First Name: ");
        String fname = scanner.nextLine();

        System.out.print("Last Name: ");
        String lname = scanner.nextLine();

        student = new Student(fname, lname);
        System.out.println("Thanks for signing up for GradeAir!");

        homepage();
    }

    //MODIFIES: Student
    //EFFECTS: creates the homepage
    public void homepage() {
        System.out.println("\n" + student.getFirstName() + " " + student.getLastName());
        System.out.println("Overall GPA: " + df.format(student.calculateGPA()) + "%");
        displayCourses();
        printOptions();

        while (runProgram) {
            if (scanner.hasNext()) {
                String command = scanner.nextLine();
                command = makePrettyUserInput(command);
                parseInput(command);
            }
        }
    }

    //MODIFIES: Student, Course
    //EFFECTS: create grading scheme for course
    public void initializeWeightings(Course course) {
        int totalWeightSoFar = 0;
        do {
            System.out.print("Category: ");
            String category = makePrettyUpperCase(scanner.nextLine());
            System.out.print("Weighting: ");
            int weighting = Integer.parseInt(scanner.nextLine());

            course.getWeightingScheme().add(new Weighting(category, weighting));
            totalWeightSoFar += course.getWeightingScheme().get(course.getWeightingScheme().size() - 1).getWeight();
        } while (!(totalWeightSoFar == 100));
    }

    //MODIFIES: Student
    //EFFECTS: Jump to whichever 'tab' user decides to go to
    @SuppressWarnings("methodlength")
    public void parseInput(String command) {
        switch (command) {
            case ADD_COURSE:
                parseAddCourse();
                homepage();
                break;
            case SAVE:
                saveStudent();
                homepage();
                break;
            case REMOVE_COURSE:
                System.out.println("What course would you like to remove?");
                String courseToRemove = makePrettyUpperCase(scanner.nextLine());
                student.removeCourseByName(courseToRemove);
                homepage();
            case SORT:
                sort();
                homepage();
            case VIEW_COURSE:
                viewCourses();
                homepage();
                break;
            case VIEW_ACCOUNT:
                viewUserFields();
                homepage();
                break;
            case QUIT:
                quit();
                break;
            default:
                System.out.println("Could not understand your input, please try again");
                break;
        }
    }

    //EFFECTS: quits the program, asking if the user would like to save before termination
    public void quit() {
        System.out.println("Would you like to save? y or n?");
        String cmd = scanner.nextLine();
        if (cmd.equals("y")) {
            saveStudent();
            runProgram = false;
        } else if (cmd.equals("n")) {
            runProgram = false;
        } else {
            quit();
        }
    }

    //MODIFIES: Student, Course
    //EFFECTS: add a course with a weighting scheme to student's courses
    public void parseAddCourse() {
        System.out.print("Enter the course name: ");
        String courseName = makePrettyUpperCase(scanner.nextLine());
        System.out.print("Enter the course subject: ");
        String subject = makePrettyUpperCase(scanner.nextLine());

        Course course = new Course(courseName, subject);
        System.out.println("Please assign a weighting scheme to the course (must add up to 100)");
        initializeWeightings(course);
        student.addCourse(course);
    }

    //MODIFIES: Student
    //EFFECTS: sorts courses according to what user wants
    public void sort() {
        System.out.println("How would you like to sort your courses?\n 'alphabetical' \n 'grade'");
        String command = makePrettyUserInput(scanner.nextLine());
        switch (command) {
            case ALPHABETICAL:
                student.sortCoursesAlphabetical();
                break;
            case GRADE:
                student.sortCoursesByGrade();
                break;
            default:
                System.out.println("Could not understand your input, please try again");
                sort();
                break;
        }
    }

    //REQUIRES: user inputs an existing course
    //EFFECTS: Let user view a course out of their list of courses
    public void viewCourses() {
        System.out.println("Which course would you like to view?");
        String command = makePrettyUserInput(scanner.nextLine());

        for (Course course : student.getCourses()) {
            if (command.equalsIgnoreCase(course.getCourseName())) {
                showClassInfo(course);
            }
        }
    }

    //MODIFIES: Course
    //EFFECTS: Show course details for one course
    public void showClassInfo(Course course) {
        System.out.println(course.getCourseName() + ": " + df.format(course.getCourseGrade()) + "%");
        System.out.println("Subject: " + course.getSubject());

        if (!isNull(course.getTeacher())) {
            System.out.println("Teacher: " + course.getTeacher());
        }

        displayMarks(course);

        System.out.println("To add a mark, type '" + ADD_MARK + "'");
        System.out.println("To edit course info, type 'edit course', or press enter to go back home");
        String command = scanner.nextLine();

        if (command.equals("edit course")) {
            printCourseOptions();
            editCourseInfo(course);
        } else if (command.equals(ADD_MARK)) {
            parseAddMark(course);
        }
    }

    //MODIFIES: Course
    //EFFECTS: add a mark to a course
    public void parseAddMark(Course course) {
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Mark from 0-100 (press enter if no mark yet): ");
        double mark = Double.parseDouble(scanner.nextLine());
        System.out.println("Type of mark (must be of a type that you initialized in your weighting scheme): ");
        String category = makePrettyUpperCase(scanner.nextLine());
        MarkEntry markEntry = new MarkEntry(name, mark, category);
        course.addMarkEntry(markEntry);
        showClassInfo(course);
    }

    //MODIFIES: Course
    //EFFECTS: Update course specific fields upon user input
    public void editCourseInfo(Course course) {
        String command = makePrettyUserInput(scanner.nextLine());
        switch (command) {
            case COURSE_NAME:
                parseCourseName(course);
                break;
            case SUBJECT:
                parseSubject(course);
                break;
            case TEACHER:
                parseTeacher(course);
                break;
            default:
                System.out.println("Could not understand input, please try again");
                editCourseInfo(course);
                break;
        }
    }

    //MODIFIES: Course
    //EFFECTS: change course name of a course
    public void parseCourseName(Course course) {
        System.out.print("Enter course name: ");
        String str = makePrettyUpperCase(scanner.nextLine());
        course.setCourseName(str);
        showClassInfo(course);
    }

    //MODIFIES: Course
    //EFFECTS: change subject name of a course
    public void parseSubject(Course course) {
        System.out.print("Enter subject name: ");
        String str = makePrettyUpperCase(scanner.nextLine());
        course.setSubject(str);
        showClassInfo(course);
    }

    //MODIFIES: Course
    //EFFECTS: change teacher name of a course
    public void parseTeacher(Course course) {
        System.out.print("Enter teacher name: ");
        String str = makePrettyUpperCase(scanner.nextLine());
        course.setTeacher(str);
        showClassInfo(course);
    }

    //EFFECTS: display mark for one course
    public void displayMarks(Course course) {
        System.out.println("Marks: ");
        for (Weighting weighting : course.getWeightingScheme()) {
            System.out.println(weighting.getCategory());
            if (weighting.getMarksList().size() == 0) {
                System.out.println("No " + weighting.getCategory() + " added yet");
            } else {
                System.out.println(weighting.retrieveMarksToPrint());
            }
        }
    }

    //EFFECTS: print every course
    public void displayCourses() {
        System.out.println("\nClasses:");

        if (student.getCourses().size() == 0) {
            System.out.println("You have no classes added");
        } else {
            for (Course course : student.getCourses()) {
                System.out.println(course.getCourseName() + ": " + df.format(course.getCourseGrade()) + "%");
            }
        }
    }

    //MODIFIES: Student
    //EFFECTS: Update user account fields
    public void updateUserFields() {
        System.out.println("What field would you like to update?\n 'first name' \n 'last name'");
        String command = makePrettyUserInput(scanner.nextLine());
        switch (command) {
            case FIRST_NAME_FIELD:
                System.out.print("Enter first name: ");
                String opt1 = scanner.nextLine();
                student.setFirstName(opt1);
                break;
            case LAST_NAME_FIELD:
                System.out.print("Enter last name: ");
                String opt2 = scanner.nextLine();
                student.setLastName(opt2);
                break;
            default:
                System.out.println("Could not understand your input, please try again");
                updateUserFields();
                break;
        }
    }

    //EFFECTS: show user account information and present option to edit account information
    public void viewUserFields() {
        System.out.println("\nHere is your account information");
        System.out.println("First Name: " + student.getFirstName());
        System.out.println("Last Name: " + student.getLastName());

        System.out.println("To edit this information, type '" + EDIT_ACCOUNT + "', or press enter to go back");
        if (scanner.nextLine().equals(EDIT_ACCOUNT)) {
            updateUserFields();
        }
    }

    //EFFECTS: show available user options for 'switching' tabs on homepage
    public void printOptions() {
        if (student.getCourses().size() != 0) {
            System.out.println("\nTo view any course more specifically, type '" + VIEW_COURSE + "'");
            System.out.println("To add a course, type '" + ADD_COURSE + "'");
            System.out.println("To remove a course, type '" + REMOVE_COURSE + "'");
            System.out.println("To sort courses, type '" + SORT + "'");
        } else {
            System.out.println("\nTo add a course, type '" + ADD_COURSE + "'");
        }
        System.out.println("To view your account, type '" + VIEW_ACCOUNT + "'");
        System.out.println("To save, type '" + SAVE + "'");
        System.out.println(("To quit the application, type '" + QUIT + "'"));
    }

    //EFFECTS: show available user inputs to edit a class
    public void printCourseOptions() {
        System.out.println("To change course name, type '" + COURSE_NAME + "'");
        System.out.println("To change the subject, type '" + SUBJECT + "'");
        System.out.println("To change teacher name, type '" + TEACHER + "'");
    }

    //EFFECTS: makes user input lowercase and no white space
    public String makePrettyUserInput(String str) {
        str = str.toLowerCase();
        str = str.trim();
        return str;
    }

    //EFFECTS: make user input all upper case and trimmed
    public String makePrettyUpperCase(String str) {
        str = str.toUpperCase();
        str = str.trim();
        return str;
    }

    //EFFECTS: end the program
    public void end() {
        System.out.println("Quitting...");
        scanner.close();
        runProgram = false;
    }

    //MODIFIES: this
    // EFFECTS: saves the student to file
    private void saveStudent() {
        try {
            jsonWriter.open();
            jsonWriter.write(student);
            jsonWriter.close();
            System.out.println("Saved " + student.getFirstName() + " " + student.getLastName() + " to " + JSON_STORE);
        } catch (FileNotFoundException e) {
            System.out.println("Unable to write to file: " + JSON_STORE);
        }
    }

    // MODIFIES: this
    // EFFECTS: loads student from file
    private void loadStudent() {
        try {
            student = jsonReader.read();
            System.out.println("Loaded "
                    + student.getFirstName() + " " + student.getLastName() + " from " + JSON_STORE);
        } catch (IOException e) {
            System.out.println("Unable to read from file: " + JSON_STORE);
        }
    }
}