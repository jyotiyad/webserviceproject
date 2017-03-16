package com.jyoti.ws.project;

public class MatcherLauncher {

    public static void main(String[] args) {
        String inputWsdlFolderPath = "src/main/resources/WSDLs";
        String syntacticOutputFilePath = "src/main/output/syntacticOutput.xml";
        double minMatchScoreThreshold = 0.8d;
        SyntacticMatcher syntacticMatcher = new SyntacticMatcher(inputWsdlFolderPath, syntacticOutputFilePath, minMatchScoreThreshold);
        syntacticMatcher.match();

        String inputSaWsdlFolderPath = "src/main/resources/SAWSDL";
        String semanticOutputFilePath = "src/main/output/semanticOutput.xml";
        String owlFilePath = "/Users/manojy/dev/codebase/github/webserviceproject/data/SUMO.owl";

        minMatchScoreThreshold = 0.5d;
        SemanticMatcher semanticMatcher = new SemanticMatcher(inputSaWsdlFolderPath, owlFilePath, semanticOutputFilePath, minMatchScoreThreshold);
        semanticMatcher.match();
    }
}
