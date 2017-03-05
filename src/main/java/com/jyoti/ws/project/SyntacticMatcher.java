package com.jyoti.ws.project;


public class SyntacticMatcher extends BaseMatcher {

    private String sourceFilesDirPath;
    private String outputFilePath;
    private double minMatchScoreThreshold;

    public SyntacticMatcher(String sourceFilesDirPath, String outputFilePath, double minMatchScoreThreshold) {
        this.sourceFilesDirPath = sourceFilesDirPath;
        this.outputFilePath = outputFilePath;
        this.minMatchScoreThreshold = minMatchScoreThreshold;
    }

    public void match() {
        super.match(sourceFilesDirPath, outputFilePath, minMatchScoreThreshold);
    }


    @Override
    protected double getMatchingScore(Parameter inputParameter, Parameter outputParameter) {
        return com.jyoti.ws.project.EditDistance.getSimilarity(inputParameter.name, outputParameter.name);
    }

}
