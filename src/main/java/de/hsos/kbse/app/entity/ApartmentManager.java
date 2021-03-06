/*
 * INTERFACE ApartmentManager
 *
 */
package de.hsos.kbse.app.entity;

import de.hsos.kbse.app.util.AppException;
import java.util.List;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel
 */
public interface ApartmentManager {
    
    public void createApartment(Apartment apartment) throws AppException;
    public void deleteApartment(Apartment apartment) throws AppException;
    public Apartment updateApartment(Apartment apartment) throws AppException;
    public Apartment findApartment(Long id) throws AppException;
    public List<Apartment> getAllApartments() throws AppException;
}
