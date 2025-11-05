package com.example.callepeninsulares;

public class Schedule {
    private int id;
    private String subject, building, date, startTime, endTime, room, meetLink;
    private int minutesBefore;
    private boolean isOnlineClass;


    public Schedule(int id, String building, String subject, String date, String startTime, String endTime, String room, int minutesBefore, boolean isOnlineClass, String meetLink) {
        this.id = id;
        this.building = building;
        this.subject = subject;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.minutesBefore = minutesBefore;
        this.isOnlineClass = isOnlineClass;
        this.meetLink = meetLink;
    }

    // getters
    public int getId() { return id; }
    public String getBuilding() { return building; }
    public String getSubject() { return subject; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getRoom() { return room; }
    public int getMinutesBefore() { return minutesBefore; }
    public boolean isOnlineClass() { return isOnlineClass; }
    public String getMeetLink() { return meetLink; }
}



