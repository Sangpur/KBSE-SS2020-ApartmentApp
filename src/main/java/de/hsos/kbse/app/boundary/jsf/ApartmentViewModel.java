/*
 * BOUNDARY CLASS ApartmentViewModel
 *
 */
package de.hsos.kbse.app.boundary.jsf;

import de.hsos.kbse.app.control.ApartmentRepository;
import de.hsos.kbse.app.control.MemberRepository;
import de.hsos.kbse.app.entity.Apartment;
import de.hsos.kbse.app.entity.member.Member;
import de.hsos.kbse.app.entity.member.MemberDetail;
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
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
    @Valid
    private Member currentMember = null;
    @NotNull(groups = {General.class, Condition.class}, message="Das Passwort darf nicht leer sein!")
    @Size(groups = {General.class, Condition.class}, min=3, max=50, message="Das Passwort muss zwischen 3 und 50 Zeichen liegen!")
    private String newpassword = "";
    @NotNull(groups = {General.class, Condition.class}, message="Die Passwort-Wiederholung darf nicht leer sein!")
    private String repassword = "";
    private Boolean editMode = false;
    private MemberColor[] colors;
    private MemberRole[] roles = {MemberRole.ADMIN, MemberRole.USER};
    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    @Inject
    @Logable(LogLevel.INFO)
    public ApartmentViewModel(ApartmentRepository repository, MemberRepository memberRepository, Conversation conversation) {
        this.repository = repository;
        this.memberRepository = memberRepository;
        this.conversation = conversation;
        this.colors = MemberColor.values();
        this.initApartmentByID(getLoggedInMember().getApartmentID()); // TODO: Bei Login-Prozess entsprechende ID setzen
    }
    
    
    @PostConstruct
    public void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    public String addMember() {
        this.currentMember = new Member();
        this.repassword = "";
        this.editMode = false;
        this.currentMember.setApartmentID(apartment.getId());
        this.currentMember.setMemberRole(MemberRole.USER);
        /* Berechnung der naechsten Mitglieds-Farbe anhand der Anzahl der Mitglieder */
        int amountMembers = this.members.size();
        int colorIndex = amountMembers % MemberColor.values().length;
        MemberColor color = this.colors[colorIndex];
        this.currentMember.getDetails().setColor(color);
        return "members-add";
    }
    
    public String editMember(Member member) {
        /*this.currentMember = members.stream().filter(x -> x.getId().equals(id)).findFirst().orElse(null);
        if(currentMember == null){
            FacesContext.getCurrentInstance().addMessage("Error", new FacesMessage("Member with id " + id + "could not be found."));
            return "";
        }*/
        this.currentMember = member;
        this.newpassword = "";
        this.repassword = "";
        this.editMode = true;
        return "members-add";
    }
    
    public String changePassword(Member member) {
        System.out.println("Change Password");
        return "";
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
            initApartmentByID(apartment.getId());
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
        if(this.validateInput(ValidationGroup.GENERAL)) { // Gueltige Eingabe
            if(!editMode){  // Member hinzufuegen
                if(currentMember.getPassword().equals(this.repassword)){
                    try {
                        /* Anlegen eines neuen Members in der Datenbank */
                        currentMember.getDetails().setCashBalance(new BigDecimal(0));
                        this.memberRepository.createMember(currentMember);
                        /* FacesMessage fuer erfolgreiches Anlegen eines neuen Mitglieds */
                        String message = "Mitglied erfolgreich angelegt!";
                        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Information.", message);
                        facesContext.addMessage("Information",msg);
                        return resetMember();
                    } catch (AppException ex) {
                        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
                        facesContext.addMessage("Error",msg);
                    }
                } else {
                    String message = "Die Passwörter müssen identisch sein!";
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Constraint.", message);
                    facesContext.addMessage("Constraint",msg);
                }
            } else {        // Bestehenden Member updaten
                if(newpassword.equals(this.repassword)){
                    currentMember.setPassword(newpassword);
                    try {
                        /* Bestehendes Mitglied in der Datenbank updaten */
                        this.memberRepository.updateMember(currentMember);
                        /* FacesMessage fuer erfolgreiches Updates eines Mitglieds */
                        String message = "Mitglied erfolgreich aktualisiert!";
                        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Information.", message);
                        facesContext.addMessage("Information",msg);
                        return resetMember();
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
    
    public String resetMember() {
        this.currentMember = null;
        this.newpassword = "";
        this.repassword = "";
        this.editMode = false;
        return "members";
    }
    
    public Boolean isAllowedToChangeMember(Long id){
        if(getLoggedInMember().getMemberRole().equals(MemberRole.ADMIN))
            return true;
        this.currentMember = members.stream().filter(x -> x.getId().equals(id)).findFirst().orElse(null);
        if(currentMember == null){
            FacesContext.getCurrentInstance().addMessage("Error", new FacesMessage("Member with id " + id + "could not be found."));
            return false;
        }
        return currentMember.getId().equals(getLoggedInMember().getId());
    }
    
    public Boolean isAdmin(){
        return getLoggedInMember().getMemberRole().equals(MemberRole.ADMIN);
    }
    
    public Boolean isLoggedInMember(Member member) {
        return this.getLoggedInMember().getId().equals(member.getId());
    }
    
    /* ------------------------------------- METHODEN PRIVATE ------------------------------------- */
    
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
         * Moeglichkeit, die Validation erst dann auszufuehren, wenn die entsprechende Gruppe direkt durch das Programm angesprochen wird.
         * s. https://www.baeldung.com/javax-validation-groups */
        Set<ConstraintViolation<ApartmentViewModel>> constraintViolations = null;
        if(group == ValidationGroup.GENERAL) {
            constraintViolations = validator.validate(this, General.class );
        }
        if(constraintViolations != null && constraintViolations.isEmpty()) {
            /* Gueltige Eingabe */
            return true;
        } else {
            /* Ungueltige Eingabe */
            FacesContext facesContext = FacesContext.getCurrentInstance();
            Iterator<ConstraintViolation<ApartmentViewModel>> iter = constraintViolations.iterator();
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

    public Member getLoggedInMember() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);

        Member loggedInMember = (Member) session.getAttribute("user");
        return loggedInMember;
    }

    public Member getCurrentMember() {
        return currentMember;
    }

    public void setCurrentMember(Member currentMember) {
        this.currentMember = currentMember;
    }

    public String getRepassword() {
        return repassword;
    }

    public void setRepassword(String repassword) {
        this.repassword = repassword;
    }

    public String getNewpassword() {
        return newpassword;
    }

    public void setNewpassword(String newpassword) {
        this.newpassword = newpassword;
    }

    public MemberColor[] getColors() {
        return colors;
    }

    public MemberRole[] getRoles() {
        return roles;
    }

    public Boolean getEditMode() {
        return editMode;
    }

    public void setEditMode(Boolean editMode) {
        this.editMode = editMode;
    }
}
