/*
 * ENTITY CLASS CalenderDay
 *
 */
package de.hsos.kbse.app.entity.features;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
public class CalendarDay {
    
    /* ----------------------------------------- ATTRIBUTES ---------------------------------------- */
    
    private LocalDate date;
    private List<Event> events;
    private boolean hasEvents;
    
    /* --------------------------------------- PUBLIC METHODS -------------------------------------- */
    
    public CalendarDay(){
        this.events = new LinkedList<>();
        this.hasEvents = false;
    }
    
    public CalendarDay(LocalDate date, List<Event> events, boolean hasEvents){
        this.date = date;
        this.events = events;
        this.hasEvents = hasEvents;
        prepareEventList();
    }
    
    /* -------------------------------------- PRIVATE METHODS -------------------------------------- */

    private void prepareEventList(){
        /* sortiert die Event-Liste */

        Collections.sort(events);
        Collections.reverse(events);
    }
    
    /* -------------------------------------- GETTER AND SETTER ------------------------------------ */

    public LocalDate getDate() {
        return date;
    }
    
    public String getDateFormat(){
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM");
        String strDate = formatter.format(java.sql.Date.valueOf(this.date));
        return strDate;
    }
    
    public String getWeekday(){
        SimpleDateFormat formatter = new SimpleDateFormat("E");
        String strDate = formatter.format(java.sql.Date.valueOf(this.date));
        return strDate;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public boolean isHasEvents() {
        return hasEvents;
    }

    public void setHasEvents(boolean hasEvents) {
        this.hasEvents = hasEvents;
    }

}
