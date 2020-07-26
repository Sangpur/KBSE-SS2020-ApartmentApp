/*
 * JAX RS BOUNDARY CLASS ShoppingListResource
 * 
 */
package de.hsos.kbse.app.boundary.rs;

import de.hsos.kbse.app.control.ApartmentRepository;
import de.hsos.kbse.app.control.MemberRepository;
import de.hsos.kbse.app.control.ShoppingList;
import de.hsos.kbse.app.entity.Apartment;
import de.hsos.kbse.app.entity.features.ShoppingItem;
import de.hsos.kbse.app.util.AppException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Lucca Oberhößel
 */
@Stateless
@Path("apartments/{apartmentId}/shoppingitems")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class ShoppingListResource implements Serializable {
    
    /* ----------------------------------------- ATTRIBUTE ---------------------------------------- */
    
    @Inject 
    ApartmentRepository apartmentRepo;
    
    @Inject 
    MemberRepository memberRepo;
    
    @Inject 
    ShoppingList shoppingRepo;
    
    @Inject
    private Jsonb jsonb;
    
    @Context
    UriInfo uriInfo;
    
    private static final SimpleDateFormat DF = new SimpleDateFormat( "yyyy-MM-dd" );
    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    @GET
    public Response getAllShoppingItems(@PathParam("apartmentId") String apartmentIdStr) {
        Long apartmentId;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url id is not a number").build();
        }
        List<ShoppingItem> results = null;
        try {
            results = this.shoppingRepo.getAllShoppingItemsFrom(apartmentId);
        } catch (AppException ex) {
            Logger.getLogger(ShoppingListResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to find the ShoppingItems.").build();
        }
        if(results != null)
            return Response.ok(jsonb.toJson(results)).build();
        return Response.ok("[]").build(); // Return empty Array if no ShoppingItems were found.
    }
    
    @GET
    @Path("{id}")
    public Response getShoppingItem(@PathParam("apartmentId") String apartmentIdStr, @PathParam("id") String idStr) {
        Long apartmentId;
        Long itemId;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
            itemId = Long.parseLong(idStr);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url ids are not numbers").build();
        }
        ShoppingItem result = null;
        try {
            result = this.shoppingRepo.findShoppingItem(itemId);
        } catch (AppException ex) {
            Logger.getLogger(ShoppingListResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to find the ShoppingItem.").build();
        }
        if(result != null){
            if(result.getApartmentID().equals(apartmentId))
                return Response.ok(jsonb.toJson(result)).build();
        }
        return Response.ok("{}").build(); // Returns empty object for wrong id 
    }
    
    @POST
    public Response createShoppingItem(@PathParam("apartmentId") String apartmentIdStr, ShoppingItem parameters) {
        Long apartmentId;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url id is not a number").build();
        }
        Apartment a = null;
        try {
            a = apartmentRepo.findApartment(apartmentId);
        } catch (AppException ex) {
            Logger.getLogger(ShoppingListResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to find apartment.").build();
        }
        if(a == null){
            return Response.status(400, "Bad Request: No apartment with this id found.").build();
        }
        
        if (parameters.getName() != null && parameters.getName().length() >= 3
                && parameters.getAmount() >= 1 ) {
            ShoppingItem s = new ShoppingItem(new Date(), a.getId());
            s.setName(parameters.getName());
            s.setAmount(parameters.getAmount());
            
            try {
                shoppingRepo.createShoppingItem(s);
            } catch (AppException ex) {
                Logger.getLogger(ShoppingListResource.class.getName()).log(Level.SEVERE, null, ex);
                return Response.status(500, "Server Error: Failed while trying to create the ShoppingItem.").build();
            }
            return Response.ok(jsonb.toJson(s)).build();
        } else {
            return Response.status(400, "Bad Request: ShoppingItem could not be created. This Form is needed: {\n" +
                    "    name: ...,\n" +
                    "    amount: ...,\n" +
                    "}").build();
        }
    }
    
    @PUT
    @Path("{id}")
    public Response updateShoppingItem(@PathParam("apartmentId") String apartmentIdStr, @PathParam("id") String idStr, ShoppingItem parameters) {
        Long apartmentId;
        Long itemId;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
            itemId = Long.parseLong(idStr);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url ids are not numbers").build();
        }
        ShoppingItem s = null;
        try {
            s = this.shoppingRepo.findShoppingItem(itemId);
        } catch (AppException ex) {
            Logger.getLogger(ShoppingListResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to find the ShoppingItem.").build();
        }
        if(s == null){
            return Response.status(400, "Bad Request: ShoppingItem with this id could not be found.").build();
        }
        if(!s.getApartmentID().equals(apartmentId)){
            return Response.status(400, "Bad Request: Apartment id does not match URL.").build();
        }
        if (parameters.getName() != null && parameters.getName().length() >= 3
                && parameters.getAmount() >= 1 ) {
            s.setName(parameters.getName());
            s.setAmount(parameters.getAmount());
            if (parameters.isChecked()) {
                s.setChecked(true);
            } else {
                s.setChecked(false);
            }
            try {
                shoppingRepo.updateShoppingItem(s);
            } catch (AppException ex) {
                Logger.getLogger(ShoppingListResource.class.getName()).log(Level.SEVERE, null, ex);
                return Response.status(500, "Server Error: Failed while trying to update the ShoppingItem.").build();
            }
            return Response.ok(jsonb.toJson(s)).build();
        } else {
            return Response.status(400, "Bad Request: ShoppingItem could not be updated. This Form is needed: {\n" +
                    "    name: ...,\n" +
                    "    amount: ...,\n" +
                    "}").build();
        }
        
    }
    
    @DELETE
    @Path("{id}")
    public Response deleteShoppingItem(@PathParam("apartmentId") String apartmentIdStr, @PathParam("id") String idStr) {
        Long apartmentId;
        Long itemId;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
            itemId = Long.parseLong(idStr);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url ids are not numbers").build();
        }
        ShoppingItem s = null;
        try {
            s = this.shoppingRepo.findShoppingItem(itemId);
        } catch (AppException ex) {
            Logger.getLogger(ShoppingListResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to find the ShoppingItem.").build();
        }
        if(s == null){
            return Response.status(400, "Bad Request: ShoppingItem with this id could not be found.").build();
        }
        if(!s.getApartmentID().equals(apartmentId)){
            return Response.status(400, "Bad Request: Apartment id does not match URL.").build();
        }
        try {
            shoppingRepo.deleteShoppingItem(s);
            return Response.ok().build();
        } catch (AppException ex) {
            Logger.getLogger(ShoppingListResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to delete the ShoppingItem.").build();
        } 
    }
}
