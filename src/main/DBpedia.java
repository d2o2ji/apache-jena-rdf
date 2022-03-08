package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.ModelFactory;

public class DBpedia
{
    static public void main(String... argv)
    {
        String serviceURI  = "http://dbpedia-live.openlinksw.com/sparql" ;

        String queryString = 
            "SELECT * WHERE { " +
            "    SERVICE <" + serviceURI + "> { " +
            "        select distinct ?Scientist where {?Scientist a <http://dbpedia.org/ontology/Scientist>} LIMIT 20" +
            "    }" +
            "}" ;
        Query query = QueryFactory.create(queryString);

        // Execution.
        try(QueryExecution qexec = QueryExecutionFactory.create(query, ModelFactory.createDefaultModel())) {
            Map<String, Map<String,List<String>>> serviceParams = new HashMap<>() ;
            Map<String,List<String>> params = new HashMap<>() ;
            List<String> values = new ArrayList<>() ;
            values.add("2000") ;
            params.put("timeout", values) ;
            serviceParams.put(serviceURI, params) ;
            qexec.getContext().set(ARQ.serviceParams, serviceParams) ;
            ResultSet rs = qexec.execSelect() ;
            ResultSetFormatter.out(System.out, rs, query) ;
        }
    }
}
