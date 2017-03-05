package com.jyoti.ws.project;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.jyoti.ws.project.ontology.MyOntManager;

import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public class SemanticMatcher extends BaseMatcher {

    private String sourceFileFolderPath;
    private String matchResultFileName;
    private double minMatchScoreThreshold;
    private String ontLocation;
    private MyOntManager ontsum;
    private OWLOntologyManager manager;
    private OWLOntology ontology;
    private Reasoner reasoner;
    private Map<String, OWLClass> classMap;
    private double exact = 1d;
    private double subsumption = 0.8d;
    private double plugIn = 0.6d;
    private double structural = 0.5d;
    private double notMatched = 0d;

    public SemanticMatcher(String sourceFileFolderPath, String owlFilePath,
                           String matchResultFileName, double minMatchScoreThreshold) {
        this.sourceFileFolderPath = sourceFileFolderPath;
        this.matchResultFileName = matchResultFileName;
        this.minMatchScoreThreshold = minMatchScoreThreshold;
        ontLocation = "file:///" + owlFilePath;

        ontsum = new MyOntManager();
        manager = ontsum.initializeOntologyManager();
        ontology = ontsum.initializeOntology(manager, ontLocation);
        reasoner = ontsum.initializeReasoner(ontology, manager);
        classMap = ontsum.loadClasses(reasoner);
    }

    public void match() {
        super.match(sourceFileFolderPath, matchResultFileName, minMatchScoreThreshold);
    }


    @Override
    protected double getMatchingScore(Parameter inputParameter, Parameter outputParameter) {
        return findMatching(inputParameter.modelReference, outputParameter.modelReference);
    }


    public double findMatching(String input, String output) {
        if (input == null || input.isEmpty() || output == null || output.isEmpty()) {
            System.out.println("notMatched");
            return notMatched;
        } else if (isSameAs(input, output)) {
            return exact;
        } else if (isSubClassOf(output, input)) {
            return subsumption;
        } else if (isSubClassOf(input, output)) {
            return plugIn;
        } else if (hasRelationWith(input, output)) {
            return structural;
        } else {
            return notMatched;
        }
    }


    private boolean isSameAs(String input, String output) {
        if (input == null) {
            return false;
        }

        return input.equals(output);
    }


    private boolean isSubClassOf(String child, String parent) {
        OWLClass childOwlClass = classMap.get(child.toLowerCase());
        OWLClass parentOwlClass = classMap.get(parent.toLowerCase());

        if (childOwlClass == null || parentOwlClass == null) {
            return false;
        }

        return reasoner.isSubClassOf(childOwlClass, parentOwlClass);
    }


    private boolean hasRelationWith(String input, String output) {
        OWLClass inputOwlClass = classMap.get(input.toLowerCase());
        OWLClass outputOwlClass = classMap.get(output.toLowerCase());

        if (inputOwlClass == null || outputOwlClass == null) {
            return false;
        }

        Vector<OWLObjectProperty> relationship = ontsum.findRelationship(inputOwlClass, outputOwlClass, reasoner);

        return relationship.size() > 0;
    }

}
