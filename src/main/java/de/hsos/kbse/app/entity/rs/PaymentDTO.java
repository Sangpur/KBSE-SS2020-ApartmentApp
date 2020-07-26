/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hsos.kbse.app.entity.rs;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author Lucca
 */
public class PaymentDTO {
    public String id;
    public String sum;
    public String description;
    public String giverid;
    public List<String> listofinvolvedid;
}
