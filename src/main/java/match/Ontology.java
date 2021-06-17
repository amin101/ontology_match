package match;

import com.google.common.collect.Multimap;
import openllet.owlapi.*;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.io.StreamDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Ontology {

    public OWLOntologyManager manager;
    public OWLDataFactory factory;
    public String prefix;
    public OWLOntology ont;
    //    OWLReasonerFactory reasonerFactory;
    public OpenlletReasoner reasoner;


    public Ontology(IRI path) throws OWLOntologyCreationException, URISyntaxException {

        this.manager = OWLManager.createOWLOntologyManager();
        this.factory = manager.getOWLDataFactory();
        // IRI ontologyIRI = IRI.create(new File(path));

        ont = load(path);
        // ont.toString();
        reasoner = createReasoner(ont);

        prefix = ont.getOntologyID().getOntologyIRI().get() + "#";


    }


    public OntModel jenaLoadOntology(String path) {
       // JenaSystem.init();
        org.apache.jena.query.ARQ.init();
        OntModel m = ModelFactory.createOntologyModel();
        OntDocumentManager dm = m.getDocumentManager();

        InputStream in = FileManager.get().open(path);
        try {
            m.read(new FileInputStream(path), "RDF/XML");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return m;
    }

    public String jenaRunSparql(String queryString, OntModel model) {

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();

            String fmt = ResultSetFormatter.asText(results);
            // System.out.println(fmt);
            return fmt;
        }
    }


    OWLOntology load(IRI path) throws OWLOntologyCreationException {
        // in this test, the ontology is loaded from a string

        return manager.loadOntologyFromOntologyDocument(path);
        //  reasoner.refresh();
    }


    private OpenlletReasoner createReasoner(OWLOntology rootOntology) {
        return OpenlletReasonerFactory.getInstance().createReasoner(rootOntology);
    }


    public void saveOntology(OWLOntology ont, PrefixDocumentFormat owlDocumentFormat) throws OWLOntologyStorageException {

        OWLDocumentFormat format = manager.getOntologyFormat(ont);

        if (format.isPrefixOWLDocumentFormat()) {
            owlDocumentFormat.copyPrefixesFrom(format.asPrefixOWLDocumentFormat());
            format = owlDocumentFormat;
        }

        File file = new File("ontology_saving.owl");
        manager.saveOntology(ont, format, IRI.create(file.toURI()));
    }


    public OWLOntology getOwlOntology() {

        return reasoner.getOntology();

    }

    public ArrayList<String> getAllIndividuals(OWLOntology ontology) {
        ArrayList<String> myArray = new ArrayList<String>();

        Iterable<OWLNamedIndividual> inds = reasoner.getOntology().getIndividualsInSignature();
        inds.forEach(x -> myArray.add(x.getIRI().getFragment()));
        return myArray;
    }


    public ArrayList<String> listAllDataPropertyValues(OWLNamedIndividual individual, OWLOntology ontology, OWLReasoner reasoner) {
        ArrayList<String> myArray = new ArrayList<String>();

        OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();

        Multimap<OWLDataPropertyExpression, OWLLiteral> assertedValues = EntitySearcher.getDataPropertyValues(individual, ontology);


        for (OWLDataProperty dataProp : ontology.getDataPropertiesInSignature(true)) {

            for (OWLLiteral literal : reasoner.getDataPropertyValues(individual, dataProp)) {
                Collection<OWLLiteral> literalSet = assertedValues.get(dataProp);
                boolean asserted = (literalSet != null && literalSet.contains(literal));

                myArray.add((asserted ? "<font>asserted" : "<font color=\"blue\">inferred") + " data property for " + renderer.render(individual) + " : "
                        + renderer.render(dataProp) + " -> " + renderer.render(literal) + "</font>");
            }
        }

        return myArray;
    }


    public ArrayList<String> listAllObjectPropertyValues(OWLNamedIndividual individual, OWLOntology ontology, OWLReasoner reasoner) {
        ArrayList<String> myArray = new ArrayList<String>();

        OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();

        Multimap<OWLObjectPropertyExpression, OWLIndividual> assertedValues = EntitySearcher.getObjectPropertyValues(individual, ontology);


        for (OWLObjectProperty objectPropOfAll : ontology.getObjectPropertiesInSignature(true)) {

            for (Node<OWLNamedIndividual> reasonerIndividualNode : reasoner.getObjectPropertyValues(individual, objectPropOfAll)) {
                Collection<OWLIndividual> assertedOwlIndividuals = assertedValues.get(objectPropOfAll);
                AtomicBoolean asserted = new AtomicBoolean(false);//(assertedOwlIndividuals != null && assertedOwlIndividuals.contains(reasonerIndividualNode));

                reasonerIndividualNode.forEach(x -> {
//                       asserted.set(renderer.render(x).contains(renderer.render(reasonerIndividualNode.getRepresentativeElement())));
//                        if(asserted.get()) return;

                    asserted.set(assertedOwlIndividuals.
                            stream().
                            map(renderer::render).
                            anyMatch(c -> c.contains(renderer.render(x))));


                    myArray.add((asserted.get() ? "<font>asserted" : "<font color=\"blue\">inferred") + " object property for " + renderer.render(individual) + " : "
                            + renderer.render(objectPropOfAll) + " -> " + renderer.render(x) + "</font>");


                });

            }
        }

        return myArray;
    }


    public Optional<OutputStream> outputOntology(OWLOntology ontology, PrefixDocumentFormat owlDocumentFormat) throws OWLOntologyStorageException {

        // By default ontologies are saved in the format from which they were
        // loaded. In this case the ontology was loaded from rdf/xml. We
        // can get information about the format of an ontology from its manager
        OWLDocumentFormat format = manager.getOntologyFormat(ontology);
        // We can save the ontology in a different format. Lets save the
        // ontology
        // in owl/xml format

//        OWLXMLDocumentFormat owlxmlFormat = new OWLXMLDocumentFormat();
        // Some ontology formats support prefix names and prefix IRIs. In our
        // case we loaded the Koala ontology from an rdf/xml format, which
        // supports prefixes. When we save the ontology in the new format we
        // will copy the prefixes over so that we have nicely abbreviated IRIs
        // in the new ontology document

        if (format.isPrefixOWLDocumentFormat()) {
            owlDocumentFormat.copyPrefixesFrom(format.asPrefixOWLDocumentFormat());
        }

        //  manager.saveOntology(ontology, owlxmlFormat, IRI.create(file.toURI()));

        // We can also dump an ontology to System.out by specifying a different
        // OWLOntologyOutputTarget. Note that we can write an ontology to a
        // stream in a similar way using the StreamOutputTarget class
        // Try another format - The Manchester OWL Syntax
        //  ManchesterSyntaxDocumentFormat manSyntaxFormat = new ManchesterSyntaxDocumentFormat();

//        if (format.isPrefixOWLDocumentFormat()) {
//            manSyntaxFormat.copyPrefixesFrom(format.asPrefixOWLDocumentFormat());
//        }
        // Replace the ByteArrayOutputStream wth an actual output stream to save
        // to a file.
        StreamDocumentTarget str = new StreamDocumentTarget(new ByteArrayOutputStream());

        manager.saveOntology(ontology, owlDocumentFormat, str);
        return str.getOutputStream();
    }

}


