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
 * @author Lucca Oberhößel
 */
public class JsonbFactory {
    
    @Produces
    public Jsonb createJsonb() {
        return JsonbBuilder.create();
    }
    
}
