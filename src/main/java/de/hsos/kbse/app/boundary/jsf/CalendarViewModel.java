/*
 * BOUNDARY CLASS CalendarViewModel
 *
 */
package de.hsos.kbse.app.boundary.jsf;

import de.hsos.kbse.app.control.Calendar;
import de.hsos.kbse.app.entity.features.CalendarDay;
import de.hsos.kbse.app.entity.features.Event;
import de.hsos.kbse.app.entity.member.Member;
import de.hsos.kbse.app.enums.EventCategory;
import de.hsos.kbse.app.enums.LogLevel;
import de.hsos.kbse.app.enums.MemberRole;
import de.hsos.kbse.app.enums.ValidationGroup;
import de.hsos.kbse.app.util.AppException;
import de.hsos.kbse.app.util.General;
import de.hsos.kbse.app.util.Logable;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
    private Event originalEvent;
    private Long apartmentID = 1000L; // TODO: Hinzufuegen sobald Scope gestartet wird bei Login-Prozess
    private List<Event> events;
    private List<CalendarDay> month;
    private boolean admin;           // true = ADMIN, false = USER
    private boolean addEvent;         // true = addNote()
    private boolean editEvent;        // true = editNote()
    private EventCategory[] categories;
    private LocalDate today;    // Heutiges Datum der System-Zeitzone
    private LocalDate header;   // Datum des Headers, immer der fünfte Tag eines Monats
    
    
    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    @Inject
    @Logable(LogLevel.INFO)
    public CalendarViewModel(Calendar calendar, Conversation conversation) {
        this.calendar = calendar;
        this.conversation = conversation;
        this.events = new LinkedList<>();
        
        this.currentEvent = new Event();
        this.originalEvent = new Event();
        this.categories = EventCategory.values();
        this.initEventList();

        this.today = LocalDate.now(ZoneId.systemDefault());
        this.header = this.today.withDayOfMonth(5);
        this.month = new LinkedList<>();
        this.initCalendarForMonth();
        
    }
    
    @PostConstruct
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Logable(LogLevel.INFO)
    public String addEvent() {
        /* Neues Event-Objekt initiieren */
        this.currentEvent = new Event(this.loggedInMember, this.apartmentID);
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
        /* Löscht das Event-Objekt aus der Datenbank und Event-Liste */
        System.out.println("deleteEvent()");
        System.out.println("Title: " + event.getTitle());

        this.addEvent = false;
        this.editEvent = false;
        try {
            this.calendar.deleteEvent(event);
        } catch (AppException ex) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
            facesContext.addMessage("Error",msg);
        }
        /* Bestehendes Event-Objekt aus der Liste entfernen */
        removeFromEventList(event);

    }
    
    @Logable(LogLevel.INFO)
    public String saveEvent(){
        System.out.println("Save Event");
        if(this.validateInput(ValidationGroup.GENERAL)) { // Gültige Eingabe
            if(this.addEvent ){ // Neues Event 
                try {
                    /* Neues Event-Objekt der Datenbank hinzufügen */
                    this.calendar.createEvent(currentEvent);
                } catch (AppException ex) {
                    FacesContext facesContext = FacesContext.getCurrentInstance();
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
                    facesContext.addMessage("Error",msg);
                }
                /* Hinzufügen in die Liste der Events */
                addEventToList(this.currentEvent);
            }else if(this.editEvent){ // Bestehendes Event bearbeiten
                try {
                    /* Bestehendes Event-Objekt wird in der Datenbank aktualisiert */
                    this.calendar.updateEvent(currentEvent);

                } catch (AppException ex) {
                    FacesContext facesContext = FacesContext.getCurrentInstance();
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
                    facesContext.addMessage("Error",msg);
                }
                
                /* Bestehendes Event-Objekt in der Liste wird aktualisiert */
                updateEventList(this.currentEvent);
            }
        }
        return "calendar";
    }
    
    public String discardEvent(){
        /* Falls ein vorhandenes Objekt bearbeitet werden sollte und diese Aktion abgebrochen wurde,
         * muss dieses auf seinen Originalzustand zurückgesetzt werden.*/
        if(this.editEvent){
            this.currentEvent = new Event(this.getOriginalEvent());
        }
        return "calendar";
    }
    
    public String getHeaderTitle(){
        /* Generiert den String des Monats und Jahres im Header des Kalenders */
        String tmpM = this.header.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        int year = this.header.getYear();
        return tmpM + ", " + year;
    }
       
    public void backwards(){
        /* Geht einen Monat in die Vergangenheit, setzt Header auf den fünften Tag des Monats */
        this.header = this.header.minusMonths(1).withDayOfMonth(5);
        this.initCalendarForMonth();
    }
    
    public void forwards(){
        /* Geht einen Monat in die Zukunft, setzt Header auf den fünften Tag des Monats */
        this.header = this.header.plusMonths(1).withDayOfMonth(5);
        this.initCalendarForMonth();
    }
      
    public void setBackToToday(){
        /* Setzt den Kalender auf Heute zurück */
        this.header = this.today.withDayOfMonth(5);
    }
       
    public void onSelectStartDate(){
        /* Beschränkt Enddatum des Datepickers */
        this.currentEvent.setEnd(null);
    }
    
    public boolean checkIfInPast(Event e){
        /* Überprüft ob Event in der Vergangenheit liegt oder noch in der Zukunft und somit zu Bearbeiten ist */
        return !e.getEnd().before(java.sql.Timestamp.valueOf(LocalDateTime.now(ZoneId.systemDefault())));
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

    public boolean checkAccessRights(Event event) {
        /* Es muss geprueft werden, ob der jeweilige Zahlung bearbeitet oder geloescht werden kann,
         * da dies nur erlaubt ist, wenn der zugreifende Nutzer Admin oder der Verfasser ist. */
        return this.admin || event.getAuthor().getId().equals(this.loggedInMember.getId());
    }
    
    /* ------------------------------------- METHODEN PRIVATE ------------------------------------- */
       
    private void removeFromEventList(Event e){
        /* Entfernt das gesuchte Event aus der Liste und initiiert den Monat anschließend neu */
        for(int i = 0; i < this.events.size(); i++){
            if(this.events.get(i).equals(e)){
                this.events.remove(i);
            }
        }
        
        initCalendarForMonth();
        
    }
    
    private void updateEventList(Event e){
        /* Das Event wird in der Liste aktualisiert und initiiert den Monat anschließend neu */
        this.events.forEach((event) -> {
            if(event.getId().equals(e.getId())){
                event = e;
            }
        });
        
        initCalendarForMonth();
    }
    
    private void addEventToList(Event e){
        /* Ein neues Event wird erstellt und der Liste hinzugefügt. Anschließend wird der Monat neu initiiert */
        this.events.add(e);
        initCalendarForMonth();
    }
    
    private void initEventList(){
        /* Lädt Liste der Events */
        try {
            this.events = this.calendar.getAllEventsFrom(apartmentID);
        } catch(AppException ex) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
            facesContext.addMessage("Error",msg);
        }
    }
    
    private void initCalendarForMonth(){
        /* Initiiert den Kalender für den angezeigten Monat */
        System.out.println("initCalendarForMonth()");
        this.month = new LinkedList<>();

        int yearValue = this.header.getYear();
        int monthValue = this.header.getMonthValue();
        int daysInMonth = this.header.getMonth().length(isLeapYear(this.header.getYear()));
        
        for(int i = 0; i < daysInMonth; i++){
            LocalDate tmp = LocalDate.of(yearValue, monthValue, (i + 1));
            CalendarDay tmpD = createNewDay(tmp, (i + 1));
            this.month.add(tmpD);
        }
    }
    
    private List<Event> getEventsOfDay(LocalDate date){
        /* Erstellt eine Liste der für den Index (Tag) vorliegenden Events */
        List<Event> tmp = new LinkedList<>();
        
        for(int i = 0; i < this.events.size(); i++){
            /* Umrechnung der Dates in LocalDate für Vergleich */
            LocalDate ldBegin = this.events.get(i).getBegin().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate ldEnd = this.events.get(i).getEnd().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            
            if(ldBegin.getDayOfMonth() <= date.getDayOfMonth() && date.getDayOfMonth() <= ldEnd.getDayOfMonth() || date.isEqual(ldBegin) || date.isEqual(ldEnd)){
                tmp.add(this.events.get(i));
            }
        }
        return tmp;
    }
    
    private CalendarDay createNewDay(LocalDate date, int index){
        /* Erstellt das Objekt Tag für den Monat */
        LocalDate tmpLD = date;
        List<Event> tmpEList = this.getEventsOfDay(date);
        boolean hasEvents = !tmpEList.isEmpty();  
        return new CalendarDay(tmpLD, tmpEList, hasEvents);
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

    public LocalDate getHeader() {
        return header;
    }

    public void setHeader(LocalDate header) {
        this.header = header;
    }

    public List<CalendarDay> getMonth() {
        return month;
    }

    public void setMonth(List<CalendarDay> month) {
        this.month = month;
    }

    public Event getOriginalEvent() {
        return originalEvent;
    }

    public void setOriginalEvent(Event originalEvent) {
        this.originalEvent = originalEvent;
    }

    public LocalDate getToday() {
        return today;
    }

    public void setToday(LocalDate today) {
        this.today = today;
    }
    
    
    
}
