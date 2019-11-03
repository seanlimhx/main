package controllers;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import models.member.IMember;
import models.member.Member;
import models.project.Project;
import models.reminder.IReminder;
import models.reminder.Reminder;
import models.task.ITask;
import models.task.Task;
import repositories.ProjectRepository;
import util.AssignmentViewHelper;
import util.CommandHelper;
import util.ParserHelper;
import util.ViewHelper;
import util.factories.MemberFactory;
import util.factories.ReminderFactory;
import util.factories.TaskFactory;
import util.json.JsonConverter;
import util.log.ArchDukeLogger;

public class ProjectInputController implements IController {
    private Scanner manageProjectInput;
    private ProjectRepository projectRepository;
    private MemberFactory memberFactory;
    private TaskFactory taskFactory;
    private boolean isManagingAProject;
    private ViewHelper viewHelper;
    private CommandHelper commandHelper;
    private JsonConverter jsonConverter = new JsonConverter();

    /**
     * Constructor for ProjectInputController takes in a View model and a ProjectRepository.
     * ProjectInputController is responsible for handling user input when user chooses to manage a project.
     * @param projectRepository The object holding all projects.
     */
    public ProjectInputController(ProjectRepository projectRepository) {
        this.manageProjectInput = new Scanner(System.in);
        this.projectRepository = projectRepository;
        this.memberFactory = new MemberFactory();
        this.taskFactory = new TaskFactory();
        this.isManagingAProject = true;
        this.viewHelper = new ViewHelper();
        this.commandHelper = new CommandHelper();
    }

    /**
     * Allows the user to manage the project by branching into the project of their choice.
     * @param input User input containing project index number (to add to project class).
     */
    @Override
    public String[] onCommandReceived(String input) {
        ArchDukeLogger.logInfo(ProjectInputController.class.getName(), "[onCommandReceived] User input: '"
                + input + "'");
        int projectNumber;
        try {
            projectNumber = Integer.parseInt(input);
        } catch (NumberFormatException err) {
            isManagingAProject = false;
            return new String[] {"Input is not a number! Please input a proper project index!"};
        }
        Project projectToManage = projectRepository.getItem(projectNumber);
        isManagingAProject = true;
        return manageProject(projectToManage);
    }

    /**
     * Manages the project.
     * @param projectToManage The project specified by the user.
     * @return Boolean variable giving status of whether the exit command is entered.
     */
    private String[] manageProject(Project projectToManage) {
        ArchDukeLogger.logDebug(ProjectInputController.class.getName(), "[manageProject]");
        String[] responseToView = {"Please enter a command."};
        if (manageProjectInput.hasNextLine()) {
            String projectFullCommand = manageProjectInput.nextLine();
            ArchDukeLogger.logInfo(ProjectInputController.class.getName(), "Managing:"
                    + projectToManage.getName() + ",input:'"
                    + projectFullCommand + "'");
            if (projectFullCommand.matches("exit")) {
                isManagingAProject = false;
                responseToView = projectExit(projectToManage);
            } else if (projectFullCommand.matches("add member.*")) {
                responseToView =  projectAddMember(projectToManage, projectFullCommand);
            } else if (projectFullCommand.matches("edit member.*")) {
                responseToView = projectEditMember(projectToManage, projectFullCommand);
            } else if (projectFullCommand.matches("delete member.*")) {
                responseToView = projectDeleteMember(projectToManage, projectFullCommand);
            } else if (projectFullCommand.matches("view members.*")) {
                responseToView = projectViewMembers(projectToManage);
            } else if (projectFullCommand.matches("role.*")) {
                responseToView = projectRoleMembers(projectToManage, projectFullCommand);
            } else if (projectFullCommand.matches("view credits.*")) {
                responseToView = projectViewCredits(projectToManage);
            } else if (projectFullCommand.matches("add task.*")) {
                responseToView = projectAddTask(projectToManage, projectFullCommand);
            } else if (projectFullCommand.matches("view tasks.*")) {
                responseToView = projectViewTasks(projectToManage, projectFullCommand);
            } else if (projectFullCommand.matches("view assignments.*")) {
                responseToView = projectViewAssignments(projectToManage, projectFullCommand);
            } else if (projectFullCommand.matches("view task requirements.*")) { // need to refactor this
                responseToView = projectViewTaskRequirements(projectToManage, projectFullCommand);
            } else if (projectFullCommand.matches("edit task requirements.*")) {
                responseToView = projectEditTaskRequirements(projectToManage, projectFullCommand);
            } else if (projectFullCommand.matches("edit task.*")) {
                responseToView = projectEditTask(projectToManage, projectFullCommand);
            } else if (projectFullCommand.matches("delete task.*")) {
                responseToView = projectDeleteTask(projectToManage, projectFullCommand);
            } else if (projectFullCommand.matches("assign task.*")) {
                responseToView = projectAssignTask(projectToManage, projectFullCommand);
            } else if (projectFullCommand.matches("add reminder.*")) {
                responseToView = projectAddReminder(projectToManage,projectFullCommand);
            } else if (projectFullCommand.matches("help")) {
                responseToView = projectHelp();
            } else if (projectFullCommand.matches("bye")) {
                return end();
            } else {
                return new String[] {"Invalid command. Try again!"};
            }
        }
        jsonConverter.saveProject(projectToManage);
        return responseToView;
    }

    private String[] projectHelp() {
        ArrayList<ArrayList<String>> toPrintAll = new ArrayList<>();
        toPrintAll.add(commandHelper.getCommandsForProject());
        return viewHelper.consolePrintTable(toPrintAll);
    }

    /**
     * Adds roles to Members in a Project.
     * @param projectToManage : The project specified by the user.
     * @param projectCommand : User input.
     */
    public String[] projectRoleMembers(Project projectToManage, String projectCommand) {
        ArchDukeLogger.logDebug(ProjectInputController.class.getName(), "[projectRoleMembers] User input: '"
                + projectCommand + "'");
        String parsedCommands = projectCommand.substring(5);
        String[] commandOptions = parsedCommands.split(" -n ");
        if (commandOptions.length != 2) {
            return new String[] {"Wrong command format! Please enter role INDEX -n ROLE_NAME"};
        }
        int memberIndex = Integer.parseInt(commandOptions[0]);
        IMember selectedMember = projectToManage.getMembers().getMember(memberIndex);
        selectedMember.setRole(commandOptions[1]);
        return new String[] {"Successfully changed the role of " + selectedMember.getName() + " to "
                                + selectedMember.getRole() + "."};
    }

    /**
     * Adds a member to the current project.
     * @param projectToManage The project specified by the user.
     * @param projectCommand The user input.
     */
    public String[] projectAddMember(Project projectToManage, String projectCommand) {
        ArchDukeLogger.logDebug(ProjectInputController.class.getName(), "[projectToManage] User input: '"
                + projectCommand + "'");
        if (projectCommand.length() < 11) {
            return new String[] {"Add member command minimum usage must be \"add member -n NAME\"!",
                                 "Please refer to user guide for additional details."};
        }
        String memberDetails = projectCommand.substring(11);
        int numberOfCurrentMembers = projectToManage.getNumOfMembers();
        memberDetails = memberDetails + " -x " + numberOfCurrentMembers;
        //try to create member
        IMember newMember = memberFactory.create(memberDetails);
        if (newMember.getName() != null) {
            if (projectToManage.memberExists(newMember)) {
                return new String[] {"The member you have tried to add already exists!",
                    "Member name: " + newMember.getName(),
                    "Please ensure that each member has a different name."};
            } else {
                projectToManage.addMember((Member) newMember);
                return new String[]
                    {"Added new member to: " + projectToManage.getName(),
                    "Member details " + newMember.getDetails()};
            }
        } else {
            return new String[] {newMember.getDetails()};
        }
    }

    /**
     * Updates the details of a given member in the current project.
     * @param projectToManage The project specified by the user.
     * @param projectCommand The user input.
     */
    public String[] projectEditMember(Project projectToManage, String projectCommand) {
        ArchDukeLogger.logDebug(ProjectInputController.class.getName(), "[projectEditMember] User input: '"
                + projectCommand + "'");
        try {
            int memberIndexNumber = Integer.parseInt(projectCommand.substring(12).split(" ")[0]);
            if (projectToManage.getNumOfMembers() >= memberIndexNumber && memberIndexNumber > 0) {
                String updatedMemberDetails = projectCommand.substring(projectCommand.indexOf("-"));
                projectToManage.editMember(memberIndexNumber,updatedMemberDetails);
                return new String[] { "Updated member details with the index number " + memberIndexNumber};
            } else {
                return new String[] {"The member index entered is invalid."};
            }
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            ArchDukeLogger.logError(ProjectInputController.class.getName(), "[projectEditMember] "
                    + "Please enter the updated member details format correctly.");
            return new String[] {"Please enter the updated member details format correctly."};
        }
    }

    /**
     * Deletes a member from the current project.
     * @param projectToManage The project specified by the user.
     * @param projectCommand The user input.
     */
    public String[] projectDeleteMember(Project projectToManage, String projectCommand) {
        ArchDukeLogger.logDebug(ProjectInputController.class.getName(), "[projectDeleteMember] User input: '"
                + projectCommand + "'");
        if (projectCommand.length() <= 14) {
            return new String[] {"Can't delete members: No member index numbers detected!",
                "Please enter them as space-separated integers."};
        }
        ArrayList<String> outputMessages = new ArrayList<>();
        ParserHelper parserHelper = new ParserHelper();
        ArrayList<Integer> validMemberIndexes = parserHelper.parseMembersIndexes(projectCommand.substring(14),
            projectToManage.getNumOfMembers());
        outputMessages.addAll(parserHelper.getErrorMessages());
        if (validMemberIndexes.isEmpty()) {
            outputMessages.add("No valid member indexes. Cannot delete members.");
            return outputMessages.toArray(new String[0]);
        }
        Collections.sort(validMemberIndexes);
        Collections.reverse(validMemberIndexes);
        for (Integer index : validMemberIndexes) {
            Member memberToRemove = projectToManage.getMember(index);
            outputMessages.add("Removed member " + index + ": " + memberToRemove.getDetails());
            projectToManage.removeMember(memberToRemove);
        }
        if (!validMemberIndexes.isEmpty()) {
            outputMessages.add("Take note that the member indexes might have changed after deleting!");
        }
        //Shift this logger statement into ParserHelper to detect anytime there is incorrect input
        /*
        try {
        } catch (IndexOutOfBoundsException e) {
            ArchDukeLogger.logError(ProjectInputController.class.getName(), "[projectEditMember] "
                    + "Please enter the index number of the member to be deleted correctly.");
            return new String[] {"Please enter the index number of the member to be deleted correctly."};
        }*/
        return outputMessages.toArray(new String[0]);
    }

    /**
     * Displays all the members in the current project.
     * Can be updated later on to include more information (tasks etc).
     * @param projectToManage The project specified by the user.
     */
    public String[] projectViewMembers(Project projectToManage) {
        ArchDukeLogger.logDebug(ProjectInputController.class.getName(), "[projectViewMembers]");
        ArrayList<String> allMemberDetailsForTable = projectToManage.getMembers().getAllMemberDetailsForTable();
        String header = "Members of " + projectToManage.getName() + ":";
        allMemberDetailsForTable.add(0, header);
        ArchDukeLogger.logDebug(ProjectInputController.class.getName(), allMemberDetailsForTable.toString());
        ArrayList<ArrayList<String>> tablesToPrint = new ArrayList<>();
        tablesToPrint.add(allMemberDetailsForTable);
        return viewHelper.consolePrintTable(tablesToPrint);
    }

    /**
     * Displays the members’ credits, their index number, name, and name of tasks completed.
     * @param projectToManage The project specified by the user.
     */
    public String[] projectViewCredits(Project projectToManage) {
        ArchDukeLogger.logDebug(ProjectInputController.class.getName(), "[projectViewCredits]");
        ArrayList<String> allCredits = projectToManage.getCredits();
        ArchDukeLogger.logDebug(ProjectInputController.class.getName(), "allCredits: " + allCredits.toString());
        if (allCredits.isEmpty()) {
            allCredits.add(0, "There are no members in this project.");
        } else {
            allCredits.add(0, "Here are all the member credits: ");
        }
        return allCredits.toArray(new String[0]);
    }


    /**
     * Adds a task to the current project.
     * @param projectToManage The project specified by the user.
     * @param projectCommand The user input.
     */
    public String[] projectAddTask(Project projectToManage, String projectCommand) {
        ArchDukeLogger.logDebug(ProjectInputController.class.getName(), "[projectAddTask] User input: '"
                + projectCommand + "'");
        try {
            ITask newTask = taskFactory.createTask(projectCommand.substring(9));
            if (newTask.getDetails() != null) {
                if (projectToManage.taskExists(newTask)) {
                    return new String[]{"The task you are trying to add already exists!",
                        "Task name: " + newTask.getTaskName(),
                        "Please ensure that each task has a different task name."};
                }
                projectToManage.addTask((Task) newTask);
                return new String[] {"Added new task to the list."};
            }
            return new String[] {"Failed to create new task. Please ensure all "
                        + "necessary parameters are given"};

        } catch (NumberFormatException | ParseException e) {
            ArchDukeLogger.logError(ProjectInputController.class.getName(), "[projectAddTask] "
                    + "Please enter your task format correctly.");
            return new String[] {"Please enter your task format correctly."};
        }
    }

    /**
     * Updates the task details of a given task in the project.
     * @param projectToManage The project specified by the user.
     * @param projectCommand The user input.
     */
    public String[] projectEditTask(Project projectToManage, String projectCommand) {
        ArchDukeLogger.logDebug(ProjectInputController.class.getName(), "[projectEditTask] User input: '"
                + projectCommand + "'");
        try {
            if (projectCommand.length() <= 10) {
                return new String[]
                {"No parameters detected. Please enter details in the following format:",
                 "TASK_INDEX [-t TASK_NAME] [-p TASK_PRIORITY] [-d TASK_DUEDATE] [-c TASK_CREDIT] [-s STATE]"};
            }
            int taskIndexNumber = Integer.parseInt(projectCommand.substring(10).split(" ")[0]);
            String updatedTaskDetails = projectCommand.substring(projectCommand.indexOf("-"));

            if (projectToManage.getNumOfTasks() >= taskIndexNumber && taskIndexNumber > 0) {
                projectToManage.editTask(taskIndexNumber, updatedTaskDetails);
                return new String[] { "The task has been updated!" };
            }
            return new String[] {"The task index entered is invalid."};

        } catch (NumberFormatException e) {
            ArchDukeLogger.logError(ProjectInputController.class.getName(), "[projectEditTask] "
                    + "Please enter your task format correctly.");
            return new String[] {"Please enter your task format correctly."};
        }
    }

    /**
     * Deletes a task from the project.
     * @param projectToManage The project specified by the user.
     * @param projectCommand The user input.
     */
    public String[] projectDeleteTask(Project projectToManage, String projectCommand) {
        ArchDukeLogger.logDebug(ProjectInputController.class.getName(), "[projectDeleteTask] User input: '"
                + projectCommand + "'");
        if (projectCommand.length() <= 12) {
            return new String[] {"No task number detected! Please enter the task index number."};
        }
        ArrayList<String> outputMessages = new ArrayList<>();
        ParserHelper parserHelper = new ParserHelper();
        ArrayList<Integer> validTaskIndexes = parserHelper.parseTasksIndexes(projectCommand.substring(12),
            projectToManage.getNumOfTasks());
        outputMessages.addAll(parserHelper.getErrorMessages());
        // Sort to ensure task indexes work in the correct way
        Collections.sort(validTaskIndexes);
        Collections.reverse(validTaskIndexes);
        for (Integer index: validTaskIndexes) {
            outputMessages.add("Removed task " + index + ": " + projectToManage.getTaskIndexName(index));
            projectToManage.removeTask(index);
        }
        if (!validTaskIndexes.isEmpty()) {
            outputMessages.add("\t * Take note that index numbers of other tasks may have changed after deleting!");
        }
        return outputMessages.toArray(new String[0]);
    }

    /**
     * Updates the task requirements of a given task in the project.
     * @param projectToManage The project specified by the user.
     * @param projectCommand The user input.
     */
    public String[] projectEditTaskRequirements(Project projectToManage, String projectCommand) {
        ArchDukeLogger.logDebug(ProjectInputController.class.getName(),
                "[projectEditTaskRequirements] User input: '" + projectCommand + "'");
        if (projectCommand.length() <= 23) {
            return new String[] {"Task index is missing! Please input index of task to be edited!"};
        }
        try {
            int taskIndexNumber = Integer.parseInt(projectCommand.substring(23).trim().split(" ")[0]);
            if (projectToManage.getNumOfTasks() >= taskIndexNumber && taskIndexNumber > 0) {
                if (!projectCommand.contains("-")) {
                    return new String[] {"No flags are found! Please use flags such as '-r' or '-rm' to indicate "
                            + "the new requirements to be added or removed! Refer to the user guide for more help!"};
                } else {
                    String updatedTaskRequirements = projectCommand.substring(projectCommand.indexOf("-"));
                    return projectToManage.editTaskRequirements(taskIndexNumber,updatedTaskRequirements);
                }
            }
            return new String[] {"The task index entered is invalid."};
        } catch (NumberFormatException e) {
            ArchDukeLogger.logError(ProjectInputController.class.getName(), "[projectEditTaskRequirements] "
                    + "Task index is missing! Please input a proper task index!");
            return new String[] {"Task index is invalid! Please input a proper task index!"};
        }
    }

    /**
     * Displays the tasks in the current project.
     * @param projectToManage The project specified by the user.
     * @param projectCommand The user input.
     */
    public String[] projectViewTaskRequirements(Project projectToManage, String projectCommand) {
        ArchDukeLogger.logDebug(ProjectInputController.class.getName(),
                "[projectViewTaskRequirements] User input: '" + projectCommand + "'");
        if (projectCommand.length() < 23) {
            return new String[] {"Please indicate the index of the task to be viewed."};
        } else {
            try {
                int taskIndex = Integer.parseInt(projectCommand.substring(23));
                if (projectToManage.getNumOfTasks() >= taskIndex && taskIndex > 0) {
                    if (projectToManage.getTask(taskIndex).getNumOfTaskRequirements() == 0) {
                        return new String[] {"This task has no specific requirements."};
                    } else {
                        ArrayList<String> taskRequirements = projectToManage.getTask(taskIndex).getTaskRequirements();
                        return taskRequirements.toArray(new String[0]);
                    }
                }
                return new String[] {"The task index entered is invalid."};
            } catch (NumberFormatException e) {
                ArchDukeLogger.logError(ProjectInputController.class.getName(),
                        "[projectAssignTask] Input is not a number! " + "Please input a proper task index!");
                return new String[] {"Input is not a number! Please input a proper task index!"};
            }
        }
    }

    /**
     * Manages the assignment to and removal of tasks from members.
     * @param projectToManage The project specified by the user.
     * @param projectCommand The user input.
     */
    public String[] projectAssignTask(Project projectToManage, String projectCommand) {
        ArchDukeLogger.logDebug(ProjectInputController.class.getName(), "[projectAssignTask] User input: '"
                + projectCommand + "'");
        AssignmentController assignmentController = new AssignmentController(projectToManage);
        assignmentController.assignAndUnassign(projectCommand);
        ArrayList<String> errorMessages = assignmentController.getErrorMessages();
        ArrayList<String> successMessages = assignmentController.getSuccessMessages();
        errorMessages.addAll(successMessages);
        if (errorMessages.isEmpty()) {
            return new String[]{"No valid assignment input detected! Please refer to the user guide for help."};
        }
        return errorMessages.toArray(new String[0]);
    }

    /**
     * Displays list of assignments according to specifications of user.
     * @param projectToManage The project to manage.
     * @param projectCommand The full command by the user.
     */
    public String[] projectViewAssignments(Project projectToManage, String projectCommand) {
        ArchDukeLogger.logDebug(ProjectInputController.class.getName(),
                "[projectViewAssignments] User input: '" + projectCommand + "'");
        String errorMessageInsufficientParams = "Please input the parameters to view assignments:";
        String errorMessageNoSymbol = "**\t-m for viewing by member, -t for viewing by task.";
        String errorMessageNoSuffix = "**\t\"all\" to view all assignments,"
            + "or enter selected task/member index numbers.";
        String errorMessageGuide = "You may refer to the user guide or enter \"help\""
            + "for the list of possible commands.";

        if (projectCommand.length() <= 18) {
            return (new String[] {errorMessageInsufficientParams, errorMessageNoSuffix, errorMessageNoSymbol,
                errorMessageGuide});
        } else {
            String input = projectCommand.substring(17);
            if (input.charAt(0) == '-' && input.charAt(1) == 'm') {
                return projectViewMembersAssignments(projectToManage,
                        projectCommand.substring(20));
            } else if (input.charAt(0) == '-' && input.charAt(1) == 't') {
                return projectViewTasksAssignments(projectToManage,
                        projectCommand.substring(20));
            } else {
                return (new String[]
                {"Could not understand your command! Please use:", errorMessageNoSymbol});
            }
        }
    }

    /**
     * Displays all the tasks in the given project.
     * @param projectToManage The project specified by the user.
     * @param projectCommand The user input.
     */
    public String[] projectViewTasks(Project projectToManage, String projectCommand) {
        ArchDukeLogger.logDebug(ProjectInputController.class.getName(), "[projectViewTasks] User input: '"
                + projectCommand + "'");
        try {
            if (("view tasks").equals(projectCommand)) {
                HashMap<Task, ArrayList<Member>> tasksAndAssignedMembers = projectToManage.getTasksAndAssignedMembers();
                ArrayList<ArrayList<String>> tableToPrint = new ArrayList<>();
                ArrayList<String> allTaskDetailsForTable
                        = projectToManage.getTasks().getAllTaskDetailsForTable(tasksAndAssignedMembers, "/PRIORITY");
                allTaskDetailsForTable.add(0, "Tasks of " + projectToManage.getName() + ":");
                ArchDukeLogger.logDebug(ProjectInputController.class.getName(), allTaskDetailsForTable.toString());
                tableToPrint.add(allTaskDetailsForTable);
                return viewHelper.consolePrintTable(tableToPrint);
            } else if (projectCommand.length() >= 11) {
                String sortCriteria = projectCommand.substring(11);
                HashMap<Task, ArrayList<Member>> tasksAndAssignedMembers = projectToManage.getTasksAndAssignedMembers();
                ArrayList<ArrayList<String>> tableToPrint = new ArrayList<>();
                ArrayList<String> allTaskDetailsForTable =
                        projectToManage.getTasks().getAllTaskDetailsForTable(tasksAndAssignedMembers, sortCriteria);
                ArchDukeLogger.logDebug(ProjectInputController.class.getName(), allTaskDetailsForTable.toString());
                allTaskDetailsForTable.add(0, "Tasks of " + projectToManage.getName() + ":");
                tableToPrint.add(allTaskDetailsForTable);
                return viewHelper.consolePrintTable(tableToPrint);
            }
        } catch (IndexOutOfBoundsException e) {
            ArchDukeLogger.logError(ProjectInputController.class.getName(), "[projectAssignTask] "
                    + "Currently there are no tasks with the specified attribute.");
            return (new String[] {"Currently there are no tasks with the specified attribute."});
        }
        return null;
    }

    /**
     * Prints a list of members' individual list of tasks.
     * @param projectToManage the project being managed.
     * @param projectCommand The command by the user containing index numbers of the members to view.
     */
    public String[] projectViewMembersAssignments(Project projectToManage, String projectCommand) {
        ArchDukeLogger.logDebug(ProjectInputController.class.getName(),
                "[projectViewMembersAssignments] User input: '" + projectCommand + "'");
        ParserHelper parserHelper = new ParserHelper();
        ArrayList<Integer> validMembers = parserHelper.parseMembersIndexes(projectCommand,
            projectToManage.getNumOfMembers());
        if (!parserHelper.getErrorMessages().isEmpty()) {
            return parserHelper.getErrorMessages().toArray(new String[0]);
        }
        return AssignmentViewHelper.getMemberOutput(validMembers,
            projectToManage).toArray(new String[0]);
    }

    /**
     * Prints a list of tasks and the members assigned to them.
     * @param projectToManage The project to manage.
     * @param projectCommand The user input.
     */
    private String[] projectViewTasksAssignments(Project projectToManage, String projectCommand) {
        ArchDukeLogger.logDebug(ProjectInputController.class.getName(),
                "[projectViewTasksAssignments] User input: '" + projectCommand + "'");
        ParserHelper parserHelper = new ParserHelper();
        ArrayList<Integer> validTasks = parserHelper.parseTasksIndexes(projectCommand,
            projectToManage.getNumOfTasks());
        if (!parserHelper.getErrorMessages().isEmpty()) {
            return parserHelper.getErrorMessages().toArray(new String[0]);
        }
        return AssignmentViewHelper.getTaskOutput(validTasks,
            projectToManage).toArray(new String[0]);
    }

    /**
     * Exits the current project.
     * @param projectToManage The project specified by the user.
     * @return Boolean variable specifying the exit status.
     */
    public String[] projectExit(Project projectToManage) {
        ArchDukeLogger.logDebug(ProjectInputController.class.getName(), "[projectExit]");
        return new String[] {"Exited project: " + projectToManage.getName()};
    }

    public boolean getIsManagingAProject() {
        return isManagingAProject;
    }

    public String[] end() {
        ArchDukeLogger.logDebug(ProjectInputController.class.getName(), "[end]");
        return new String[] {"Bye. Hope to see you again soon!"};
    }


    /**
     * Add reminder to the default list list of tasks and the members assigned to them.
     * @param projectToManage The project to manage.
     * @param projectCommand The user input.
     */
    private String [] projectAddReminder(Project projectToManage, String projectCommand) {
        ArchDukeLogger.logDebug(ProjectInputController.class.getName(),
                "[projectAddReminder] User input: '" + projectCommand + "'");
        try {
            ReminderFactory reminderFactory = new ReminderFactory();
            IReminder newReminder = reminderFactory.createReminder(projectCommand.substring(13));
            if (newReminder.getReminderName() != null) {
                projectToManage.addReminderToList((Reminder) newReminder);
                return new String[] {"Added new reminder to the Reminder List in project."};
            }
            return new String[] {"Failed to create new task. Please ensure all "
                    + "necessary parameters are given"};

        } catch (NumberFormatException | ParseException e) {
            ArchDukeLogger.logError(ProjectInputController.class.getName(), "[projectAddReminder] "
                    + "Please enter your reminder date format correctly.");
            return new String[] {"Please enter your reminder date format correctly."};
        }
    }
}
