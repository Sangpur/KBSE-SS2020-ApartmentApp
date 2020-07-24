/*
 * BOUNDARY CLASS CashFlowViewModel
 *
 */
package de.hsos.kbse.app.boundary.jsf;

import de.hsos.kbse.app.control.CashFlow;
import de.hsos.kbse.app.control.MemberRepository;
import de.hsos.kbse.app.entity.features.Payment;
import de.hsos.kbse.app.entity.member.Member;
import de.hsos.kbse.app.enums.LogLevel;
import de.hsos.kbse.app.enums.MemberRole;
import de.hsos.kbse.app.enums.ValidationGroup;
import de.hsos.kbse.app.util.AppException;
import de.hsos.kbse.app.util.General;
import de.hsos.kbse.app.util.Logable;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
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
@Named("cashflowVM")
@ConversationScoped
public class CashFlowViewModel implements Serializable {
    
    /* ----------------------------------------- ATTRIBUTE ---------------------------------------- */
    
    /* Controller Classes */
    private final CashFlow cashflow;
    private final MemberRepository memberRepository;
    
    /* Injected Conversation */
    private final Conversation conversation;
    
    /* Bean Validation API */
    private static Validator validator;
    
    private Long apartmentID = 1000L; // TODO: Hinzufuegen sobald Scope gestartet wird bei Login-Prozess
    private List<Payment> payments;
    private List<Member> members;
    private Member loggedInMember;
    private boolean admin;              // true = ADMIN, false = USER
    private Payment currentPayment;
    private Payment originalPayment;    // Sicherung des zu bearbeitenden Payment-Objekts
    private boolean addPayment;         // true = addPayment()
    private boolean editPayment;        // true = editPayment()
    private boolean deletePayment;      // true = deletePayment();
    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    @Inject
    @Logable(LogLevel.INFO)
    public CashFlowViewModel(CashFlow cashflow, MemberRepository memberRepository, Conversation conversation) {
        this.cashflow = cashflow;
        this.memberRepository = memberRepository;
        this.conversation = conversation;
        this.payments = new LinkedList(); // Hier kann das neuste Element an Position 0 eingefuegt werden
        this.initPaymentsList();
        this.initMemberList();
        this.initLoggedInMember();
    }
    
    @PostConstruct
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    public boolean checkBalance(float sum) {
        return sum >= 0;
    }
    
    public boolean checkBalancePositive(float sum) {
        return sum > 0;
    }
    
    public boolean checkAccessRights(Payment payment) {
        /* Es muss geprueft werden, ob die jeweilige Zahlung bearbeitet oder geloescht werden kann,
         * da dies nur erlaubt ist, wenn der zugreifende Nutzer Admin oder der Verfasser ist. */
        return this.admin || payment.getGiver().getId().equals(this.loggedInMember.getId());
    }
    
    @Logable(LogLevel.INFO)
    public String addPayment() {
        /* Neues Payment-Objekt initiieren */
        this.currentPayment = new Payment(this.loggedInMember, new Date(), this.apartmentID);
        this.addPayment = true;
        this.editPayment = false;
        this.deletePayment = false;
        return "cashflow-add";
    }
    
    @Logable(LogLevel.INFO)
    public String editPayment(Payment payment) {
        /* Ausgewaehltes Payment-Objekt setzen und Sicherungskopie anlegen */
        this.currentPayment = payment;
        this.originalPayment = new Payment(payment);
        this.editPayment = true;
        this.addPayment = false;
        this.deletePayment = false;
        return "cashflow-add";
    }
    
    @Logable(LogLevel.INFO)
    public void deletePayment(Payment payment) {
        this.deletePayment = true;
        this.addPayment = false;
        this.editPayment = false;
        try {
            /* Bestehendes Payment-Objekt aus der Datenbank entfernen */
            this.cashflow.deletePayment(payment);
        } catch(AppException ex) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
            facesContext.addMessage("Error",msg);
        }
        /* Bestehendes Payment-Objekt aus der Payments-Liste entfernen */
        this.payments.remove(payment);
        /* Neue Balance für die Mitglieder berechnen */
        this.calculateBalance(payment);
    }
    
    @Logable(LogLevel.INFO)
    public String savePayment() {
        if(this.validateInput(ValidationGroup.GENERAL)) {   // Gueltige Eingabe
            if(this.addPayment) {   // Zahlung hinzufuegen
                try {
                    /* Neues Payment-Objekt der Datenbank hinzufügen */
                    this.cashflow.createPayment(this.currentPayment);
                } catch(AppException ex) {
                    FacesContext facesContext = FacesContext.getCurrentInstance();
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
                    facesContext.addMessage("Error",msg);
                }
                /* Neues Payment-Objekt der zu Anfang initialisierten Payments-Liste hinzufügen */
                this.payments.add(0, this.currentPayment);
                /* Neue Balance für die Mitglieder berechnen */
                this.calculateBalance(this.currentPayment);
            }
            if(this.editPayment) {  // Bestehende Zahlung updaten
                try {
                    /* Bestehendes Payment-Objekt in der Datenbank updaten */
                    this.cashflow.updatePayment(this.currentPayment);
                } catch(AppException ex) {
                    FacesContext facesContext = FacesContext.getCurrentInstance();
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
                    facesContext.addMessage("Error",msg);
                }
                /* Neue Balance für die Mitglieder berechnen */
                this.calculateBalance(this.currentPayment);
            }
        } else {    // Ungueltige Eingabe
            return "";
        }
        return "cashflow";
    }
    
    @Logable(LogLevel.INFO)
    public String discardPayment() {
        if(this.editPayment) {
            /* Falls ein vorhandenes Objekt bearbeitet werden sollte und diese Aktion abgebrochen wurde,
             * muss dieses auf seinen Originalzustand zurückgesetzt werden.*/
            this.currentPayment.setDescription(this.originalPayment.getDescription());
            this.currentPayment.setInvolvedMembers(this.originalPayment.getInvolvedMembers());
            this.currentPayment.setSum(this.originalPayment.getSum());
        }
        return "cashflow";
    }
    
    /* ------------------------------------- METHODEN PRIVATE ------------------------------------- */
    
     private void initPaymentsList() {
        try {
            this.payments = this.cashflow.getAllPaymentsFrom(apartmentID);
            /* Absteigende Sortierung der Zahlungen anhand des Datums */
            Collections.sort(this.payments);
            Collections.reverse(this.payments);
        } catch(AppException ex) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
            facesContext.addMessage("Error",msg);
        }
    }
     
    private void initMemberList() {
        try {
            this.members = this.memberRepository.getAllMembersFrom(apartmentID);
        } catch(AppException ex) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
            facesContext.addMessage("Error",msg);
        }
    }
    
    private void initLoggedInMember() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);

        this.loggedInMember = (Member) session.getAttribute("user");
        if(this.loggedInMember.getMemberRole() == MemberRole.ADMIN) {
            this.admin = true;
        }
    }
    
    private Member findMemberByID(Long id) {
        /* Auffinden eines Mitglieds mit der uebergebenen ID in der Members-Liste */
        for(int i = 0; i < this.members.size(); i++) {
            Member tempMember = this.members.get(i);
            if(tempMember.getId().equals(id)) {
                return tempMember;
            }
        }
        return null;
    }
    
    private void calculateBalance(Payment payment) {
        /* Die Member-Objekte in dem Payment-Objekt sollen nicht innerhalb des Payment-Objekts 
         * geupdatet werden, sondern in der Members-Liste und der Datenbank, da diese sonst 
         * moeglicherweise nicht auf dem aktuellen Stand bzgl. der CashBalance sind. */
        Member giver = this.findMemberByID(payment.getGiver().getId());
        int amountMembers = payment.getInvolvedMembers().size();
        List<Member> involvedMembers = new ArrayList();
        for(int i = 0; i < amountMembers; i++) {
            Long tempID = payment.getInvolvedMembers().get(i).getId();
            Member tempMember = this.findMemberByID(tempID);
            involvedMembers.add(tempMember);
        }
        BigDecimal balanceTotal = payment.getSum();
        BigDecimal balancePerMember = balanceTotal.divide(BigDecimal.valueOf(amountMembers));
        boolean isGiverInvolved = false;
        
        if(this.deletePayment | this.editPayment) {
            /* Bei dem Loeschen eines Payment-Objekts ist der nachfolgende Schritt obsolet. Bei der
             * Editierung ist dieser jedoch notwendig, da sich die Anzahl der involvierten Mitglieder
             * und alle weiteren Angaben nach der Editierung geaendert haben koennen. In diesem Fall
             * muss aber das vorherige Payment-Objekt entfernt und im Folgenden das neue hinzugefuegt 
             * werden. */
            List<Member> involvedMembersForReverse = involvedMembers;
            int amountMembersForReverse = amountMembers;
            BigDecimal balanceTotalForReverse = balanceTotal;
            BigDecimal balancePerMemberForReverse = balancePerMember;
            
            if(this.editPayment) {
                involvedMembersForReverse.clear();
                amountMembersForReverse = this.originalPayment.getInvolvedMembers().size();
                for(int i = 0; i < amountMembersForReverse; i++) {
                    Long tempID = this.originalPayment.getInvolvedMembers().get(i).getId();
                    Member tempMember = this.findMemberByID(tempID);
                    involvedMembersForReverse.add(tempMember);
                }
                balanceTotalForReverse = this.originalPayment.getSum();
                balancePerMemberForReverse = balanceTotalForReverse.divide(BigDecimal.valueOf(amountMembersForReverse));
            } 
            
            /* Der gezahlte Gesamtbetrag wird hier beim Geldgebener wieder abgezogen und bei 
             * den involvierten Mitgliedern hinzugefuegt. */
            giver.getDetails().subtractCashBalance(balanceTotalForReverse);
            for(int i = 0; i < amountMembersForReverse; i++) {
                Member tempMember = involvedMembersForReverse.get(i);
                tempMember.getDetails().addCashBalance(balancePerMemberForReverse);
                if(tempMember.getId().equals(giver.getId())) {
                    isGiverInvolved = true;
                }
            }
        }
        
        if(this.addPayment | this.editPayment) {
            /* Dem Geldgeber wird der gesamte Betrag positiv angerechnet. Falls dieser sich aber 
             * ebenfalls in der Liste der involvierten Mitgiedern befindet, wird in der folgenden 
             * Schleife der Betrag, den er dementsprechend nur fuer sich bezahlt hat, 
             * von seiner Cashbalance angezogen. */
            giver.getDetails().addCashBalance(balanceTotal);
            for(int i = 0; i < amountMembers; i++) {
                Member tempMember = involvedMembers.get(i);
                tempMember.getDetails().subtractCashBalance(balancePerMember);
                if(tempMember.getId().equals(giver.getId())) {
                    isGiverInvolved = true;
                }
            }
        }
        if(!isGiverInvolved) {
            /* Falls der Geldgeber nicht zu den involvierten Mitgliedern gehoert, wird dieses Mitglied
             * einer Kopie der Liste hinzugefuegt, damit alle in der Liste enthaltenen Mitglieder 
             * in der Datenbank geupdatet werden koennen. */
            involvedMembers.add(giver);
        } 
        this.updateCashbalanceInDatabase(involvedMembers);
    }
    
    private void updateCashbalanceInDatabase(List<Member> members) {
        for(int i = 0; i < members.size(); i++) {
            Member tempMember = members.get(i);
            try {
                this.memberRepository.updateMember(tempMember);
            } catch(AppException ex) {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
                facesContext.addMessage("Error",msg);
            }
        }
    }
    
    @Logable(LogLevel.INFO)
    private boolean validateInput(ValidationGroup group) {
        /* Die Methode validate() gibt ein Set von ConstraintViolations zurueck, in dem alle moeglicherweise begangenen Verstoesse aufgefuehrt
         * sind. Dieses Set ist leer, falls die Eingabe gueltig ist. Durch die Gruppierung der Constraints, hier General.class, besteht die
         * Moeglichkeit, die Validation erst dann auszufuehren, wenn die entsprechende Gruppe direkt durch das Programm angesprochen wird.
         * s. https://www.baeldung.com/javax-validation-groups */
        Set<ConstraintViolation<Payment>> constraintViolations = null;
        if(group == ValidationGroup.GENERAL) {
            constraintViolations = validator.validate( this.currentPayment, General.class );
        }

        if(constraintViolations != null && constraintViolations.isEmpty()) {
            /* Gueltige Eingabe */
            return true;
        } else {
            /* Ungueltige Eingabe */
            FacesContext facesContext = FacesContext.getCurrentInstance();
            Iterator<ConstraintViolation<Payment>> iter = constraintViolations.iterator();
            while (iter.hasNext()) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Constraint.", iter.next().getMessage());
                facesContext.addMessage("Constraint Violation",msg);
            }
            return false;
        }
    }
    
    /* -------------------------------------- GETTER AND SETTER ------------------------------------ */

    public void setApartmentID(Long apartmentID) {
        this.apartmentID = apartmentID;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public List<Member> getMembers() {
        return members;
    }

    public Payment getCurrentPayment() {
        return currentPayment;
    }

    public void setCurrentPayment(Payment currentPayment) {
        this.currentPayment = currentPayment;
    }

    public boolean isAddPayment() {
        return addPayment;
    }

    public boolean isEditPayment() {
        return editPayment;
    }

}
