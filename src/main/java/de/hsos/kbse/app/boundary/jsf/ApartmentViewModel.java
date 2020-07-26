/*
 * BOUNDARY CLASS ApartmentViewModel
 *
 */
package de.hsos.kbse.app.boundary.jsf;

import de.hsos.kbse.app.control.ApartmentRepository;
import de.hsos.kbse.app.control.MemberRepository;
import de.hsos.kbse.app.entity.Apartment;
import de.hsos.kbse.app.entity.member.Member;
import de.hsos.kbse.app.enums.LogLevel;
import de.hsos.kbse.app.enums.MemberColor;
import de.hsos.kbse.app.enums.MemberRole;
import de.hsos.kbse.app.enums.ValidationGroup;
import de.hsos.kbse.app.util.AppException;
import de.hsos.kbse.app.util.Condition;
import de.hsos.kbse.app.util.General;
import de.hsos.kbse.app.util.Logable;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Iterator;
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
@Named("apartmentVM")
@ConversationScoped
public class ApartmentViewModel implements Serializable {
    
    /* ----------------------------------------- ATTRIBUTE ---------------------------------------- */
    
    /* Controller Classes */
    private final ApartmentRepository repository;
    private final MemberRepository memberRepository;
    
    /* Conversation */
    private final Conversation conversation;
    
    /* Bean Validation API */
    private static Validator validator;
    
    private Apartment apartment;
    private List<Member> members;
    private Member loggedInMember;
    private Member currentMember;
    private Member originalMember;  // Sicherung des zu bearbeitenden Member-Objekts
    private final MemberColor[] colors;
    private final MemberRole[] memberRoles;
    private boolean addMember;      // true = addMember()
    private boolean editMember;     // true = editMember()
    private boolean changePassword; // true = changePassword()
    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    @Inject
    @Logable(LogLevel.INFO)
    public ApartmentViewModel(ApartmentRepository repository, MemberRepository memberRepository, Conversation conversation) {
        this.repository = repository;
        this.memberRepository = memberRepository;
        this.conversation = conversation;
        this.colors = MemberColor.values();
        this.memberRoles = MemberRole.values();
        this.initLoggedInMember();
        System.out.println("Eingeloggt: " + this.loggedInMember.getName());
        this.initApartmentByID(this.loggedInMember.getApartmentID());
        System.out.println("ApartmentID: " + this.apartment.getId());
        System.out.println("Member: " + this.members.size());
    }
    
    public boolean checkAccessRights(Member member) {
        /* Es muss geprueft werden, ob das jeweilige Mitglied bearbeitet oder geloescht werden kann,
         * da dies nur erlaubt ist, wenn der zugreifende Nutzer Admin ist oder seine eigenen Daten
         * bearbeiten moechte. */
        return this.loggedInMember.getMemberRole().equals(MemberRole.ADMIN) || member.getId().equals(this.loggedInMember.getId());
    }
    
    public boolean isAdmin(){
        return this.loggedInMember.getMemberRole().equals(MemberRole.ADMIN);
    }
    
    public boolean isLoggedInMember(Member member) {
        return this.loggedInMember.getId().equals(member.getId());
    }
    
    public String addMember() {
        /* Initialisierung eines neuen Member-Objekts */
        this.currentMember = new Member(MemberRole.USER, this.apartment.getId());
        /* Berechnung der naechsten Mitglieds-Farbe anhand der Anzahl der Mitglieder */
        int amountMembers = this.members.size();
        int colorIndex = amountMembers % MemberColor.values().length;
        MemberColor color = this.colors[colorIndex];
        this.currentMember.getDetails().setColor(color);
        this.addMember = true;
        this.editMember = false;
        this.changePassword = false;
        return "members-add";
    }
    
    public String editMember(Member member) {
        /* Ausgewaehltes Member-Objekt setzen und Sicherungskopie anlegen */
        this.currentMember = member;
        this.currentMember.setRepassword(this.currentMember.getPassword());
        this.originalMember = new Member(this.currentMember);
        this.editMember = true;
        this.addMember = false;
        this.changePassword = false;
        return "members-add";
    }
    
    public String changePassword(Member member) {
        System.out.println("Change Password");
        this.currentMember = this.loggedInMember;
        this.currentMember.setPassword("");
        this.currentMember.setRepassword("");
        this.originalMember = new Member(this.loggedInMember);
        this.changePassword = true;
        this.addMember = false;
        this.editMember = false;
        return "member-password";
    }
    
    public String deleteMember(Member member) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        this.currentMember = member;
        try {
            /* Bestehendes Mitglied wird auch beim Loeschen in der Datenbank geupdaten und der deleted-Status
             * auf true gesetzt. So wird verhindert, dass ein kaskadierendes Loeschen von Eintraegen notwendig
             * ist und von dem Mitglied erstellte Beitraege erhalten bleiben. */
            currentMember.setDeleted(true);
            this.memberRepository.updateMember(currentMember);
            /* Entfernen des Members aus der Members-List */
            this.members.remove(this.currentMember);
            String message = "Mitglied erfolgreich geloescht!";
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Information.", message);
            return "members";
        } catch (AppException ex) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
            facesContext.addMessage("Error",msg);
        }
        return "";
    }
    
    public String saveMember() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if(this.validateInput(ValidationGroup.CONDITION)) { // Gueltige Eingabe
            if(this.addMember){  // Member hinzufuegen
                /* Ueberpruefung, ob der Name innerhalb der WG schon vorhanden ist! */
                for(int i = 0; i < this.members.size(); i++) {
                    Member tempMember = this.members.get(i);
                    if(this.currentMember.getName().equals(tempMember.getName())) {
                        String message = "Der Name ist in der WG bereits vorhanden!";
                        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Constraint.", message);
                        facesContext.addMessage("Constraint",msg);
                        return "";
                    }
                }
                if(this.currentMember.getPassword().equals(this.currentMember.getRepassword())){
                    try {
                        /* Anlegen eines neuen Members in der Datenbank */
                        this.currentMember.getDetails().setCashBalance(new BigDecimal(0));
                        this.memberRepository.createMember(this.currentMember);
                        /* Hinzufuegen des neuen Members zur Members-List */
                        this.members.add(this.currentMember);
                        /* FacesMessage fuer erfolgreiches Anlegen eines neuen Mitglieds */
                        String message = "Mitglied erfolgreich angelegt!";
                        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Information.", message);
                        facesContext.addMessage("Information",msg);
                        return "members";
                    } catch (AppException ex) {
                        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
                        facesContext.addMessage("Error",msg);
                    }
                } else {
                    String message = "Die Passwörter müssen identisch sein!";
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Constraint.", message);
                    facesContext.addMessage("Constraint",msg);
                }
            } else if(this.editMember) {    // Bestehenden Member updaten
                try {
                    /* Bestehendes Mitglied in der Datenbank updaten */
                    this.memberRepository.updateMember(this.currentMember);
                    /* FacesMessage fuer erfolgreiches Updates eines Mitglieds */
                    String message = "Mitglied erfolgreich aktualisiert!";
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Information.", message);
                    facesContext.addMessage("Information",msg);
                    return "members";
                } catch (AppException ex) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
                    facesContext.addMessage("Error",msg);
                }
            } else if(this.changePassword) {    // Passwort des eingeloggten Mitglieds aendern
                if(this.currentMember.getPassword().equals(this.currentMember.getRepassword())){
                    try {
                        /* Eingeloggtes Mitglied in der Datenbank updaten */
                        this.memberRepository.updateMember(this.currentMember);
                        String message = "Passwort erfolgreich aktualisiert!";
                        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Information.", message);
                        facesContext.addMessage("Information",msg);
                        return "members";
                    } catch (AppException ex) {
                        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
                        facesContext.addMessage("Error",msg);
                    }
                } else {
                    String message = "Die Passwörter müssen identisch sein!";
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Constraint.", message);
                    facesContext.addMessage("Constraint",msg);
                }
            }
        }
        return "";
    }
    
    public String discard() {
        if(this.editMember || this.changePassword) {
            this.currentMember.setName(this.originalMember.getName());
            this.currentMember.getDetails().setBirthday(this.originalMember.getDetails().getBirthday());
            this.currentMember.getDetails().setColor(this.originalMember.getDetails().getColor());
            this.currentMember.setMemberRole(this.originalMember.getMemberRole());
        }
        return "members";
    }
    
    /* ------------------------------------- METHODEN PRIVATE ------------------------------------- */
    
    @PostConstruct
    private void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    private void initLoggedInMember() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
        this.loggedInMember = (Member) session.getAttribute("user");
    }
    
    private void initApartmentByID(Long id) {
        try {
            this.apartment = this.repository.findApartment(id);
            this.members = this.memberRepository.getAllMembersFrom(id);
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
         * Moeglichkeit, die Validation erst dann auszufuehren, wenn die entsprechende Gruppe direkt durch das Programm angesprochen wird. */
        Set<ConstraintViolation<Member>> constraintViolations = null;
        if(group == ValidationGroup.GENERAL) {
            constraintViolations = validator.validate(this.currentMember, General.class );
        } else if(group == ValidationGroup.CONDITION) {
            constraintViolations = validator.validate(this.currentMember, Condition.class );
        }
        if(constraintViolations != null && constraintViolations.isEmpty()) {
            /* Gueltige Eingabe */
            return true;
        } else {
            /* Ungueltige Eingabe */
            FacesContext facesContext = FacesContext.getCurrentInstance();
            Iterator<ConstraintViolation<Member>> iter = constraintViolations.iterator();
            while (iter.hasNext()) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Constraint.", iter.next().getMessage());
                facesContext.addMessage("Constraint Violation",msg);
            }
            return false;
        }
    }
    
    /* -------------------------------------- GETTER AND SETTER ------------------------------------ */

    public List<Member> getMembers() {
        return members;
    }

    public Member getCurrentMember() {
        return currentMember;
    }

    public void setCurrentMember(Member currentMember) {
        this.currentMember = currentMember;
    }
    
    public boolean getAddMember() {
        return addMember;
    }

    public boolean getEditMember() {
        return editMember;
    }
    
    public MemberColor[] getColors() {
        return colors;
    }

    public MemberRole[] getRoles() {
        return memberRoles;
    }

}
