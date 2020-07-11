/*
 * CONTROLLER CLASS ShoppingList
 *
 */
package de.hsos.kbse.app.control;

import de.hsos.kbse.app.entity.features.ShoppingItem;
import de.hsos.kbse.app.entity.features.ShoppingItemManager;
import de.hsos.kbse.app.util.AppException;
import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
@RequestScoped
@Transactional
public class ShoppingList implements ShoppingItemManager, Serializable {
    
    /* ----------------------------------------- ATTRIBUTES ---------------------------------------- */
    
    @PersistenceContext(name = "ApartmentPU")
    private EntityManager em;
    
    /* --------------------------------------- PUBLIC METHODS -------------------------------------- */

    @Override
    public void createShoppingItem(ShoppingItem item) throws AppException {
        try {
            this.em.persist(item);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Artikel konnte nicht angelegt werden!");
        }
    }

    @Override
    public void deleteShoppingItem(ShoppingItem item) throws AppException {
        try {
            ShoppingItem toMerge = this.em.merge(item);
            this.em.remove(toMerge);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Artikel konnte nicht geloescht werden!");
        }
    }

    @Override
    public ShoppingItem updateShoppingItem(ShoppingItem item) throws AppException {
        try {
            item = this.em.merge(item);                     // Rueckgabe ist null, falls Artikel nicht existiert
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Artikel konnte nicht angepasst werden!");
        }
        return item;
    }

    @Override
    public ShoppingItem findShoppingItem(Long id) throws AppException {
        ShoppingItem item = null;
        try {
            item = this.em.find(ShoppingItem.class, id);    // Rueckgabe ist null, falls Artikel nicht existiert
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Artikel konnte nicht gefunden werden!");
        }
        return item;
    }
    
    /* -------------------------------------- PRIVATE METHODS -------------------------------------- */
    
    /* -------------------------------------- GETTER AND SETTER ------------------------------------ */
    
}
