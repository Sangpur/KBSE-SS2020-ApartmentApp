/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
public class Day {
    
    private LocalDate date;
    private List<Event> events;
    private boolean hasEvents;
    
    public Day(){
        this.events = new LinkedList<>();
        this.hasEvents = false;
    }
    
    public Day(LocalDate date, List<Event> events, boolean hasEvents){
        this.date = date;
        this.events = events;
        this.hasEvents = hasEvents;
        prepareEventList();
    }

    private void prepareEventList(){
        /* Setzt allDayEvent-boolean Flag und sortiert die Event-Liste */
        LocalDateTime midNight = this.date.atStartOfDay();
        LocalDateTime beforeMidNight = midNight.plusHours(23).plusMinutes(59).plusSeconds(59);
        
        for(int i = 0; i < this.events.size(); i++){
            LocalDateTime tmpBegin = this.events.get(i).getBegin().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();;
            LocalDateTime tmpEnd = this.events.get(i).getEnd().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            
            if(tmpBegin.isBefore(midNight) && tmpEnd.isAfter(beforeMidNight)){
                this.events.get(i).setAllDayEvent(true);
            }
        }
        
        Collections.sort(events);
        Collections.reverse(events);
    }

    public LocalDate getDate() {
        return date;
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
    
    public String convertToDate(LocalDate lDate){
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM");
        String strDate = formatter.format(java.sql.Date.valueOf(date));
        return strDate;
    }
  
}
