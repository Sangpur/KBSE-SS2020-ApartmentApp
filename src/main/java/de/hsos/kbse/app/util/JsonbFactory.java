/*
 * FACTORY CLASS JsonbFactory
 *
 */
package de.hsos.kbse.app.util;

import javax.enterprise.inject.Produces;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
public class JsonbFactory {
    
    @Produces
    public Jsonb createJsonb() {
        return JsonbBuilder.create();
    }
    
}
