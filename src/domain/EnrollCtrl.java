package domain;

import java.util.List;
import java.util.Map;

import domain.exceptions.EnrollmentRulesViolationException;

public class EnrollCtrl{
public void enroll(Student s, List<CSE> courses) throws EnrollmentRulesViolationException{
    for(CSE o : courses){

        CoursePassCheck(s, o);
        CoursePrerequisiteCheck(s, o);
        examAndDuplicateCheck(courses, o);
    }

    CheckGPA(courses, s);

    for(CSE o : courses)
        s.takeCourse(o.getCourse(), o.getSection());
}

private void examAndDuplicateCheck(List<CSE> courses, CSE o) throws EnrollmentRulesViolationException{
    for(CSE o2 : courses){
        if(o == o2)
            continue;
        if(o.getExamTime().equals(o2.getExamTime()))
            throw new EnrollmentRulesViolationException(String.format("Two offerings %s and %s have the same exam time", o, o2));
        if(o.getCourse().equals(o2.getCourse()))
            throw new EnrollmentRulesViolationException(String.format("%s is requested to be taken twice", o.getCourse().getName()));
    }
}

private void CoursePrerequisiteCheck( Student s, CSE o) throws EnrollmentRulesViolationException{
    List<Course> prereqs = o.getCourse().getPrerequisites();
    nextPre:
    for(Course pre : prereqs){
        for(Map.Entry<Term, Map<Course, Double>> tr : s.getTranscript().entrySet()){
            for(Map.Entry<Course, Double> r : tr.getValue().entrySet()){
                if(r.getKey().equals(pre) && r.getValue() >= 10)
                    continue nextPre;
            }
        }
        throw new EnrollmentRulesViolationException(String.format("The student has not passed %s as a prerequisite of %s", pre.getName(), o.getCourse().getName()));
    }
}

private void CoursePassCheck( Student s, CSE o) throws EnrollmentRulesViolationException{
    for(Map.Entry<Term, Map<Course, Double>> tr : s.getTranscript().entrySet()){
        for(Map.Entry<Course, Double> r : tr.getValue().entrySet()){
            if(r.getKey().equals(o.getCourse()) && r.getValue() >= 10)
                throw new EnrollmentRulesViolationException(String.format("The student has already passed %s", o.getCourse().getName()));
        }
    }
}

private void CheckGPA(List<CSE> courses, Student s) throws EnrollmentRulesViolationException{

    if((s.getGpa() < 12 && getUnitsRequested(courses) > 14) ||
            (s.getGpa() < 16 && getUnitsRequested(courses) > 16) ||
            (getUnitsRequested(courses) > 20))
        throw new EnrollmentRulesViolationException(String.format("Number of units (%d) requested does not match GPA of %f", getUnitsRequested(courses), s.getGpa()));
}

private int getUnitsRequested(List<CSE> courses){
    int unitsRequested = 0;
    for(CSE o : courses)
        unitsRequested += o.getCourse().getUnits();
    return unitsRequested;
}

}
