/*
 * BOUNDARY CLASS AuthViewModel
 *
 */
package de.hsos.kbse.app.boundary.jsf;

import de.hsos.kbse.app.control.ApartmentRepository;
import de.hsos.kbse.app.control.MemberRepository;
import de.hsos.kbse.app.entity.Apartment;
import de.hsos.kbse.app.entity.Member;
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
import java.math.BigDecimal;
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

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel
 */
@Named("authVM")
@ConversationScoped
public class AuthViewModel implements Serializable {
    
    /* ----------------------------------------- ATTRIBUTE ---------------------------------------- */
    
    /* Controller Classes */
    private final ApartmentRepository repository;
    private final MemberRepository memberRepository;
    
    /* Conversation */
    private final Conversation conversation;
    
    /* Bean Validation API */
    private static Validator validator;
    
    private Apartment currentApartment;
    private Member currentMember;
    private Member loggedInMember;
    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    @Inject
    @Logable(LogLevel.INFO)
    public AuthViewModel(MemberRepository memberRepository, Conversation conversation, ApartmentRepository repository) {
        this.repository = repository;
        this.memberRepository = memberRepository;
        this.conversation = conversation;
        this.currentApartment = new Apartment();
        this.currentMember = new Member();
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
                /* Abruf des Membes aus der Datenbank */
                Member user = memberRepository.findMemberByName(this.currentApartment.getId(), this.currentMember.getName());
                if(user == null || user.getDeleted()) {                 // User exisiert nicht oder ist als geloescht markiert
                    String message = "Die WG-ID oder der Username ist falsch!";
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", message);
                    facesContext.addMessage("Error",msg);
                } else if(!user.getPassword().equals(this.currentMember.getPassword())){   // User existiert, aber falsches Passwort
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
    public String addApartment() {
        this.currentApartment = new Apartment();
        this.currentMember = new Member();
        return "register";
    }
    
    @Logable(LogLevel.INFO)
    public String register() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if(this.validateInput(ValidationGroup.CONDITION)) { // Gueltige Eingabe
            if(this.currentMember.getPassword().equals(this.currentMember.getRepassword())){
                try {
                    /* Anlegen eines neuen Apartments und Members in der Datenbank */
                    repository.createApartment(this.currentApartment);
                    this.currentMember.setMemberRole(MemberRole.ADMIN);
                    this.currentMember.setApartmentID(this.currentApartment.getId());
                    this.currentMember.getDetails().setColor(MemberColor.RED);
                    this.currentMember.getDetails().setCashBalance(new BigDecimal(0));
                    memberRepository.createMember(this.currentMember);
                    /* FacesMessage fuer erfolgreiches Anlegen einer Wohngemeinschaft */
                    String message = "WG #" + this.currentApartment.getId() + " erfolgreich angelegt!";
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
        this.currentApartment = new Apartment();
        this.currentMember = new Member();
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
         * Moeglichkeit, die Validation erst dann auszufuehren, wenn die entsprechende Gruppe direkt durch das Programm angesprochen wird. */
        Set<ConstraintViolation<Apartment>> constraintViolations = null;
        Set<ConstraintViolation<Member>> constraintViolationsM = null;
        if(group == ValidationGroup.GENERAL) {
            constraintViolationsM = validator.validate(this.currentMember, General.class );
        } else if(group == ValidationGroup.CONDITION) {
            constraintViolations = validator.validate(this.currentApartment, Condition.class );
            constraintViolationsM = validator.validate(this.currentMember, Condition.class );
            
            if(constraintViolations.size() > 0) {
                /* Ungueltige Eingabe fuer Apartment */
                FacesContext facesContext = FacesContext.getCurrentInstance();
                Iterator<ConstraintViolation<Apartment>> iter = constraintViolations.iterator();
                while (iter.hasNext()) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Constraint.", iter.next().getMessage());
                    facesContext.addMessage("Constraint Violation",msg);
                }
                return false;
            }
        }
        
        if(constraintViolationsM != null && constraintViolationsM.isEmpty()) {
            /* Gueltige Eingabe fuer Member */
            return true;
        } else {
            /* Ungueltige Eingabe */
            FacesContext facesContext = FacesContext.getCurrentInstance();
            Iterator<ConstraintViolation<Member>> iter = constraintViolationsM.iterator();
            while (iter.hasNext()) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Constraint.", iter.next().getMessage());
                facesContext.addMessage("Constraint Violation",msg);
            }
            return false;
        }
    }
    
    /* -------------------------------------- GETTER AND SETTER ------------------------------------ */

    public Apartment getCurrentApartment() {
        return currentApartment;
    }

    public void setCurrentApartment(Apartment currentApartment) {
        this.currentApartment = currentApartment;
    }

    public Member getCurrentMember() {
        return currentMember;
    }

    public void setCurrentMember(Member currentMember) {
        this.currentMember = currentMember;
    }
    
    public Member getLoggedInMember() {
        return loggedInMember;
    }

}
