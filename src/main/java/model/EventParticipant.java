package model;

import java.time.LocalDate;
import model.enums.*;

public class EventParticipant {
    private Integer id;
    private Integer eventId;
    private Integer studentId;
    private EventRole role;
    private EventPosition position;
    private String notes;

    public EventParticipant() { }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id=id; }

    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId=eventId; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId=studentId; }

    public EventRole getRole() { return role; }
    public void setRole(EventRole role) { this.role=role; }

    public EventPosition getPosition() { return position; }
    public void setPosition(EventPosition position) { this.position=position; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes=notes; }
}
