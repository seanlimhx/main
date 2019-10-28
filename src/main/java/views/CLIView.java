package views;

import controllers.ConsoleInputController;
import models.data.IProject;
import models.data.Project;
import models.member.Member;
import models.task.Task;
import util.log.DukeLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class CLIView {
    private static final String HORILINE = "\t____________________________________________________________";
    private static final String INDENTATION = "\t";
    private static final String BORDER_CORNER = "+";
    private static final char HORI_BORDER_UNIT = '-';
    private static final String VERTI_BORDER_UNIT = "|";
    private static final int DEFAULT_HORI_BORDER_LENGTH = 60;


    private ConsoleInputController consoleInputController;

    public CLIView() {
        this.consoleInputController = new ConsoleInputController(this);
    }

    /**
     * Prints an indented and formatted message with a top and bottom border.
     * @param lines The lines to be printed in between the border.
     */
    public void consolePrint(String... lines) {
        System.out.println(HORILINE);
        for (String message : lines) {
            System.out.println(INDENTATION + message);
        }
        System.out.println(HORILINE);
    }

    /**
     * Returns a String array that contains input in table form.
     * @param toPrintAll ArrayList with each element fitting into one table, and each element consists of an
     *                   ArrayList of Strings containing the lines to be printed in a table
     * @return A String array that contains input in table form.
     */
    public String[] consolePrintTable(ArrayList<ArrayList<String>> toPrintAll) {
        ArrayList<String> tableContent = new ArrayList<>();
        for (ArrayList<String> toPrint : toPrintAll) {
            tableContent.add(consolePrintTableHoriBorder(DEFAULT_HORI_BORDER_LENGTH));
            boolean hasPrintedTableHeader = false;
            for (String s : toPrint) {
                if (s.length() <= DEFAULT_HORI_BORDER_LENGTH) {
                    String line = VERTI_BORDER_UNIT + s
                            + getRemainingSpaces(DEFAULT_HORI_BORDER_LENGTH - s.length())
                            + VERTI_BORDER_UNIT;
                    tableContent.add(line);
                } else {
                    String[] splitStrings = getArrayOfSplitStrings(s);
                    for (String s1 : splitStrings) {
                        String line = VERTI_BORDER_UNIT + s1
                                + getRemainingSpaces(DEFAULT_HORI_BORDER_LENGTH - s1.length())
                                + VERTI_BORDER_UNIT;
                        tableContent.add(line);
                    }
                }
                if (!hasPrintedTableHeader) {
                    tableContent.add(consolePrintTableHoriBorder(DEFAULT_HORI_BORDER_LENGTH));
                    hasPrintedTableHeader = true;
                }
            }
            tableContent.add(consolePrintTableHoriBorder(DEFAULT_HORI_BORDER_LENGTH));
        }

        return tableContent.toArray(new String[0]);
    }

    /**
     * Splits a long String into an array of smaller Strings to fit the table display.
     * indexOfStringSplitStart refers to the index of the first char of the split string.
     * indexOfStringSplitEnd refers to the index after the index of the last char of the split string.
     * @param toPrint String to be printed in table form.
     * @return array of Strings to be printed line by line to fit the table width requirement.
     */
    private String[] getArrayOfSplitStrings(String toPrint) {
        ArrayList<String> splitStrings = new ArrayList<>();
        int indexOfStringSplitStart = 0;
        int indexOfStringSplitEnd = DEFAULT_HORI_BORDER_LENGTH;
        boolean isLastLine = false;
        while (!isLastLine) {
            if (toPrint.substring(indexOfStringSplitStart, indexOfStringSplitEnd).contains(" ")) {
                while (toPrint.charAt(indexOfStringSplitEnd - 1) != ' ') {
                    indexOfStringSplitEnd--;
                }
                splitStrings.add(toPrint.substring(indexOfStringSplitStart, indexOfStringSplitEnd));
                indexOfStringSplitStart = indexOfStringSplitEnd;
                indexOfStringSplitEnd += DEFAULT_HORI_BORDER_LENGTH;

            } else {
                //if a single word without space is longer than defaultHoriBorderLength
                while (toPrint.charAt(indexOfStringSplitEnd - 1) != ' ') {
                    indexOfStringSplitEnd++;
                    if (indexOfStringSplitEnd == toPrint.length()) {
                        break;
                    }
                }
                int numOfLines = (indexOfStringSplitEnd - indexOfStringSplitStart) / (DEFAULT_HORI_BORDER_LENGTH - 1);
                for (int i = 1; i <= numOfLines; i++) {
                    String wordSegment = toPrint.substring(indexOfStringSplitStart,
                            indexOfStringSplitStart + DEFAULT_HORI_BORDER_LENGTH - 1) + "-";
                    splitStrings.add(wordSegment);
                    indexOfStringSplitStart += (DEFAULT_HORI_BORDER_LENGTH - 1);
                }
                indexOfStringSplitEnd = indexOfStringSplitStart + DEFAULT_HORI_BORDER_LENGTH;
            }
            if (indexOfStringSplitEnd >= toPrint.length()) {
                splitStrings.add(toPrint.substring(indexOfStringSplitStart));
                isLastLine = true;
            }
        }
        return splitStrings.toArray(new String[0]);
    }

    /**
     * Returns a String of the number of spaces needed to complete the table.
     * @param numOfRemainingSpaces number of spaces needed.
     * @return String containing indicated number of spaces.
     */
    private String getRemainingSpaces(int numOfRemainingSpaces) {
        if (numOfRemainingSpaces == 0) {
            return "";
        } else {
            char[] remainingSpaces = new char[numOfRemainingSpaces];
            for (int i = 0; i < numOfRemainingSpaces; i++) {
                remainingSpaces[i] = ' ';
            }
            return new String(remainingSpaces);
        }
    }

    /**
     * Returns an indented horizontal border of a defined length with border corners (length not inclusive of corners).
     * @param borderLength Length of border excluding corners.
     * @return A String containing an indented horizontal border of a defined length with border corners.
     */
    private String consolePrintTableHoriBorder(int borderLength) {
        char[] border = new char[borderLength];
        for (int i = 0; i < borderLength; i++) {
            border[i] = HORI_BORDER_UNIT;
        }
        String borderString = new String(border);
        return BORDER_CORNER + borderString + BORDER_CORNER;
    }

    /**
     * Method to call when View model is started.
     */
    public void start() {
        DukeLogger.logInfo(CLIView.class, "ArchDuke have started.");
        Scanner sc = new Scanner(System.in);

        consolePrint("Hello! I'm Duke", "What can I do for you?");

        //noinspection InfiniteLoopStatement
        while (true) {
            String command = sc.nextLine();
            consoleInputController.onCommandReceived(command);
        }
    }

    /**
     * Method to be called when user says bye to exit the program.
     */
    public void end() {
        consolePrint("Bye. Hope to see you again soon!");
        DukeLogger.logInfo(CLIView.class, "ArchDuke have stopped.");
        System.exit(0);
    }

    /**
     * Method called when users wishes to view all Projects that are currently created or stored.
     * @param allProjectsDetails ArrayList of details to be presented in each table, with each element as an ArrayList
     *                          containing each row in the table, returned to View model by the Controller from the
     *                          Repository
     */
    public void viewAllProjects(ArrayList<ArrayList<String>> allProjectsDetails) {
        if (allProjectsDetails.size() == 0) {
            consolePrint("You currently have no projects!");
        } else {
            System.out.println("Here are all the Projects you are managing:");

            consolePrint(consolePrintTable(allProjectsDetails));
        }
    }

    /**
     * Method called to inform the user that the project has exited.
     * @param projectName The name of the project exited.
     */
    public void exitProject(String projectName) {
        consolePrint("Exited project: " + projectName);
    }

    /**
     * Adds a member to the project.
     * @param projectToManage The project specified by the user.
     */
    public void addMember(IProject projectToManage, String memberDetails) {
        consolePrint("Added new member to: " + projectToManage.getDescription(), ""
            + "Member details: " + memberDetails);
    }

    /**
     * Edits the details of a member belonging to the project.
     * @param projectToManage The project specified by the user.
     * @param memberIndexNumber The member whose details are to be updated.
     * @param memberDetails The updated details of the member.
     */
    public void editMember(IProject projectToManage, int memberIndexNumber, String memberDetails) {
        projectToManage.editMember(memberIndexNumber, memberDetails);
        consolePrint("Updated member details with the index number " + memberIndexNumber);
    }

    /**
     * Adds a member to the project.
     * @param projectToManage The project specified by the user.
     * @param newTask A new task with details specified by the user.
     */
    public void addTask(IProject projectToManage, Task newTask) {
        projectToManage.addTask(newTask);
        consolePrint("Added new task to the list.");
    }

    /**
     * Deletes a task from the project.
     * @param projectToManage The project specified by the user.
     * @param taskIndexNumber The index of the task to be deleted.
     */
    public void removeTask(IProject projectToManage, int taskIndexNumber) {
        consolePrint("Removed " + projectToManage.getTask(taskIndexNumber).getTaskName());
        projectToManage.removeTask(taskIndexNumber);
    }

    /**
     * Shows the details of all the members in the project.
     * Can be updated later on to include more information (tasks etc).
     * @param projectToManage The project specified by the user.
     */
    public void viewAllMembers(Project projectToManage) {
        ArrayList<String> allMemberDetailsForTable = projectToManage.getMembers().getAllMemberDetailsForTable();
        String header = "Members of " + projectToManage.getDescription() + ":";
        allMemberDetailsForTable.add(0, header);
        DukeLogger.logDebug(CLIView.class, allMemberDetailsForTable.toString());
        ArrayList<ArrayList<String>> tablesToPrint = new ArrayList<>();
        tablesToPrint.add(allMemberDetailsForTable);
        consolePrint(consolePrintTable(tablesToPrint));
    }

    /**
     * Shows the details of all the task in the project.
     * @param projectToManage The project specified by the user.
     */
    public void viewAllTasks(IProject projectToManage) {
        HashMap<Task, ArrayList<Member>> tasksAndAssignedMembers = projectToManage.getTasksAndAssignedMembers();
        ArrayList<ArrayList<String>> tableToPrint = new ArrayList<>();
        ArrayList<String> allTaskDetailsForTable
                = projectToManage.getTasks().getAllTaskDetailsForTable(tasksAndAssignedMembers);
        allTaskDetailsForTable.add(0, "Tasks of " + projectToManage.getDescription() + ":");
        DukeLogger.logDebug(CLIView.class, allTaskDetailsForTable.toString());
        tableToPrint.add(allTaskDetailsForTable);
        consolePrint(consolePrintTable(tableToPrint));
    }

    /**
     * Shows a list of all tasks in the project, sorted based on certain criteria as chosen by the user.
     * @param projectToManage The project specified by the user.
     * @param sortCriteria Criteria to sort chosen by user.
     */
    public void viewSortedTasks(IProject projectToManage, String sortCriteria) {
        HashMap<Task, ArrayList<Member>> tasksAndAssignedMembers = projectToManage.getTasksAndAssignedMembers();
        ArrayList<String> allTaskDetails =
                projectToManage.getTasks().getAllSortedTaskDetails(tasksAndAssignedMembers,sortCriteria);

        DukeLogger.logDebug(CLIView.class, allTaskDetails.toString());
        consolePrint(allTaskDetails.toArray(new String[0]));
    }

    /**
     * Shows specific requirements of a task.
     * @param projectToManage The project specified by the user.
     * @param taskIndex Index of task to be viewed.
     */
    public void viewTaskRequirements(IProject projectToManage, int taskIndex) {
        ArrayList<String> taskRequirements = projectToManage.getTask(taskIndex).getTaskRequirements();
        DukeLogger.logDebug(CLIView.class,taskRequirements.toString());
        consolePrint(taskRequirements.toArray(new String[0]));
    }

    /**
     * Shows all members’ credits, their index number, name, and name of tasks completed.
     * @param projectToManage The project specified by the user.
     */
    public void viewCredits(IProject projectToManage) {
        ArrayList<String> allCredits = projectToManage.getCredits();
        DukeLogger.logDebug(CLIView.class, allCredits.toString());
        if (allCredits.isEmpty()) {
            allCredits.add(0, "There are no members in this project.");
        } else {
            allCredits.add(0, "Here are all the member credits: ");
        }
        consolePrint(allCredits.toArray(new String[0]));
    }

    /**
     * Updates task details based on index number when tasks are sorted by priority.
     * @param projectToManage The project specified by the user.
     * @param updatedTaskDetails The updated task details.
     * @param taskIndexNumber The index number of the task to be updated.
     */
    public void editTask(IProject projectToManage, String updatedTaskDetails, int taskIndexNumber) {
        projectToManage.editTask(taskIndexNumber, updatedTaskDetails);
        consolePrint("The task has been updated!");
    }

    /**
     * Updates task requirements based on index number when tasks are sorted by priority.
     * @param projectToManage The project specified by the user.
     * @param taskIndexNumber The index number of the task to be updated.
     * @param updatedTaskRequirements The updated task requirements.
     */
    public void editTaskRequirements(IProject projectToManage, int taskIndexNumber, String updatedTaskRequirements) {
        projectToManage.editTaskRequirements(taskIndexNumber, updatedTaskRequirements);
        consolePrint("The requirements of your specified task has been updated!");
    }

}
