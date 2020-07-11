/*
 * INTERFACE ShoppingItemManager
 *
 */
package de.hsos.kbse.app.entity.features;

import de.hsos.kbse.app.util.AppException;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
public interface ShoppingItemManager {
    
    public void createShoppingItem(ShoppingItem item) throws AppException;
    public void deleteShoppingItem(ShoppingItem item) throws AppException;
    public ShoppingItem updateShoppingItem(ShoppingItem item) throws AppException;
    public ShoppingItem findShoppingItem(Long id) throws AppException;
    
}
