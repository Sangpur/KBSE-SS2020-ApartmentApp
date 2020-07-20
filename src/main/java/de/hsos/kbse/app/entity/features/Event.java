/*
 * ENTITY CLASS Event
 *
 */
package de.hsos.kbse.app.entity.features;

import de.hsos.kbse.app.entity.member.Member;
import de.hsos.kbse.app.enums.EventCategory;
import de.hsos.kbse.app.util.Condition;
import de.hsos.kbse.app.util.General;
import java.io.Serializable;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
@Entity
@Vetoed
public class Event implements Serializable, Comparable<Event> {
    
    /* ----------------------------------------- ATTRIBUTES ---------------------------------------- */
    
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "modEvent")
    @TableGenerator(name = "modEvent", initialValue = 3)
    private Long id;
    
    @NotNull(groups = {General.class, Condition.class}, message="Der Titel darf nicht leer sein!")
    @Size(groups = {General.class, Condition.class}, min=3, max=50, message="Der Titel muss zwischen 3 und 50 Zeichen liegen!")
    @Pattern(groups = {General.class, Condition.class}, regexp = "^[0-9A-Za-zäÄöÖüÜß\\-\\.\\s]+$", message="Der Titel enthält ungültige Bezeichner!")
    private String title;
    
    @OneToOne(cascade = CascadeType.MERGE)
    private Member author;
    
    @Column(name="datetime_begin")
    @Temporal(TemporalType.TIMESTAMP)   // TemporalType enthaelt Datum und Zeit
    private Date begin;
    
    @Column(name="datetime_end")
    @Temporal(TemporalType.TIMESTAMP)
    private Date end;
    
    @Enumerated(EnumType.STRING)
    private EventCategory category;   // BIRTHDAY (Geburtstag), EVENT (Veranstaltung), VACATION (Urlaub), APPOINTMENT (Termin), OTHER (Sonstiges);
    
    @Column(name="apartment_id")
    private Long apartmentID;
    
    /* --------------------------------------- PUBLIC METHODS -------------------------------------- */
    
    @Override
    public int compareTo(Event e) {
        System.out.println("compareTo()");
        
        if (this.begin == null || this.end == null || e.getBegin() == null || e.getEnd() == null){
            return 0;
        }
        return this.begin.compareTo(e.getBegin());

    }
    
    /* -------------------------------------- PRIVATE METHODS -------------------------------------- */
    
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
    
    
}
