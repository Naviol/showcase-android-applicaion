package com.plymouth.assessment.cw2.showcase;

public class Project {

    private Integer projectID;
    private int studentID;
    private String title;
    private String description;
    private int year;
    private String thumbnailURL;
    private String posterURL;
    private String first_Name;
    private String second_Name;
    private String photo;

    public Project(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public Project(int studentID, String title, String description, int year, String first_Name, String second_Name) {
        this.studentID = studentID;
        this.title = title;
        this.description = description;
        this.year = year;
        this.first_Name = first_Name;
        this.second_Name = second_Name;
    }

    public Project(int studentID, String title, String description, int year, String first_Name, String second_Name, String posterURL) {
        this.studentID = studentID;
        this.title = title;
        this.description = description;
        this.year = year;
        this.first_Name = first_Name;
        this.second_Name = second_Name;
        this.posterURL = posterURL;
    }

    public Integer getProjectID() {
        return projectID;
    }

    public int getStudentID() {
        return studentID;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getYear() {
        return year;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public String getFirst_Name() {
        return first_Name;
    }

    public String getSecond_Name() {
        return second_Name;
    }

    public String getPhoto() {
        return photo;
    }
}
