/*
 * RS ENTITY CLASS PaymentDTO
 *
 */
package de.hsos.kbse.app.boundary.rs.dto;

import java.util.List;

/**
 *
 * @author Lucca Oberhößel
 */
public class PaymentDTO {
    
    /* ----------------------------------------- ATTRIBUTES ---------------------------------------- */
    
    public String id;
    public String sum;
    public String description;
    public String giverid;
    public List<String> listofinvolvedid;
    
}
