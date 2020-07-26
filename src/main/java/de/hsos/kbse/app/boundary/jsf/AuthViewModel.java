/*
 * BOUNDARY CLASS AuthViewModel
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
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 *
 * @author Lucca Oberhößel
 */
@Named("authVM")
@ConversationScoped
public class AuthViewModel implements Serializable {
    
    /* ----------------------------------------- ATTRIBUTE ---------------------------------------- */
    
    /* Controller Classes */
    private final MemberRepository memberRepository;
    private final ApartmentRepository apartmentRepository;
    
    /* Conversation */
    private final Conversation conversation;
    
    /* Bean Validation API */
    private static Validator validator;
    
    /* Login Form fields */
    @NotNull(groups = {General.class, Condition.class}, message="Der Username darf nicht leer sein!")
    @Size(groups = {General.class, Condition.class}, min=3, max=50, message="Der Username muss zwischen 3 und 50 Zeichen liegen!")
    @Pattern(groups = {General.class, Condition.class}, regexp = "^[0-9A-Za-zäÄöÖüÜß\\-\\.\\s]+$", message="Der Username enthält ungültige Bezeichner!")
    private String username;
    @NotNull(groups = {General.class, Condition.class}, message="Das Passwort darf nicht leer sein!")
    private String password;
    
    /* Register Form fields */
    @NotNull(groups = {Condition.class}, message="Die Passwortwiederholung darf nicht leer sein!")
    private String repassword;
    @NotNull(groups = {Condition.class}, message="Der Name der WG darf nicht leer sein!")
    @Size(groups = {Condition.class}, min=3, max=50, message="Der Name der WG muss zwischen 3 und 50 Zeichen liegen!")
    @Pattern(groups = {Condition.class}, regexp = "^[0-9A-Za-zäÄöÖüÜß\\-\\.\\s]+$", message="Der Name der WG enthält ungültige Bezeichner!")
    private String apartmentName;
    @NotNull(groups = {Condition.class}, message="Das Geburtsdatum darf nicht leer sein!")
    @Past(groups = {Condition.class}, message="Das Geburtsdatum muss in der Vergangenheit liegen!")
    private Date birthday;
    private String color;
    
    private Member loggedInMember;
    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    @Inject
    @Logable(LogLevel.INFO)
    public AuthViewModel(MemberRepository memberRepository, Conversation conversation, ApartmentRepository apartmentRepository) {
        this.memberRepository = memberRepository;
        this.apartmentRepository = apartmentRepository;
        this.conversation = conversation;
    }
    
    @Logable(LogLevel.INFO)
    public void initConversation() {
        if (conversation.isTransient()) {
            conversation.begin();
        }
    }
    
    @Logable(LogLevel.INFO)
    public void endConversation(){
        if(!conversation.isTransient()) {
            conversation.end();
        }
    }
    
    @Logable(LogLevel.INFO)
    public String login() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        if(this.validateInput(ValidationGroup.GENERAL)) {
            try {
                Member user = memberRepository.findMemberByName(this.username);
                /* Abruf des Membes aus der Datenbank */
                if(user == null || user.getDeleted()) {                 // User exisiert nicht oder ist als geloescht markiert
                    String message = "Der Username ist falsch!";
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", message);
                    facesContext.addMessage("Error",msg);
                } else if(!user.getPassword().equals(this.password)){   // User existiert, aber falsches Passwort
                    String message = "Das Passwort ist falsch!";
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", message);
                    facesContext.addMessage("Error",msg);
                } else {                                                // Erfolgreicher Login
                    this.loggedInMember = user;
                    session.setAttribute("user", user);
                    return "pages/apartment?faces-redirect=true";
                }
            } catch(AppException ex) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
                facesContext.addMessage("Error",msg);
            }
        }
        return "";
    }
    
    @Logable(LogLevel.INFO)
    public void logout() throws IOException {
        /* Conversation beenden */
        this.endConversation();
        /* Session beenden */
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        session.invalidate();
        /* Redirect zur Startseite */
        facesContext.getExternalContext().redirect("/KBSE-SS2020-ApartmentApp/faces/login.xhtml");
    }
    
    @Logable(LogLevel.INFO)
    public String register() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if(this.validateInput(ValidationGroup.CONDITION)) { // Gueltige Eingabe
            if(password.equals(repassword)){
                    Apartment apartment = new Apartment(this.apartmentName);
                    Member member = new Member(username, MemberRole.ADMIN, password, birthday, MemberColor.RED);
                    /* Eingaben zuruecksetzen */
                    this.apartmentName = "";
                    this.username = "";
                    this.password = "";
                    this.repassword = "";
                    this.birthday = null;
                    try {
                        /* Anlegen eines neuen Apartments und Members in der Datenbank */
                        apartmentRepository.createApartment(apartment);
                        member.setApartmentID(apartment.getId());
                        memberRepository.createMember(member);
                        /* FacesMessage fuer erfolgreiches Anlegen einer Wohngemeinschaft */
                        String message = "WG #" + apartment.getId() + " erfolgreich angelegt!";
                        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Information.", message);
                        facesContext.addMessage("Information",msg);
                        return "faces/login?faces-redirect=true";
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
        return "";
    }
    
    public String discardRegistration() {
        this.apartmentName = "";
        this.username = "";
        this.password = "";
        this.repassword = "";
        this.birthday = null;
        return "login";
    }

    /* ------------------------------------- METHODEN PRIVATE ------------------------------------- */
    
    @PostConstruct
    private static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Logable(LogLevel.INFO)
    private boolean validateInput(ValidationGroup group) {
        /* Die Methode validate() gibt ein Set von ConstraintViolations zurueck, in dem alle moeglicherweise begangenen Verstoesse aufgefuehrt
         * sind. Dieses Set ist leer, falls die Eingabe gueltig ist. Durch die Gruppierung der Constraints, hier General.class, besteht die
         * Moeglichkeit, die Validation erst dann auszufuehren, wenn die entsprechende Gruppe direkt durch das Programm angesprochen wird.
         * s. https://www.baeldung.com/javax-validation-groups */
        Set<ConstraintViolation<AuthViewModel>> constraintViolations = null;
        if(group == ValidationGroup.GENERAL) {
            constraintViolations = validator.validate(this, General.class );
        } else if(group == ValidationGroup.CONDITION) {
            constraintViolations = validator.validate(this, Condition.class );
        }

        if(constraintViolations != null && constraintViolations.isEmpty()) {
            /* Gueltige Eingabe */
            return true;
        } else {
            /* Ungueltige Eingabe */
            FacesContext facesContext = FacesContext.getCurrentInstance();
            Iterator<ConstraintViolation<AuthViewModel>> iter = constraintViolations.iterator();
            while (iter.hasNext()) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Constraint.", iter.next().getMessage());
                facesContext.addMessage("Constraint Violation",msg);
            }
            return false;
        }
    }
    
    /* -------------------------------------- GETTER AND SETTER ------------------------------------ */

    public Member getLoggedInMember() {
        return loggedInMember;
    }

    public void setLoggedInMember(Member loggedInMember) {
        this.loggedInMember = loggedInMember;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getRepassword() {
        return repassword;
    }

    public void setRepassword(String repassword) {
        this.repassword = repassword;
    }

    public String getApartmentName() {
        return apartmentName;
    }

    public void setApartmentName(String apartmentName) {
        this.apartmentName = apartmentName;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

}
