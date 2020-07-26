/*
 * BOUNDARY CLASS CalendarViewModel
 *
 */
package de.hsos.kbse.app.boundary.jsf;

import de.hsos.kbse.app.control.Calendar;
import de.hsos.kbse.app.entity.features.CalendarDay;
import de.hsos.kbse.app.entity.features.Event;
import de.hsos.kbse.app.entity.Member;
import de.hsos.kbse.app.enums.EventCategory;
import de.hsos.kbse.app.enums.LogLevel;
import de.hsos.kbse.app.enums.MemberRole;
import de.hsos.kbse.app.enums.ValidationGroup;
import de.hsos.kbse.app.util.AppException;
import de.hsos.kbse.app.util.Condition;
import de.hsos.kbse.app.util.General;
import de.hsos.kbse.app.util.Logable;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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
import javax.servlet.http.HttpSession;
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
    
    private final Long apartmentID;
    private Event currentEvent;
    private Event originalEvent;
    
    private boolean addEvent;           // true = addEvent()
    private boolean editEvent;          // true = editEvent()
    private boolean isWholeDay; 
    private EventCategory[] categories;
    private LocalDate today;            // Heutiges Datum der System-Zeitzone
    private LocalDate currentMonth;     // Datum des Headers, immer der fünfte Tag eines Monats
    
    private List<Event> currentMonthEvents;
    private List<CalendarDay> currentMonthDays;
    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    @Inject
    @Logable(LogLevel.INFO)
    public CalendarViewModel(Calendar calendar, Conversation conversation) {
        this.calendar = calendar;
        this.conversation = conversation;
        this.currentMonthEvents = new ArrayList();
        this.currentMonthDays = new ArrayList();
        this.categories = EventCategory.values();
        this.today = LocalDate.now(ZoneId.systemDefault());
        this.currentMonth = this.today.withDayOfMonth(5);
        this.apartmentID = this.getLoggedInMember().getApartmentID();
        this.initCurrentMonth();
    }
    
    public boolean editBeginInPast() {
        Date date = this.currentEvent.getBegin();
        return this.editEvent && date.before(new Date());
    }
    
    public boolean editBeginInFuture() {
        Date date = this.currentEvent.getBegin();
        return this.editEvent && date.after(new Date());
    }
    
    public LocalDateTime compareBeginToday(Date begin){
        /* Beschränkt das Startdatum beim Bearbeiten eines Events */
        LocalDateTime tmpBegin = begin.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        if(tmpBegin.isBefore(LocalDateTime.now(ZoneId.systemDefault()))){
            return tmpBegin;
        }else{
            return LocalDateTime.now(ZoneId.systemDefault());
        }
    }
    
    public boolean isFutureDate(LocalDate date){
        /* Überprüft ob Event in der Vergangenheit liegt oder noch in der Zukunft und somit zu Bearbeiten ist */
        return date.isAfter(today.minusDays(1));
    }

    public boolean checkAccessRightAndDate(LocalDate currentDay, Event currentEvent) {
        /* Es muss geprueft werden, ob der jeweilige Zahlung bearbeitet oder geloescht werden kann,
         * da dies nur erlaubt ist, wenn der zugreifende Nutzer Admin oder der Verfasser ist. */
        boolean isAdmin = this.getLoggedInMember().getMemberRole().equals(MemberRole.ADMIN);
        boolean access = isAdmin || currentEvent.getAuthor().getId().equals(this.getLoggedInMember().getId());
        boolean isFutureDate = currentDay.isAfter(today.minusDays(1));
        return access && isFutureDate;
    }
    
    @Logable(LogLevel.INFO)
    public String addEvent() {
        /* Neues Event-Objekt initiieren */
        this.currentEvent = new Event(this.getLoggedInMember(), new Date(), new Date(), this.apartmentID);
        this.originalEvent = new Event(this.currentEvent);
        this.addEvent = true;
        this.editEvent = false;
        return "calendar-add";
    }
    
    @Logable(LogLevel.INFO)
    public String editEvent(Event event){
        /* Das Event-Objekt setzen und eine Sicherheitskopie des Objekts erstellen */
        System.out.println("editEvent()");
        this.currentEvent = event;
        this.originalEvent = new Event(this.currentEvent);
        this.editEvent = true;
        this.addEvent = false;
        return "calendar-add";
    }
    
    @Logable(LogLevel.INFO)
    public void deleteEvent(Event event){
        this.addEvent = false;
        this.editEvent = false;
        try {
            /* Entfernen ded Event-Objekts aus der Datenbank */
            this.calendar.deleteEvent(event);
        } catch (AppException ex) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
            facesContext.addMessage("Error",msg);
        }
        /* Bestehendes Event-Objekt aus der Liste entfernen */
        this.currentMonthEvents.remove(event);
        this.initCurrentMonthView();

    }
    
    @Logable(LogLevel.INFO)
    public String saveEvent(){
        if(this.isIsWholeDay()){
            this.currentEvent.setAllDayEvent(this.isIsWholeDay());
            this.currentEvent.setBegin(this.currentEvent.getEnd());
        }
        if(this.addEvent ){ // Neues Event
            if(this.validateInput(ValidationGroup.GENERAL)) { // Gültige Eingabe
                try {
                    /* Neues Event-Objekt der Datenbank hinzufügen */
                    this.calendar.createEvent(currentEvent);
                } catch (AppException ex) {
                    FacesContext facesContext = FacesContext.getCurrentInstance();
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
                    facesContext.addMessage("Error",msg);
                }
                /* Hinzufügen in die Liste der Events */
                this.addToEventsList(this.currentEvent);
                this.initCurrentMonthView();
                return "calendar";
            }
        } else if(this.editEvent){ // Bestehendes Event bearbeiten
            boolean valid;
            if(this.originalEvent.getBegin().before(new Date())) {
                valid = this.validateInput(ValidationGroup.CONDITION);
            } else {
                valid = this.validateInput(ValidationGroup.GENERAL);
            }
            if(valid) { // Gültige Eingabe
                try {
                    /* Bestehendes Event-Objekt wird in der Datenbank aktualisiert */
                    this.currentEvent.setAllDayEvent(this.isIsWholeDay());
                    this.calendar.updateEvent(currentEvent);
                } catch (AppException ex) {
                    FacesContext facesContext = FacesContext.getCurrentInstance();
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
                    facesContext.addMessage("Error",msg);
                }
                this.initCurrentMonthView();
                return "calendar";
            }
        }
        return "";
    }
    
    public String discardEvent(){
        /* Falls ein vorhandenes Objekt bearbeitet werden sollte und diese Aktion abgebrochen wurde,
         * muss dieses auf seinen Originalzustand zurückgesetzt werden.*/
        if(this.editEvent){
            /* Falls ein vorhandenes Objekt bearbeitet werden sollte und diese Aktion abgebrochen wurde,
             * muss dieses auf seinen Originalzustand zurückgesetzt werden.*/
            this.currentEvent.setTitle(this.originalEvent.getTitle());
            this.currentEvent.setBegin(this.originalEvent.getBegin());
            this.currentEvent.setEnd(this.originalEvent.getEnd());
            this.currentEvent.setCategory(this.originalEvent.getCategory());
            this.currentEvent.setAllDayEvent(this.originalEvent.isAllDayEvent());
        }
        return "calendar";
    }
       
    public void deleteAllEvents() {
        try {
            /* Bestehende Event-Objekte in der Datenbank loeschen */
            this.calendar.deleteAllEventsFrom(this.apartmentID);
        } catch (AppException ex) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
            facesContext.addMessage("Error",msg);
        }
    }
    
    public void backwards(){
        /* Geht einen Monat in die Vergangenheit, setzt Header auf den fünften Tag des Monats */
        this.currentMonth = this.currentMonth.minusMonths(1).withDayOfMonth(5);
        this.initCurrentMonth();
    }
    
    public void forwards(){
        /* Geht einen Monat in die Zukunft, setzt Header auf den fünften Tag des Monats */
        this.currentMonth = this.currentMonth.plusMonths(1).withDayOfMonth(5);
        this.initCurrentMonth();
    }
      
    public void setBackToToday(){
        /* Setzt den Kalender auf Heute zurück */
        this.currentMonth = this.today.withDayOfMonth(5);
    }
       
    public void onSelectStartDate(){
        /* Beschränkt Enddatum des Datepickers */
        this.currentEvent.setEnd(null);
    }
    
    
    
    /* ------------------------------------- METHODEN PRIVATE ------------------------------------- */
    
    @PostConstruct
    private void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    private void initCurrentMonth() {
        this.initCurrentMonthEvents();
        this.initCurrentMonthView();
    }
    
    private void initCurrentMonthEvents() {
        int year = this.currentMonth.getYear();
        int month = this.currentMonth.getMonthValue();
        int totalDays = this.currentMonth.getMonth().length(isLeapYear(this.currentMonth.getYear()));
        try {
            /* Abruf der Events aus dem aktuellen Monat aus der Datenbank */
            this.currentMonthEvents = this.calendar.getAllEventsFromMonth(this.apartmentID, year, month, totalDays);
        } catch(AppException ex) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
            facesContext.addMessage("Error",msg);
        }
    }
    
    private void initCurrentMonthView() {
        /* Initialisiert CalenderDay-Objekte zur Anzeige des gesamten Monats */
        this.currentMonthDays.clear();
        int year = this.currentMonth.getYear();
        int month = this.currentMonth.getMonthValue();
        int totalDays = this.currentMonth.getMonth().length(isLeapYear(this.currentMonth.getYear()));
        for(int i = 0; i < totalDays; i++){
            LocalDate tmpDate = LocalDate.of(year, month, (i + 1));
            CalendarDay tempDay = initCalenderDay(tmpDate);
            this.currentMonthDays.add(tempDay);
        } 
    }
    
    private CalendarDay initCalenderDay(LocalDate date){
        /* Initialisiert CalendarDay-Objekt mit zugehoeriger Eventsliste und einem Boolean zur
         * Anzeige, der abspeichert, ob ueberhaupt Events vorhanden sind. */
        List<Event> eventList = this.getEventsListOf(date);
        boolean hasEvents = !eventList.isEmpty();  
        return new CalendarDay(date, eventList, hasEvents);
    }
    
    private List<Event> getEventsListOf(LocalDate currentDate){
        /* Erstellt eine Liste der für den Tag vorliegenden Events */
        List<Event> eventList = new ArrayList();
        /* Innerhalb der Liste werden alle Events des aktuellen Monats durchlaufen und zu den entsprechenden Tagen zugeordnet. */
        for(int i = 0; i < this.currentMonthEvents.size(); i++){
            Event tmpEvent = this.currentMonthEvents.get(i);
            /* Konvertierung des Date-Objekts in LocalDate für nachfolgenden Vergleich */
            LocalDate eventBegin = tmpEvent.getBegin().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();   // Beginn des Events
            LocalDate eventEnd = tmpEvent.getEnd().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();       // Ende des Events
            /* Das Event wird der Liste nur dann hinzugefuegt, wenn der Eventbeginn vor oder am aktuellen Tag und das
             * Event-Ende gleich nach oder am aktuellen Tag stattfindet. */
            if(eventBegin.isBefore(currentDate) && eventEnd.isAfter(currentDate) || currentDate.isEqual(eventBegin) || currentDate.isEqual(eventEnd)) {
                eventList.add(tmpEvent);
            }
        }
        return eventList;
    }
    
    private void addToEventsList(Event event){
        LocalDate eventBegin = event.getBegin().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate eventEnd = event.getEnd().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int month = this.currentMonth.getMonthValue();      // aktueller Monat
        int eventBeginMonth = eventBegin.getMonthValue();
        int eventEndMonth = eventEnd.getMonthValue();
        /* Nur falls das neu hinzugefuegte Event in dem gerade angezeigten Monat liegt, muss eine Aktualisierung der Liste "currentMonthEvents"
         * stattfinden. Im anderen Fall wird dieses Event erst mit dem Monatswechsel entsprechend aus der Datenbank bezogen. */
        if(eventBeginMonth == month || eventEndMonth == month || (eventBeginMonth < month && eventEndMonth > month)) {
            /* Hinzufuegen des neuen Event-Objekts zur Liste und erneute Initiierung der Monats-Uebersicht */
            this.currentMonthEvents.add(event);
            this.initCurrentMonthView();
        }
    }
    
    private boolean isLeapYear(int year){
        /* Berechnet Schaltjahr */
        return year % 4 == 0 && year % 100 != 0 || year % 400 == 0 || new GregorianCalendar().isLeapYear(year);
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
        } else if(group == ValidationGroup.CONDITION) {
            constraintViolations = validator.validate( this.currentEvent, Condition.class );
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
    
    private Member getLoggedInMember() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
        return (Member) session.getAttribute("user");
    }

    public Event getCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(Event currentEvent) {
        this.currentEvent = currentEvent;
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

    public LocalDate getCurrentMonth() {
        return currentMonth;
    }
    
    public String getCurrentMonthFormat(){
        /* Generiert den String des Monats und Jahres im Header des Kalenders */
        String tmpM = this.currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        int year = this.currentMonth.getYear();
        return tmpM + " " + year;
    }

    public void setCurrentMonth(LocalDate currentMonth) {
        this.currentMonth = currentMonth;
    }

    public List<CalendarDay> getMonthsDayList() {
        return currentMonthDays;
    }

    public void setMonthsDayList(List<CalendarDay> monthsDayList) {
        this.currentMonthDays = monthsDayList;
    }

    public LocalDate getToday() {
        return today;
    }

    public void setToday(LocalDate today) {
        this.today = today;
    }

    public boolean isIsWholeDay() {
        return isWholeDay;
    }

    public void setIsWholeDay(boolean isWholeDay) {
        this.isWholeDay = isWholeDay;
    }
    
    
    
    
}
