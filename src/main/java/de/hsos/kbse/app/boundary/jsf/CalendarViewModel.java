/*
 * BOUNDARY CLASS CalendarViewModel
 *
 */
package de.hsos.kbse.app.boundary.jsf;

import de.hsos.kbse.app.control.Calendar;
import de.hsos.kbse.app.entity.features.Event;
import de.hsos.kbse.app.entity.member.Member;
import de.hsos.kbse.app.enums.EventCategory;
import de.hsos.kbse.app.enums.LogLevel;
import de.hsos.kbse.app.enums.MemberRole;
import de.hsos.kbse.app.enums.ValidationGroup;
import de.hsos.kbse.app.util.AppException;
import de.hsos.kbse.app.util.Condition;
import de.hsos.kbse.app.util.General;
import de.hsos.kbse.app.util.Logable;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
@Named("calendarVM")
@ConversationScoped
public class CalendarViewModel implements Serializable {
    
    /* ----------------------------------------- ATTRIBUTE ---------------------------------------- */
    
    /* Controller Class */
    private final Calendar calendar;
    
    /* Injected Conversation */
    private final Conversation conversation;
    
    /* Bean Validation API */
    private static Validator validator;
    
    private Member loggedInMember;
    private Event currentEvent;
    private Long apartmentID = 1000L; // TODO: Hinzufuegen sobald Scope gestartet wird bei Login-Prozess
    private List<Event> events;
    private boolean admin;           // true = ADMIN, false = USER
    private boolean addEvent;         // true = addNote()
    private boolean editEvent;        // true = editNote()
    private EventCategory[] categories;
    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    @Inject
    @Logable(LogLevel.INFO)
    public CalendarViewModel(Calendar calendar, Conversation conversation) {
        this.calendar = calendar;
        this.conversation = conversation;
        this.events = new LinkedList<>();
        this.initEventList();
        this.currentEvent = new Event();
        this.categories = EventCategory.values();
        
    }
    
    @PostConstruct
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    public String addEvent() {
        System.out.println("addEvent()");
        this.currentEvent = new Event();
        return "calendar-add";
    }
    
    public String editEvent(Event event){
        System.out.println("editEvent()");
        this.currentEvent = event;
        this.editEvent = true;
        this.addEvent = false;
        return "pinboard-add";
    }
    
    public void deleteEvent(Event event){
        System.out.println("deleteEvent()");
        try {
            this.calendar.deleteEvent(event);
            removeFromEventList(event);
        } catch (AppException ex) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
            facesContext.addMessage("Error",msg);
        }
    }
    
    public String saveEvent(){
        System.out.println("saveEvent()");
        
        this.currentEvent.setApartmentID(apartmentID);
        
        if(this.addEvent && !this.editEvent){
            // this.currentEvent.setTimestamp(new Date());
            // this.currentEvent.setAuthor(loggedInMember);
            
            try {
                this.calendar.createEvent(currentEvent);
            } catch (AppException ex) {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
                facesContext.addMessage("Error",msg);
            }
            addEventToList(this.currentEvent);
        }else if(!this.addEvent && this.editEvent){
            // this.currentEvent.setTimestamp(new Date());
            try {
                this.calendar.updateEvent(currentEvent);
            } catch (AppException ex) {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
                facesContext.addMessage("Error",msg);
            }
            updateEventList(this.currentEvent);
        }
        
        return "calendar";
    }
    
    public String discardEvent(){
        System.out.println("discardEvent()");
        this.currentEvent = new Event();
        return "calendar";
    }
    
    /* Überarbeiten */
    public boolean checkAccessRights(Event event) {
        /* Es muss geprueft werden, ob der jeweilige Zahlung bearbeitet oder geloescht werden kann,
         * da dies nur erlaubt ist, wenn der zugreifende Nutzer Admin oder der Verfasser ist. */
        return this.admin || event.getAuthor().getId().equals(this.loggedInMember.getId());
    }
    
    
    /* ------------------------------------- METHODEN PRIVATE ------------------------------------- */
    
    private void removeFromEventList(Event e){
        System.out.println("removeFromEventList()");
        for(int i = 0; i < this.events.size(); i++){
            if(this.events.get(i).equals(e)){
                this.events.remove(i);
            }
        }
    }
    
    private void updateEventList(Event e){
        System.out.println("updateNoteList()");
        this.events.forEach((event) -> {
            if(event.getId().equals(e.getId())){
                event = e;
            }
        });
        Collections.sort(this.events);
        Collections.reverse(this.events);
    }
    
    private void addEventToList(Event e){
        System.out.println("addEventToList()");
        this.events.add(e);
        Collections.sort(events);
        Collections.reverse(events);
    }
    
    private void initEventList(){
        System.out.println("initEventList()");
        try {
            this.events = this.calendar.getAllEventsFrom(apartmentID);

            Collections.sort(this.events);
            Collections.reverse(this.events);

        } catch(AppException ex) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
            facesContext.addMessage("Error",msg);
        }
    }
    
    
    @Logable(LogLevel.INFO)
    private boolean validateInput(ValidationGroup group) {
        /* Die Methode validate() gibt ein Set von ConstraintViolations zurueck, in dem alle moeglicherweise begangenen Verstoesse aufgefuehrt
         * sind. Dieses Set ist leer, falls die Eingabe gueltig ist. Durch die Gruppierung der Constraints, hier General.class, besteht die
         * Moeglichkeit, die Validation erst dann auszufuehren, wenn die entsprechende Gruppe direkt durch das Programm angesprochen wird.
         * s. https://www.baeldung.com/javax-validation-groups */
        Set<ConstraintViolation<Event>> constraintViolations = null;
        if(group == ValidationGroup.GENERAL) {
            constraintViolations = validator.validate( this.currentEvent, General.class );
        }

        if(constraintViolations != null && constraintViolations.isEmpty()) {
            /* Gueltige Eingabe */
            return true;
        } else {
            /* Ungueltige Eingabe */
            FacesContext facesContext = FacesContext.getCurrentInstance();
            Iterator<ConstraintViolation<Event>> iter = constraintViolations.iterator();
            while (iter.hasNext()) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Constraint.", iter.next().getMessage());
                facesContext.addMessage("Constraint Violation",msg);
            }
            return false;
        }
    }
    
    /* -------------------------------------- GETTER AND SETTER ------------------------------------ */
    
    public void setLoggedInMember(Member loggedInMember) {
        this.loggedInMember = loggedInMember;
        if(this.loggedInMember.getMemberRole() == MemberRole.ADMIN) {
            this.admin = true;
        }
    }

    public static Validator getValidator() {
        return validator;
    }

    public static void setValidator(Validator validator) {
        CalendarViewModel.validator = validator;
    }

    public Event getCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(Event currentEvent) {
        this.currentEvent = currentEvent;
    }

    public Long getApartmentID() {
        return apartmentID;
    }

    public void setApartmentID(Long apartmentID) {
        this.apartmentID = apartmentID;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isAddEvent() {
        return addEvent;
    }

    public void setAddEvent(boolean addEvent) {
        this.addEvent = addEvent;
    }

    public boolean isEditEvent() {
        return editEvent;
    }

    public void setEditEvent(boolean editEvent) {
        this.editEvent = editEvent;
    }

    public EventCategory[] getCategories() {
        return categories;
    }

    public void setCategories(EventCategory[] categories) {
        this.categories = categories;
    }
    
    
}
