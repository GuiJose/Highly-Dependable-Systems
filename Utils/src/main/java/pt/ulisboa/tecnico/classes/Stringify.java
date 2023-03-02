package pt.ulisboa.tecnico.classes;

import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ClassState;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.Student;


import java.util.Comparator;
import java.util.stream.Collectors;

public class Stringify {

  public static String format(ClassState classState) {
    return String.format(
        "ClassState{\n\tcapacity=%d,\n\topenEnrollments=%s,\n\tenrolled=%s,\n\tdiscarded=%s\n}",
        classState.getCapacity(),
        classState.getOpenEnrollments(),
        classState.getEnrolledList().stream()
            .sorted(Comparator.comparing(Student::getStudentId))
            .map(Stringify::format)
            .collect(Collectors.toList()),
        classState.getDiscardedList().stream()
            .sorted(Comparator.comparing(Student::getStudentId))
            .map(Stringify::format)
            .collect(Collectors.toList()));
  }

  public static String format(Student student) {
    return String.format(
        "\n\t\tStudent{\n\t\t\tId='%s',\n\t\t\tName='%s'\n\t\t}",
        student.getStudentId(), student.getStudentName());
  }

  public static String format(ResponseCode responseCode) {
      if (responseCode == ResponseCode.OK) return "The action completed successfully.";
      if (responseCode == ResponseCode.NON_EXISTING_STUDENT) return "The student does not exist.";
      if (responseCode == ResponseCode.FULL_CLASS) return "The class has reached its maximum capacity.";
      if (responseCode == ResponseCode.STUDENT_ALREADY_ENROLLED) return "The student is already enrolled.";
      if (responseCode == ResponseCode.ENROLLMENTS_ALREADY_OPENED) return "Enrollments are already open.";
      if (responseCode == ResponseCode.ENROLLMENTS_ALREADY_CLOSED) return "Enrollments are already closed.";
      if (responseCode == ResponseCode.INACTIVE_SERVER) return "The server is down.";
      if (responseCode == ResponseCode.WRITING_NOT_SUPPORTED) return "The server you contacted does not support writes.";
      if (responseCode == ResponseCode.SERVER_NOT_RECENT_VERSION) return "The server you contacted does not have the most recent version, your request has been recorded.";
      if (responseCode == ResponseCode.SERVER_NOT_RECENT_VERSION_LIST) return "The server you contacted does not have the most recent version.";
      return"Unknown error.";
  }
}
