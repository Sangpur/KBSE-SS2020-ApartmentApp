/*
 * ENTITY CLASS Event
 *
 */
package de.hsos.kbse.app.entity.features;

import de.hsos.kbse.app.entity.Member;
import de.hsos.kbse.app.enums.EventCategory;
import de.hsos.kbse.app.util.Condition;
import de.hsos.kbse.app.util.General;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javax.enterprise.inject.Vetoed;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 *
 * @author Annika Limbrock, Christoph Weigandt
 */
@Entity
@Vetoed
public class Event implements Serializable, Comparable<Event> {
    
    /* ----------------------------------------- ATTRIBUTES ---------------------------------------- */
    
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "modEvent")
    @TableGenerator(name = "modEvent", initialValue = 5)
    private Long id;
    
    @NotNull(groups = {General.class, Condition.class}, message="Der Titel darf nicht leer sein!")
    @Size(groups = {General.class, Condition.class}, min=3, max=50, message="Der Titel muss zwischen 3 und 50 Zeichen liegen!")
    @Pattern(groups = {General.class, Condition.class}, regexp = "^[0-9A-Za-zäÄöÖüÜß\\-\\.\\s]+$", message="Der Titel enthält ungültige Bezeichner!")
    private String title;
    
    @OneToOne(cascade = CascadeType.MERGE)
    private Member author;
    
    @NotNull(groups = {General.class}, message="Der Beginn muss gesetzt werden!")
    @FutureOrPresent(groups = {General.class}, message="Der Beginn darf nicht in der Vergangenheit liegen!")
    @Column(name="datetime_begin")
    @Temporal(TemporalType.TIMESTAMP)   // TemporalType enthaelt Datum und Zeit
    private Date begin;
    
    @NotNull(groups = {General.class, Condition.class}, message="Das Ende muss gesetzt werden!")
    @Future(groups = {General.class, Condition.class}, message="Das Ende muss in der Zukunft liegen!")
    @Column(name="datetime_end")
    @Temporal(TemporalType.TIMESTAMP)
    private Date end;
    
    @Enumerated(EnumType.STRING)
    private EventCategory category;   // BIRTHDAY (Geburtstag), EVENT (Veranstaltung), VACATION (Urlaub), APPOINTMENT (Termin), OTHER (Sonstiges);
    
    @Column(name="apartment_id")
    private Long apartmentID;
    
    @Column(name="allDayEvent")
    private boolean allDayEvent;
    
    /* --------------------------------------- PUBLIC METHODS -------------------------------------- */
    
    public Event(){}
    
    public Event(Member author, Date begin, Date end, Long apartmentID){
        this.author = author;
        this.begin = begin;
        this.end = end;
        this.apartmentID = apartmentID;
    }
    
    public Event(Event e){
        this.apartmentID = e.getApartmentID();
        this.author = e.getAuthor();
        this.begin = e.getBegin();
        this.category = e.getCategory();
        this.end = e.getEnd();
        this.title = e.getTitle();
    }
    
    @Override
    public int compareTo(Event e) {
        /* Sortiert Events basierend auf Startdatum und falls diese gleich sind, findet ein Vergleich mit Enddatum statt */
        if (this.begin == null || this.end == null || e.getBegin() == null || e.getEnd() == null){
            return 0;
        }else if(this.begin.before(e.getBegin())){
            return 1;
        }else if(this.begin.after(e.getBegin())){
            return -1;
        }else if(this.begin.equals(e.getBegin())){
            if(this.end.before(e.getEnd())){
                return 1;
            }else if(this.end.after(e.getEnd())){
                return -1;
            }else if(this.end.equals(e.getEnd())){
                return 0;
            }
        }
        return this.compareTo(e);
    }
    
    /* -------------------------------------- GETTER AND SETTER ------------------------------------ */
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getBegin() {
        return begin;
    }

    public void setBegin(Date begin) {
        this.begin = begin;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public EventCategory getCategory() {
        return category;
    }

    public void setCategory(EventCategory category) {
        this.category = category;
    }

    public Long getApartmentID() {
        return apartmentID;
    }

    public void setApartmentID(Long apartmentID) {
        this.apartmentID = apartmentID;
    }

    public Member getAuthor() {
        return author;
    }

    public void setAuthor(Member author) {
        this.author = author;
    }

    public boolean isAllDayEvent() {
        return allDayEvent;
    }

    public void setAllDayEvent(boolean allDayEvent) {
        this.allDayEvent = allDayEvent;
    }
    
    public String getDateFormat(Event event, LocalDate currentCalenderDay) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        LocalDate eventBegin = event.getBegin().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate eventEnd = event.getEnd().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        
        if(eventBegin.isEqual(currentCalenderDay) && eventEnd.isEqual(currentCalenderDay)) {
            /* Das Event ist beginnt und endet am aktuell betrachteten Tag. */
            return formatter.format(event.getBegin()) + " – " + formatter.format(event.getEnd()) + " Uhr";
        } else if(eventBegin.isBefore(currentCalenderDay) && eventEnd.isAfter(currentCalenderDay)) {
            /* Das Event ist mehrtaegig und der aktuell betrachtete Tag liegt mitten innerhalb der
             * Event-Zeit.*/
            return "ganztägig";
        } else if(eventBegin.isEqual(currentCalenderDay)) {
            /* Das Event beginnt am aktuell betrachteten Tag. */
            return "ab " + formatter.format(event.getBegin()) + " Uhr";
        } else if(eventEnd.isEqual(currentCalenderDay)) {
            /* Das Event endet am aktuell betrachteten Tag. */
            return "bis " + formatter.format(event.getEnd()) + " Uhr";
        }
        return "";
    }
    
}
