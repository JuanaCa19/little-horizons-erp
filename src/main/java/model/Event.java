package model;

import java.time.LocalDate;
import model.enums.*;

public class Event {
    private Integer id;
    private String name;
    private String description;
    private EventType type;
    private LocalDate date;
    private String location;
    private boolean competition;
    private Integer organizerId;
    private boolean active;

    public Event() { }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id=id; }

    public String getName() { return name; }
    public void setName(String name) { this.name=name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description=description; }

    public EventType getType() { return type; }
    public void setType(EventType type) { this.type=type; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date=date; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location=location; }

    public boolean isCompetition() { return competition; }
    public void setCompetition(boolean competition) { this.competition=competition; }

    public Integer getOrganizerId() { return organizerId; }
    public void setOrganizerId(Integer organizerId) { this.organizerId=organizerId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active=active; }
}
