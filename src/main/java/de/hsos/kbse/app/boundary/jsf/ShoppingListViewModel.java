/*
 * BOUNDARY CLASS ShoppingListViewModel
 *
 */
package de.hsos.kbse.app.boundary.jsf;

import de.hsos.kbse.app.control.ShoppingList;
import de.hsos.kbse.app.entity.features.ShoppingItem;
import de.hsos.kbse.app.entity.member.Member;
import de.hsos.kbse.app.enums.LogLevel;
import de.hsos.kbse.app.enums.ValidationGroup;
import de.hsos.kbse.app.util.AppException;
import de.hsos.kbse.app.util.Condition;
import de.hsos.kbse.app.util.General;
import de.hsos.kbse.app.util.Logable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
@Named("shoppingVM")
@ConversationScoped
public class ShoppingListViewModel implements Serializable {
    
    /* ----------------------------------------- ATTRIBUTE ---------------------------------------- */
    
    /* Controller Class */
    private final ShoppingList shoppinglist;
    
    /* Injected Conversation */
    private final Conversation conversation;
    
    /* Bean Validation API */
    private static Validator validator;
    
    private Long apartmentID = 1000L;   // TODO: Hinzufuegen sobald Scope gestartet wird bei Login-Prozess
    private List<ShoppingItem> items;
    private Member loggedInMember;
    private ShoppingItem currentItem;
    private ShoppingItem originalItem;  // Sicherung des zu bearbeitenden ShoppingItem-Objekts
    private boolean addItem;            // true = addItem()
    private boolean editItem;           // true = editItem()
    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    @Inject
    @Logable(LogLevel.INFO)
    public ShoppingListViewModel(ShoppingList shoppinglist, Conversation conversation) {
        this.shoppinglist = shoppinglist;
        this.conversation = conversation;
        this.items = new ArrayList();
        this.initItemsList();
    }
    
    public void changeStatus(ShoppingItem item) {
        try {
            /* Bestehendes ShoppingItem-Objekt in der Datenbank updaten */
            this.shoppinglist.updateShoppingItem(item);
        } catch(AppException ex) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
            facesContext.addMessage("Error",msg);
        }
        /* Erneute alphabetische Sortierung der Artikel anhand des Status */
        Collections.sort(this.items);
    }
    
    public boolean checkAccessRights(ShoppingItem item) {
        /* Es muss geprueft werden, ob der jeweilige Artikel bereits als "erledigt" markiert ist, da in
         * diesem Fall ein Bearbeiten und Loeschen nicht mehr moeglich sein soll. Im Gegensatz zu den 
         * anderen Funktionen duerfen hier ansonsten alle Mitglieder editieren oder loeschen. */
        return !item.isChecked();
    }
    
    @Logable(LogLevel.INFO)
    public String addItem() {
        /* Neues Payment-Objekt initiieren */
        this.currentItem = new ShoppingItem(new Date(), this.apartmentID);
        this.addItem = true;
        this.editItem = false;
        return "shoppinglist-add";
    }
    
    @Logable(LogLevel.INFO)
    public String editItem(ShoppingItem item) {
        /* Ausgewaehltes ShoppingItem-Objekt setzen und Sicherungskopie anlegen */
        this.currentItem = item;
        this.originalItem = new ShoppingItem(item);
        this.editItem = true;
        this.addItem = false;
        return "shoppinglist-add";
    }
    
    @Logable(LogLevel.INFO)
    public void deleteItem(ShoppingItem item) {
        try {
            /* Bestehendes ShoppingItem-Objekt aus der Datenbank entfernen */
            this.shoppinglist.deleteShoppingItem(item);
        } catch(AppException ex) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
            facesContext.addMessage("Error",msg);
        }
        /* Bestehendes ShoppingItem-Objekt aus der Items-Liste entfernen */
        this.items.remove(item);
    }
    
    @Logable(LogLevel.INFO)
    public String saveItem() {
        if(this.validateInput(ValidationGroup.GENERAL)) {   // Gueltige Eingabe
            if(this.addItem) {   // ShoppingItem hinzufuegen
                try {
                    /* Neues ShoppingItem-Objekt der Datenbank hinzufügen */
                    this.shoppinglist.createShoppingItem(currentItem);
                } catch(AppException ex) {
                    FacesContext facesContext = FacesContext.getCurrentInstance();
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
                    facesContext.addMessage("Error",msg);
                }
                /* Neues ShoppingItem-Objekt der zu Anfang initialisierten Items-Liste hinzufügen */
                this.items.add(currentItem);
                /* Erneute alphabetische Sortierung der Artikel anhand des Status */
                Collections.sort(this.items);
            }
            if(this.editItem) {  // Bestehendes ShoppingItem updaten
                try {
                    /* Bestehendes ShoppingItem-Objekt in der Datenbank updaten */
                    this.shoppinglist.updateShoppingItem(currentItem);
                } catch(AppException ex) {
                    FacesContext facesContext = FacesContext.getCurrentInstance();
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
                    facesContext.addMessage("Error",msg);
                }
            }
        } else {    // Ungueltige Eingabe
            return "";
        }
        return "shoppinglist";
    }
    
    @Logable(LogLevel.INFO)
    public String discardItem() {
        if(this.editItem) {
            /* Falls ein vorhandenes Objekt bearbeitet werden sollte und diese Aktion abgebrochen wurde,
             * muss dieses auf seinen Originalzustand zurückgesetzt werden.*/
            this.currentItem.setName(this.originalItem.getName());
            this.currentItem.setAmount(this.originalItem.getAmount());
        }
        return "shoppinglist";
    }
    
    /* ------------------------------------- METHODEN PRIVATE ------------------------------------- */
    
    @PostConstruct
    private static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    private void initItemsList() {
        try {
            this.items = this.shoppinglist.getAllShoppingItemsFrom(apartmentID);
        } catch(AppException ex) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", ex.getMessage());
            facesContext.addMessage("Error",msg);
        }
        /* Alphabetische Sortierung der Artikel anhand des Status */
        Collections.sort(this.items);
    }
    
    @Logable(LogLevel.INFO)
    private boolean validateInput(ValidationGroup group) {
        /* Die Methode validate() gibt ein Set von ConstraintViolations zurueck, in dem alle moeglicherweise begangenen Verstoesse aufgefuehrt
         * sind. Dieses Set ist leer, falls die Eingabe gueltig ist. Durch die Gruppierung der Constraints, hier General.class, besteht die
         * Moeglichkeit, die Validation erst dann auszufuehren, wenn die entsprechende Gruppe direkt durch das Programm angesprochen wird.
         * s. https://www.baeldung.com/javax-validation-groups */
        Set<ConstraintViolation<ShoppingItem>> constraintViolations = null;
        if(group == ValidationGroup.GENERAL) {
            constraintViolations = validator.validate( this.currentItem, General.class );
        }

        if(constraintViolations != null && constraintViolations.isEmpty()) {
            /* Gueltige Eingabe */
            return true;
        } else {
            /* Ungueltige Eingabe */
            FacesContext facesContext = FacesContext.getCurrentInstance();
            Iterator<ConstraintViolation<ShoppingItem>> iter = constraintViolations.iterator();
            while (iter.hasNext()) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Constraint.", iter.next().getMessage());
                facesContext.addMessage("Constraint Violation",msg);
            }
            return false;
        }
    }
     
    /* -------------------------------------- GETTER AND SETTER ------------------------------------ */

    public List<ShoppingItem> getItems() {
        return items;
    }
    
    public void setLoggedInMember(Member loggedInMember) {
        this.loggedInMember = loggedInMember;
    }

    public ShoppingItem getCurrentItem() {
        return currentItem;
    }

    public void setCurrentItem(ShoppingItem currentItem) {
        this.currentItem = currentItem;
    }

}
