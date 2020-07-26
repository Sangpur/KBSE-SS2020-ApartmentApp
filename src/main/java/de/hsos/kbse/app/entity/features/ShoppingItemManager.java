/*
 * INTERFACE ShoppingItemManager
 *
 */
package de.hsos.kbse.app.entity.features;

import de.hsos.kbse.app.util.AppException;
import java.util.List;

/**
 *
 * @author Annika Limbrock
 */
public interface ShoppingItemManager {
    
    public void createShoppingItem(ShoppingItem item) throws AppException;
    public void deleteShoppingItem(ShoppingItem item) throws AppException;
    public void deleteAllShoppingItemsFrom(Long apartmentID) throws AppException;
    public ShoppingItem updateShoppingItem(ShoppingItem item) throws AppException;
    public ShoppingItem findShoppingItem(Long id) throws AppException;
    public List<ShoppingItem> getAllShoppingItemsFrom(Long apartmentID) throws AppException;
    
}
