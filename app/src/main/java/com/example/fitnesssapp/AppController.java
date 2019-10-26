package com.example.fitnesssapp;

public class AppController {

  float distance, kCals, mins;
  String today, time, uid;
  int steps;

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getkCals() {
        return kCals;
    }

    public void setkCals(float kCals) {
        this.kCals = kCals;
    }

    public float getMins() {
        return mins;
    }

    public void setMins(float mins) {
        this.mins = mins;
    }

    public String getToday() {
        return today;
    }

    public void setToday(String today) {
        this.today = today;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }
}
