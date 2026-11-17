package model;

public class Grade {
    private Integer id;
    private String name;
    private int numericLevel;

    public Grade() {
    }

    public Grade(int numericLevel) {
        this.numericLevel = numericLevel;
    }

    public int getNumericLevel() {
        return numericLevel;
    }

    public void setNumericLevel(int numericLevel) {
        this.numericLevel = numericLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Grade{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", numericLevel=" + numericLevel +
                '}';
    }
}
