package match;
import info.debatty.java.stringsimilarity.NGram;
import openllet.owlapi.OpenlletReasoner;
import org.semanticweb.owlapi.model.*;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.OverlapCoefficient;
import org.simmetrics.metrics.StringMetrics;

import java.io.File;
import java.net.URISyntaxException;

class Model {

    private static Model INSTANCE;

    public Ontology getOntology() {
        return ontology;
    }

    private Ontology ontology;
    private OpenlletReasoner reasoner;

    public static Model getInstance(String path)  {
        if (INSTANCE == null) {
            INSTANCE = new Model();
        }
        return INSTANCE;
    }



    protected Model()  {

    }

public Ontology initOntology(String path) throws OWLOntologyCreationException, URISyntaxException {
    ontology =  new Ontology(IRI.create(new File(path)));
        return ontology;

}

    public void refreshReasoner(){
        ontology.reasoner.refresh();
    }


}
