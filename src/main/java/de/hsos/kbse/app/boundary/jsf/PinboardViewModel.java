/*
 * BOUNDARY CLASS CalenderViewModel
 *
 */
package de.hsos.kbse.app.boundary.jsf;

import de.hsos.kbse.app.control.Pinboard;
import de.hsos.kbse.app.entity.features.Note;
import de.hsos.kbse.app.entity.Member;
import de.hsos.kbse.app.enums.LogLevel;
import de.hsos.kbse.app.enums.MemberRole;
import de.hsos.kbse.app.enums.NoteCategory;
import de.hsos.kbse.app.enums.ValidationGroup;
import de.hsos.kbse.app.util.AppException;
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
    
    private Long apartmentID;
    private Member loggedInMember;
    private Note currentNote;
    private Note originalNote;      // Sicherung des zu bearbeitenden Notiz-Objekts
    private List<Note> notes;
    private boolean admin;          // true = ADMIN, false = USER
    private boolean addNote;        // true = addNote()
    private boolean editNote;       // true = editNote()
    private final NoteCategory[] categories;
    

    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    @Inject
    @Logable(LogLevel.INFO)
    public PinboardViewModel(Pinboard pinboard, Conversation conversation) {
        this.pinboard = pinboard;
        this.conversation = conversation;
        this.notes = new LinkedList<>();
        this.initLoggedInMember();
        this.apartmentID = this.loggedInMember.getApartmentID();
        this.initNoteList();
        this.categories = NoteCategory.values();
    }
    
    public boolean checkAccessRights(Note note) {
        /* Es muss geprueft werden, ob der jeweilige Zahlung bearbeitet oder geloescht werden kann,
         * da dies nur erlaubt ist, wenn der zugreifende Nutzer Admin oder der Verfasser ist. */
        return this.admin || note.getAuthor().getId().equals(this.loggedInMember.getId());
    }
    
    @Logable(LogLevel.INFO)
    public String addNote() {
        /* Neues Note-Objekt initiieren */
        this.currentNote = new Note(this.loggedInMember, new Date(), this.apartmentID);
        this.addNote = true;
        this.editNote = false;
        return "pinboard-add";
    }
    
    @Logable(LogLevel.INFO)
    public String editNote(Note note){
        /* Ausgewaehltes Notiz-Objekt setzen und Sicherungskopie anlegen */
        this.currentNote = note;
        this.originalNote = new Note(currentNote);
        this.editNote = true;
        this.addNote = false;
        return "pinboard-add";
    }
    
    @Logable(LogLevel.INFO)
    public void deleteNote(Note note){
        this.addNote = false;
        this.editNote = false;
        try {
            /* Bestehendes Payment-Objekt aus der Datenbank entfernen */
            this.pinboard.deleteNote(note);
            
        } catch (AppException ex) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
            facesContext.addMessage("Error",msg);
        }
        /* Bestehendes Notiz-Objekt aus der Notizen-Liste entfernen */
        removeFromNoteList(note);
    }
    
    @Logable(LogLevel.INFO)
    public String saveNote(){
        
        if(this.validateInput(ValidationGroup.GENERAL)) { // Gültige Eingabe
            
            if(this.addNote){ // Neue Notiz
                try {
                    /* Neues Notiz-Objekt der Datenbank hinzufügen */
                    this.pinboard.createNote(currentNote);
                } catch (AppException ex) {
                    FacesContext facesContext = FacesContext.getCurrentInstance();
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
                    facesContext.addMessage("Error",msg);
                }
                /* Neues Notiz-Objekt der zu Anfang initialisierten Notizen-Liste hinzufügen */
                addNoteToList(this.currentNote);
            }
            else if(this.editNote){ // Bestehende Notiz bearbeiten
                
                this.currentNote.setTimestamp(new Date());
                try {
                    /* Bestehendes Noitz-Objekt in der Datenbank updaten */
                    this.pinboard.updateNote(currentNote);
                } catch (AppException ex) {
                    FacesContext facesContext = FacesContext.getCurrentInstance();
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
                    facesContext.addMessage("Error",msg);
                }
                
                /* Bestehendes Notiz-Objekt in der zu Anfang initialisierten Notizen-Liste aktualisieren */
                updateNoteList(this.currentNote);
            }
        }else{ // Ungültige Eingabe
            return "";
        }
        
        return "pinboard";
    }
    
    @Logable(LogLevel.INFO)
    public String discardNote(){
        /* Falls ein vorhandenes Objekt bearbeitet werden sollte und diese Aktion abgebrochen wurde,
         * muss dieses auf seinen Originalzustand zurückgesetzt werden.*/
        if(this.editNote){
            this.currentNote.setMessage(this.originalNote.getMessage());           
            this.currentNote.setCategory(this.originalNote.getCategory());
            this.currentNote.setTimestamp(this.originalNote.getTimestamp());
        }
        return "pinboard";
    }
    
    @Logable(LogLevel.INFO)
    public void deleteAllNotes() {
        try {
            /* Bestehende Note-Objekte in der Datenbank loeschen */
            this.pinboard.deleteAllNotesFrom(this.apartmentID);
        } catch(AppException ex) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
            facesContext.addMessage("Error",msg);
        }
    }
    
    /* ------------------------------------- METHODEN PRIVATE ------------------------------------- */
    
    @PostConstruct
    private static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    private void initLoggedInMember() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);

        this.loggedInMember = (Member) session.getAttribute("user");
        if(this.loggedInMember.getMemberRole() == MemberRole.ADMIN) {
            this.admin = true;
        }
    }
    
    private void initNoteList() {
        try {
            List<Note> tmp = this.pinboard.getAllNotesFrom(apartmentID);
            /* Einträge der letzten 7 Tage der Notizen-Liste werden hinzugefügt */
            Date lastWeek = java.sql.Date.valueOf(LocalDate.now().minusDays(7));
            tmp.forEach((note) -> {
                if(note.getTimestamp().after(lastWeek)){
                    this.notes.add(note);
                }   
            });
            /* Absteigende Sortierung der Notizen anhand des Datums. Notizen mit Status "URGENT" werden an oberster Stelle einsortiert.*/
            Collections.sort(this.notes);
            Collections.reverse(this.notes);
        } catch(AppException ex) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
            facesContext.addMessage("Error",msg);
        }
    }
    
    private void removeFromNoteList(Note n){
        /* Das Notiz-Objekt wird aus der Notizen-Liste entfernt */
        for(int i = 0; i < this.notes.size(); i++){
            if(this.notes.get(i).equals(n)){
                this.notes.remove(i);
            }
        }
    }
    
    private void updateNoteList(Note n){
        /* Das Notiz-Objekt innerhalb der Notizen-Liste wird aktualisiert */
        this.notes.forEach((note) -> {
            if(note.getId().equals(n.getId())){
                note = n;
            }
        });
        
        /* Die Notizen-List wird erneut sortiert */
        Collections.sort(this.notes);
        Collections.reverse(this.notes);
    }
    
    private void addNoteToList(Note n){
        /* Ein Noitz-Objekt wird der Notizen-Liste hinzugefügt */
        this.notes.add(n);
        
        /* Die Notizen-List wird erneut sortiert und das Notiz-Objekt somit an die richtige Stelle bewegt */
        Collections.sort(notes);
        Collections.reverse(notes);
    }
    
    @Logable(LogLevel.INFO)
    private boolean validateInput(ValidationGroup group) {
        /* Die Methode validate() gibt ein Set von ConstraintViolations zurueck, in dem alle moeglicherweise begangenen Verstoesse aufgefuehrt
         * sind. Dieses Set ist leer, falls die Eingabe gueltig ist. Durch die Gruppierung der Constraints, hier General.class, besteht die
         * Moeglichkeit, die Validation erst dann auszufuehren, wenn die entsprechende Gruppe direkt durch das Programm angesprochen wird.
         * s. https://www.baeldung.com/javax-validation-groups */
        Set<ConstraintViolation<Note>> constraintViolations = null;
        if(group == ValidationGroup.GENERAL) {
            constraintViolations = validator.validate( this.currentNote, General.class );
        }

        if(constraintViolations != null && constraintViolations.isEmpty()) {
            /* Gueltige Eingabe */
            return true;
        } else {
            /* Ungueltige Eingabe */
            FacesContext facesContext = FacesContext.getCurrentInstance();
            Iterator<ConstraintViolation<Note>> iter = constraintViolations.iterator();
            while (iter.hasNext()) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Constraint.", iter.next().getMessage());
                facesContext.addMessage("Constraint Violation",msg);
            }
            return false;
        }
    }
    
    /* -------------------------------------- GETTER AND SETTER ------------------------------------ */

    public Note getCurrentNote() {
        return currentNote;
    }

    public boolean isAddNote() {
        return addNote;
    }

    public boolean isEditNote() {
        return editNote;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public NoteCategory[] getCategories() {
        return categories;
    }

}
