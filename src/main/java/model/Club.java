package model;

import java.time.LocalDate;
import model.enums.*;

public class Club {
    private Integer id;
    private String name;
    private String description;
    private Integer supervisorId;
    private int capacity;
    private String meetingSchedule;
    private boolean active;

    public Club() { }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id=id; }

    public String getName() { return name; }
    public void setName(String name) { this.name=name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description=description; }

    public Integer getSupervisorId() { return supervisorId; }
    public void setSupervisorId(Integer supervisorId) { this.supervisorId=supervisorId; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity=capacity; }

    public String getMeetingSchedule() { return meetingSchedule; }
    public void setMeetingSchedule(String meetingSchedule) { this.meetingSchedule=meetingSchedule; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active=active; }
}
