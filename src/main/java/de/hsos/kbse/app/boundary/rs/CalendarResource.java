/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hsos.kbse.app.boundary.rs;

import de.hsos.kbse.app.entity.rs.EventDTO;
import de.hsos.kbse.app.control.ApartmentRepository;
import de.hsos.kbse.app.control.Calendar;
import de.hsos.kbse.app.control.MemberRepository;
import de.hsos.kbse.app.entity.Apartment;
import de.hsos.kbse.app.entity.features.Event;
import de.hsos.kbse.app.entity.member.Member;
import de.hsos.kbse.app.enums.EventCategory;
import de.hsos.kbse.app.util.AppException;
import java.io.Serializable;
import java.text.ParseException;
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
 * @author Lucca
 */
@Stateless
@Path("apartments/{apartmentId}/events")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class CalendarResource implements Serializable {
    /* ----------------------------------------- ATTRIBUTE ---------------------------------------- */
    
    @Inject 
    ApartmentRepository apartmentRepo;
    
    @Inject 
    MemberRepository memberRepo;
    
    @Inject 
    Calendar eventRepo;
    
    @Inject
    private Jsonb jsonb;
    
    @Context
    UriInfo uriInfo;
    
    private static final SimpleDateFormat DF = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    @GET
    public Response getAllEvents(@PathParam("apartmentId") String apartmentIdStr) {
        Long apartmentId;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url id is not a number").build();
        }
        List<Event> results = null;
        try {
            results = this.eventRepo.getAllEventsFrom(apartmentId);
        } catch (AppException ex) {
            Logger.getLogger(CalendarResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to find the Events.").build();
        }
        if(results != null)
            return Response.ok(jsonb.toJson(results)).build();
        return Response.ok("[]").build(); // Return empty Array if no Events were found.
    }
    
    @GET
    @Path("{id}")
    public Response getEvent(@PathParam("apartmentId") String apartmentIdStr, @PathParam("id") String idStr) {
        Long apartmentId;
        Long itemId;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
            itemId = Long.parseLong(idStr);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url ids are not numbers").build();
        }
        Event result = null;
        try {
            result = this.eventRepo.findEvent(itemId);
        } catch (AppException ex) {
            Logger.getLogger(CalendarResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to find the Event.").build();
        }
        if(result != null){
            if(result.getApartmentID().equals(apartmentId))
                return Response.ok(jsonb.toJson(result)).build();
        }
        return Response.ok("{}").build(); // Returns empty object for wrong id 
    }
    
    @POST
    public Response createEvent(@PathParam("apartmentId") String apartmentIdStr, EventDTO parameters) {
        Long apartmentId;
        Long authorId;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
            authorId = Long.parseLong(parameters.authorid);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url id or authorid is not a number").build();
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
        
        Member m = null;
        try {
            m = this.memberRepo.findMember(authorId);
        } catch (AppException ex) {
            Logger.getLogger(CalendarResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(m == null)
            return Response.status(400, "Bad Request: No member with this authorid found.").build();
        
        if(!m.getApartmentID().equals(apartmentId))
            return Response.status(400, "Bad Request: Author aparmentId does not match URL apartmentId.").build();
        
        
        
        if (parameters.title != null && parameters.title.length() >= 3
                && parameters.category != null && parameters.category.length() >= 5
                && parameters.begin != null && parameters.begin.length() == 16
                && parameters.end != null && parameters.end.length() == 16 ) {
            
            EventCategory category;
            try {
                category = EventCategory.valueOf(parameters.category);
            } catch (IllegalArgumentException ex) {
                return Response.status(400, "Bad Request: category needs to be either BIRTHDAY, EVENT, VACATION, APPOINTMENT or OTHER.").build();
            }
            
            Date begin;
            try {
                begin = new Date(DF.parse(parameters.begin).getTime());
            } catch (ParseException ex) {
                Logger.getLogger(CalendarResource.class.getName()).log(Level.SEVERE, null, ex);
                return Response.status(400, "Bad Request: begin format has to be yyyy-mm-dd hh:mm").build();
            }
            
            Date end;
            try {
                end = new Date(DF.parse(parameters.end).getTime());
            } catch (ParseException ex) {
                Logger.getLogger(CalendarResource.class.getName()).log(Level.SEVERE, null, ex);
                return Response.status(400, "Bad Request: begin format has to be yyyy-mm-dd hh:mm").build();
            }
            
            Event e = new Event(m, begin, end, apartmentId);
            e.setTitle(parameters.title);
            e.setBegin(begin);
            e.setEnd(end);
            e.setCategory(category);
            
            try {
                eventRepo.createEvent(e);
            } catch (AppException ex) {
                Logger.getLogger(CalendarResource.class.getName()).log(Level.SEVERE, null, ex);
                return Response.status(500, "Server Error: Failed while trying to create the Event.").build();
            }
            return Response.ok(jsonb.toJson(e)).build();
        } else {
            return Response.status(400, "Bad Request: Event could not be created. This Form is needed: {\n" +
                    "    title: ...,\n" +
                    "    authorid: ...,\n" +
                    "    category: ...,\n" +
                    "    begin: yyyy-mm-dd hh:mm ,\n" +
                    "    end: yyyy-mm-dd hh:mm ,\n" +
                    "}").build();
        }
    }
    
    @PUT
    @Path("{id}")
    public Response updateEvent(@PathParam("apartmentId") String apartmentIdStr, @PathParam("id") String idStr, EventDTO parameters) {
        Long apartmentId;
        Long id;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
            id = Long.parseLong(idStr);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url ids are not numbers").build();
        }
        Event e = null;
        try {
            e = this.eventRepo.findEvent(id);
        } catch (AppException ex) {
            Logger.getLogger(CalendarResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to find the Event.").build();
        }
        if(e == null){
            return Response.status(400, "Bad Request: Event with this id could not be found.").build();
        }
        if(!e.getApartmentID().equals(apartmentId)){
            return Response.status(400, "Bad Request: Apartment id does not match URL.").build();
        }
        
        
        if (parameters.title != null && parameters.title.length() >= 3
                && parameters.category != null && parameters.category.length() >= 5
                && parameters.begin != null && parameters.begin.length() == 16
                && parameters.end != null && parameters.end.length() == 16 ) {
            
            EventCategory category;
            try {
                category = EventCategory.valueOf(parameters.category);
            } catch (IllegalArgumentException ex) {
                return Response.status(400, "Bad Request: category needs to be either BIRTHDAY, EVENT, VACATION, APPOINTMENT or OTHER.").build();
            }
            
            Date begin;
            try {
                begin = new Date(DF.parse(parameters.begin).getTime());
            } catch (ParseException ex) {
                Logger.getLogger(CalendarResource.class.getName()).log(Level.SEVERE, null, ex);
                return Response.status(400, "Bad Request: begin format has to be yyyy-mm-dd hh:mm").build();
            }
            
            Date end;
            try {
                end = new Date(DF.parse(parameters.end).getTime());
            } catch (ParseException ex) {
                Logger.getLogger(CalendarResource.class.getName()).log(Level.SEVERE, null, ex);
                return Response.status(400, "Bad Request: begin format has to be yyyy-mm-dd hh:mm").build();
            }
            
            e.setTitle(parameters.title);
            e.setBegin(begin);
            e.setEnd(end);
            e.setCategory(category);
            
            
            try {
                eventRepo.updateEvent(e);
            } catch (AppException ex) {
                Logger.getLogger(CalendarResource.class.getName()).log(Level.SEVERE, null, ex);
                return Response.status(500, "Server Error: Failed while trying to Update the Event.").build();
            }
            return Response.ok(jsonb.toJson(e)).build();
        } else {
            return Response.status(400, "Bad Request: Event could not be updated. This Form is needed: {\n" +
                    "    title: ...,\n" +
                    "    authorid: ...,\n" +
                    "    category: ...,\n" +
                    "    begin: yyyy-mm-dd hh:mm ,\n" +
                    "    end: yyyy-mm-dd hh:mm ,\n" +
                    "}").build();
        }
    }
    
    @DELETE
    @Path("{id}")
    public Response deleteEvent(@PathParam("apartmentId") String apartmentIdStr, @PathParam("id") String idStr) {
        Long apartmentId;
        Long id;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
            id = Long.parseLong(idStr);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url ids are not numbers").build();
        }
        Event e = null;
        try {
            e = this.eventRepo.findEvent(id);
        } catch (AppException ex) {
            Logger.getLogger(ShoppingListResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to find the Event.").build();
        }
        if(e == null){
            return Response.status(400, "Bad Request: Event with this id could not be found.").build();
        }
        if(!e.getApartmentID().equals(apartmentId)){
            return Response.status(400, "Bad Request: Apartment id does not match URL.").build();
        }
        try {
            eventRepo.deleteEvent(e);
            return Response.ok().build();
        } catch (AppException ex) {
            Logger.getLogger(ShoppingListResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to delete the Event.").build();
        } 
    }
    
}
