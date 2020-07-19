/*
 * BOUNDARY CLASS CalenderViewModel
 *
 */
package de.hsos.kbse.app.boundary.jsf;

import de.hsos.kbse.app.control.Pinboard;
import de.hsos.kbse.app.entity.features.Note;
import de.hsos.kbse.app.entity.member.Member;
import de.hsos.kbse.app.enums.LogLevel;
import de.hsos.kbse.app.enums.MemberRole;
import de.hsos.kbse.app.enums.NoteCategory;
import de.hsos.kbse.app.enums.ValidationGroup;
import de.hsos.kbse.app.util.AppException;
import de.hsos.kbse.app.util.Condition;
import de.hsos.kbse.app.util.General;
import de.hsos.kbse.app.util.Logable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
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
@Named("pinboardVM")
@ConversationScoped
public class PinboardViewModel implements Serializable {
    
    /* ----------------------------------------- ATTRIBUTE ---------------------------------------- */
    
    /* Controller Class */
    private final Pinboard pinboard;
    
    /* Injected Conversation */
    private final Conversation conversation;
    
    /* Bean Validation API */
    private static Validator validator;
    
    private Long apartmentID = 1000L; // TODO: Hinzufuegen sobald Scope gestartet wird bei Login-Prozess
    private Member loggedInMember;
    private Note currentNote;
    private List<Note> notes;
    private boolean admin;           // true = ADMIN, false = USER
    private boolean addNote;         // true = addNote()
    private boolean editNote;        // true = editNote()
    private NoteCategory[] categories;

    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    @Inject
    @Logable(LogLevel.INFO)
    public PinboardViewModel(Pinboard pinboard, Conversation conversation) {
        this.pinboard = pinboard;
        this.conversation = conversation;
        this.notes = new LinkedList<>();
        this.initNoteList();
        this.currentNote = new Note();
        this.categories = NoteCategory.values();
    }
    
    @PostConstruct
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    public String addNote() {
        System.out.println("addNote()");
        this.currentNote = new Note();
        this.addNote = true;
        this.editNote = false;
        return "pinboard-add";
    }
    
    public String editNote(Note note){
        System.out.println("editNote()");
        this.currentNote = note;
        this.editNote = true;
        this.addNote = false;
        return "pinboard-add";
    }
    
    public void deleteNote(Note note){
        System.out.println("deleteNote()");
        /* try {
            this.pinboard.deleteNote(note);
        } catch (AppException ex) {
            Logger.getLogger(PinboardViewModel.class.getName()).log(Level.SEVERE, null, ex);
        } */
    }
    
    public String saveNote(){
        System.out.println("saveNote()");
        
        this.currentNote.setApartmentID(apartmentID);
        this.currentNote.setTimestamp(new Date());
        this.currentNote.setAuthor(loggedInMember);
        
        if(this.addNote && !this.editNote){
            /* try {
                this.pinboard.createNote(currentNote);
            } catch (AppException ex) {
                Logger.getLogger(PinboardViewModel.class.getName()).log(Level.SEVERE, null, ex);
            } */
            addNoteToList();
        }else{
            /* try {
                this.pinboard.updateNote(currentNote);
            } catch (AppException ex) {
                Logger.getLogger(PinboardViewModel.class.getName()).log(Level.SEVERE, null, ex);
            } */
            updateNoteList();
        }
        
        return "pinboard";
    }
    
    public String discardNote(){
        System.out.println("discardNote()");
        this.currentNote = new Note();
        return "pinboard";
    }
    
    public boolean checkAccessRights(Note note) {
        /* Es muss geprueft werden, ob der jeweilige Zahlung bearbeitet oder geloescht werden kann,
         * da dies nur erlaubt ist, wenn der zugreifende Nutzer Admin oder der Verfasser ist. */
        return this.admin || note.getAuthor().getId().equals(this.loggedInMember.getId());
    }
    
    /* ------------------------------------- METHODEN PRIVATE ------------------------------------- */
    
    private void updateNoteList(){
        System.out.println("updateNoteList()");
        this.notes.forEach((note) -> {
            if(note.getId().equals(currentNote.getId())){
                note = currentNote;
            }
        });
    }
    
    private void addNoteToList(){
        System.out.println("addNoteToList()");
        int countUrgent = 0;
        boolean added = false;
             
        for(int i = 0; i < this.notes.size(); i++){
           if(this.notes.get(i).getCategory().equals(NoteCategory.URGENT)){
               countUrgent++;
            }
        }
        if(this.currentNote.getCategory().equals(NoteCategory.URGENT)){
            for(int i = 0; i < countUrgent; i++){
                if(this.currentNote.getTimestamp().after(this.notes.get(i).getTimestamp())){
                    this.notes.add(i, currentNote);
                    added = true;
                }
            }
            if(!added){
               this.notes.add(countUrgent + 1, currentNote);
            }
        }else{
            for(int k = countUrgent; k < this.notes.size();k++){
                if(this.currentNote.getTimestamp().after(this.notes.get(k).getTimestamp())){
                    this.notes.add(k, currentNote);
                    added = true;
                }
            }
            if(!added){
               this.notes.add(currentNote);
            }
        }
    }
    
    private void initNoteList() {
        try {
            List<Note> tmp = this.pinboard.getAllNotesFrom(apartmentID);
            List<Note> tmpUrgent = new LinkedList<>();
            List<Note> tmpNormal = new LinkedList<>();
            Date lastWeek = java.sql.Date.valueOf(LocalDate.now().minusDays(7));
            
            tmp.forEach((note) -> {
                if(note.getTimestamp().after(lastWeek)){
                    if(note.getCategory().equals(NoteCategory.URGENT)){
                        tmpUrgent.add(note);
                    }else{
                        tmpNormal.add(note);
                    }
                }   
            });
            
            Collections.sort(tmpUrgent);
            Collections.sort(tmpNormal);
            Collections.reverse(tmpUrgent);
            Collections.reverse(tmpNormal);
            
            this.notes.addAll(tmpUrgent);
            this.notes.addAll(tmpNormal);
            
        } catch(AppException ex) {
            String msg = ex.getMessage();
            FacesContext.getCurrentInstance().addMessage("Error", new FacesMessage(msg));
        }
    }
    
    
    @Logable(LogLevel.INFO)
    private boolean validateInput(ValidationGroup group) {
        /* Die Methode validate() gibt ein Set von ConstraintViolations zurueck, in dem alle moeglicherweise begangenen Verstoesse aufgefuehrt
         * sind. Dieses Set ist leer, falls die Eingabe gueltig ist. Durch die Gruppierung der Constraints, hier General.class, besteht die
         * Moeglichkeit, die Validation erst dann auszufuehren, wenn die entsprechende Gruppe direkt durch das Programm angesprochen wird.
         * s. https://www.baeldung.com/javax-validation-groups */
        Set<ConstraintViolation<PinboardViewModel>> constraintViolations = null;
        if(group == ValidationGroup.GENERAL) {
            // Moeglichkeit 1 -> diese Klasse selbst validieren
            constraintViolations = validator.validate( this, General.class );
            // Moeglichkeit 1 -> Sub-Klasse validieren
            //constraintViolations = validator.validate( this.subklasse, General.class );
        } else if(group == ValidationGroup.CONDITION) {
            constraintViolations = validator.validate( this, Condition.class );
        }

        if(constraintViolations != null && constraintViolations.isEmpty()) {
            /* Gueltige Eingabe */
            return true;
        } else {
            /* Ungueltige Eingabe */
            FacesContext facesContext = FacesContext.getCurrentInstance();
            Iterator<ConstraintViolation<PinboardViewModel>> iter = constraintViolations.iterator();
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
        PinboardViewModel.validator = validator;
    }

    public Long getApartmentID() {
        return apartmentID;
    }

    public void setApartmentID(Long apartmentID) {
        this.apartmentID = apartmentID;
    }

    public Note getCurrentNote() {
        return currentNote;
    }

    public void setCurrentNote(Note currentNote) {
        this.currentNote = currentNote;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isAddNote() {
        return addNote;
    }

    public void setAddNote(boolean addNote) {
        this.addNote = addNote;
    }

    public boolean isEditNote() {
        return editNote;
    }

    public void setEditNote(boolean editNote) {
        this.editNote = editNote;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public NoteCategory[] getCategories() {
        return categories;
    }

    public void setCategories(NoteCategory[] categories) {
        this.categories = categories;
    }
   
    
    
    
    
}
