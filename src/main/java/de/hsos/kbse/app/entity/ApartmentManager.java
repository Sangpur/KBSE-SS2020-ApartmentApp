/*
 * INTERFACE ApartmentManager
 *
 */
package de.hsos.kbse.app.entity;

import de.hsos.kbse.app.util.AppException;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
public interface ApartmentManager {
    
    public void createApartment(Apartment apartment) throws AppException;
    public void deleteApartment(Apartment apartment) throws AppException;
    public Apartment updateApartment(Apartment apartment) throws AppException;
    public Apartment findApartment(Long id) throws AppException;
    
}
