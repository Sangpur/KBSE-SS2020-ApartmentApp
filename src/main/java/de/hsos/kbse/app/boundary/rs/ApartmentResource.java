/*
 * JAX RS BOUNDARY CLASS ApartmentResource
 * 
 *
 */
package de.hsos.kbse.app.boundary.rs;

import de.hsos.kbse.app.control.ApartmentRepository;
import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Lucca Oberhößel
 */
@RequestScoped
@Path("apartment")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class ApartmentResource implements Serializable {
    
    /* ----------------------------------------- ATTRIBUTE ---------------------------------------- */
    
    @Inject 
    ApartmentRepository repository;
    
    @Inject
    private Jsonb jsonb;
    
    @Context
    UriInfo uriInfo;
    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
}
