package pt.ulisboa.tecnico.classes.classserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import pt.ulisboa.tecnico.classes.Timestamp;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ClassState;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.Student;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ClassState.Builder;


public class Class {
    private static final String ABRIR_INSCRICOES_CMD = "openEnrollments";
	private static final String FECHAR_INSCRICOES_CMD = "closeEnrollments";
	private static final String CANCELAR_INSCRICAO_CMD = "cancelEnrollment";
    private static final String INSCREVER_CMD = "enroll";

    private ConcurrentHashMap<String, String> students = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> cancelStudents = new ConcurrentHashMap<>();
    private boolean open = false;
    private boolean active = true;
    private boolean flag;
    private boolean gossip = true;
    private int capacity;
    private Timestamp replicaTimestamp = new Timestamp();
    private Timestamp valueTimestamp = new Timestamp();
    private HashMap<Timestamp, String> log = new LinkedHashMap<>();
    private HashMap<Timestamp, String> pendingCommands = new LinkedHashMap<>();
    private List<String> reviewingStudents = new ArrayList<>();
    private boolean firstStudentAfterGossip = true;

    public synchronized void setFlag(boolean flag){
        this.flag = flag;
    }

    public synchronized void setPosition(int position) {
        this.replicaTimestamp.setPosition(position);
    }

    public synchronized void updatePosition() {
        this.replicaTimestamp.updatePosition();
    }

    public synchronized void updateOtherPosition() {
        int otherPosition = replicaTimestamp.getPosition() == 0 ? 1 : 0;
        this.replicaTimestamp.updateOtherPosition(otherPosition);
    }

    public synchronized void clearLog() {
        this.log.clear();
    }

    public synchronized void clearReviewingStudents() {
        this.reviewingStudents.clear();
    }

    public synchronized void updateTimestamp() {
        this.valueTimestamp.updateTimestamp(replicaTimestamp.getTimestamp());
    }

    public synchronized void updateTimestampByPrev(List<Integer> prev) {
        this.replicaTimestamp.updateTimestamp(prev);
    }

    public synchronized List<Integer> getTimestamp() {
        return this.replicaTimestamp.getTimestamp();
    }

    public synchronized HashMap<Timestamp, String> getLog() {
        return this.log;
    }

    public synchronized boolean lessOrEqual(List<Integer> other) {
        return this.valueTimestamp.lessOrEqual(other);
    }

    public synchronized void addLog(Timestamp timestamp, String command) {
        log.put(timestamp, command);
    }

    public synchronized Timestamp updatePrevTimestamp(List<Integer> prev) {
        updatePosition();
        int position = this.replicaTimestamp.getPosition(); 
        int value = this.replicaTimestamp.getTimestamp().get(this.replicaTimestamp.getPosition());

        prev.set(position, value);
        Timestamp timestamp = new Timestamp(prev);
        return timestamp;
    }

    public synchronized boolean haveGossip() {
        return this.gossip;
    }

    public synchronized ResponseCode activateGossip(){
        if(!active) return ResponseCode.INACTIVE_SERVER;
        this.gossip = true;
        return ResponseCode.OK;
    }

    public synchronized ResponseCode deactivateGossip(){
        if(!active) return ResponseCode.INACTIVE_SERVER;
        this.gossip = false;
        return ResponseCode.OK;
    }

    public synchronized ResponseCode isActive(){
        return !active? ResponseCode.INACTIVE_SERVER : ResponseCode.OK;
    }

    public synchronized ResponseCode setActive(){
        this.active = true;
        return ResponseCode.OK;
    }

    public synchronized void setFirstAfterGossip(){
        this.firstStudentAfterGossip = true;
    }

    public synchronized ResponseCode setDeactive(){
        this.active = false;
        return ResponseCode.OK;
    }

    public synchronized ResponseCode openEnrollments(int capacity, boolean gossip) {
        if(flag) System.err.println("INF: Professor tries to open enrollments");
        if(!(active || gossip)) return ResponseCode.INACTIVE_SERVER;
        if(open){
            if(flag) System.err.println("ERROR: Enrollments already opened!");
            return ResponseCode.ENROLLMENTS_ALREADY_OPENED;
        }
        if(students.size() >= capacity){
            if(flag) System.err.println("ERROR: Class full!");
            return ResponseCode.FULL_CLASS;
        }
        this.capacity = capacity;
        this.open = true;
        if(flag) System.err.println("INF: Enrollments opened with success!");
        return ResponseCode.OK;
    }

    public synchronized ResponseCode closeEnrollments(boolean gossip) {
        if(flag) System.err.println("INF: Professor tries to open enrollments");
        if(!(active || gossip)) return ResponseCode.INACTIVE_SERVER;
        if(!open){
            if(flag) System.err.println("ERROR: Enrollments already closed!");
            return ResponseCode.ENROLLMENTS_ALREADY_CLOSED;
        }
        this.open = false;
        if(flag) System.err.println("INF: Enrollments closed with success!");
        return ResponseCode.OK;
    }

    public synchronized ResponseCode enroll(String id, String name, boolean gossip){
        if(flag) System.err.println("INF: Student tries to enroll");
        if(!(active || gossip)) return ResponseCode.INACTIVE_SERVER;
        if(!open){
            if(flag) System.err.println("ERROR: Enrollments already closed!");
            return ResponseCode.ENROLLMENTS_ALREADY_CLOSED;
        }
        if(students.containsKey(id)){
            if(flag) System.err.println("ERROR: Student already enrolled!");
            return ResponseCode.STUDENT_ALREADY_ENROLLED;
        }
        if(students.size() == capacity){
            if(gossip) {
                String max = id;
                for(String studentId : reviewingStudents) {
                    if(studentId.compareTo(max) > 0) {
                        max = studentId;
                    }
                }
                if(!max.equals(id)) {
                    cancelEnroll(max, gossip);
                }
                else {
                    cancelStudents.put(id, name);
                    return ResponseCode.FULL_CLASS;
                }
            }
            else {
                if(flag) System.err.println("ERROR: Class full!");
                return ResponseCode.FULL_CLASS;
            }
        }
        if(cancelStudents.containsKey(id)) cancelStudents.remove(id);
        reviewingStudents.add(id);
        students.put(id,name);
        if(firstStudentAfterGossip && !gossip){
            reviewingStudents = reviewingStudents.subList(reviewingStudents.indexOf(id), reviewingStudents.size());
            firstStudentAfterGossip = false;
        }
        if(flag) System.err.println("INF: Student enrolled with success!");
        return ResponseCode.OK;

    }

    public synchronized ClassState lists(boolean propagate){
        if(flag && propagate) System.err.println("INF: Propagating state");
        if(flag && !propagate) System.err.println("INF: Student|Admin|Professor tries to list enrollments stats");
        Builder list = ClassState.newBuilder().setCapacity(capacity).setOpenEnrollments(open);
        for(Map.Entry<String, String> entry : students.entrySet()){
            Student student = Student.newBuilder().setStudentId(entry.getKey()).setStudentName(entry.getValue()).build();
            list.addEnrolled(student);
        }
        
        for(Map.Entry<String, String> entry : cancelStudents.entrySet()){
            Student cStudent = Student.newBuilder().setStudentId(entry.getKey()).setStudentName(entry.getValue()).build();
            list.addDiscarded(cStudent);
        }
        return list.build();
    }

    public synchronized ResponseCode cancelEnroll(String studentId, boolean gossip){
        if(flag) System.err.println("INF: Professor tries to cancel student enroll");
        if(!(active || gossip)) return ResponseCode.INACTIVE_SERVER;
        if(!students.containsKey(studentId)){
            if(flag) System.err.println("ERRO: Non existing student!"); 
            return ResponseCode.NON_EXISTING_STUDENT;
        }
        cancelStudents.put(studentId, students.get(studentId));
        if(reviewingStudents.contains(studentId)) {
            reviewingStudents.remove(studentId);
        }
        students.remove(studentId);
        if(flag) System.err.println("INF: Student enroll canceled with success!");
        return ResponseCode.OK;

    }    

    public synchronized void resolvePendingCommands() {
        if(flag) System.err.println("INF: Resolving pending commands");
        for(Map.Entry<Timestamp, String> c: pendingCommands.entrySet()) {
            Timestamp prev = c.getKey();
            List<Integer> replica = getTimestamp();
            
            if(!prev.lessOrEqual(replica)) {
                continue;
            }
            ResponseCode code = null;
            String[] lineSplited = c.getValue().split(" ", 0);
			if (ABRIR_INSCRICOES_CMD.equals(lineSplited[0])) {
                code = openEnrollments(Integer.parseInt(lineSplited[1]), false);
			} else if (FECHAR_INSCRICOES_CMD.equals(lineSplited[0])) {
				code = closeEnrollments(false);
			} else if (CANCELAR_INSCRICAO_CMD.equals(lineSplited[0])) {
				code = cancelEnroll(lineSplited[1], false);
      		} else if (INSCREVER_CMD.equals(lineSplited[0])) {
                code = enroll(lineSplited[1], lineSplited[2], false);
			}
            if(code != null && code == ResponseCode.OK) {
                prev = updatePrevTimestamp(prev.getTimestamp());
                updateTimestamp();
                addLog(prev, c.getValue());
            }
        }
        pendingCommands.clear();
        reviewingStudents.clear();
    }

    public synchronized void addPendingCommand(Timestamp timestamp, String command) {
        if(flag) System.err.println("INF: Adding command to log");
        pendingCommands.put(timestamp, command);
    }
}