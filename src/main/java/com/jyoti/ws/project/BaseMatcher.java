package com.jyoti.ws.project;

import com.jyoti.webservice.*;
import com.jyoti.ws.project.parser.WsdlParser;
import com.jyoti.ws.project.util.FileUtils;
import com.jyoti.ws.project.util.JAXBUtils;

import java.io.File;
import java.util.*;

public abstract class BaseMatcher {
    protected Map<String, List<Operation>> wsOperationsMap = new HashMap<>();


    protected void match(String sourceFilesDirPath, String outputFilePath, double minMatchScoreThreshold) {
        List<File> files = FileUtils.getAllFilesInDirectory(new File(sourceFilesDirPath));

        // Parse all wsdl files
        for (File file : files) {
            TDefinitions definitions = JAXBUtils.createWsdlFileDefinitions(file);
            WsdlParser wsdlParser = new WsdlParser();
            //key - file name and value operations for that file
            wsOperationsMap.put(file.getAbsolutePath(), wsdlParser.getOperationsList(definitions));
        }

        // now match all files
        WSMatchingType wsMatchingType = new WSMatchingType();
        List<String> fileNames = new ArrayList<>(wsOperationsMap.keySet());
        for (String inputFileName : fileNames) {
            for (String outputFileName : fileNames) {
                if (!inputFileName.equals(outputFileName)) {
                    MatchedWebServiceType matchedWebServiceType = matchWebServiceOperations(inputFileName, outputFileName);
                    if (matchedWebServiceType.getWsScore() > minMatchScoreThreshold) {
                        wsMatchingType.getMacthing().add(matchedWebServiceType);
                    }
                }
            }
        }

        Collections.sort(wsMatchingType.getMacthing(), new Comparator<MatchedWebServiceType>() {

            @Override
            public int compare(MatchedWebServiceType o1, MatchedWebServiceType o2) {
                if (o1.getWsScore() > o2.getWsScore()) {
                    return -1;
                } else if (o1.getWsScore() < o2.getWsScore()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        JAXBUtils.createOutputFile(wsMatchingType, outputFilePath);
    }

    private MatchedWebServiceType matchWebServiceOperations(String inputFileName, String outputFileName) {
        List<Operation> inputOperations = wsOperationsMap.get(inputFileName);
        List<Operation> outputOperations = wsOperationsMap.get(outputFileName);
        MatchedWebServiceType matchedWebServiceType = new MatchedWebServiceType();
        matchedWebServiceType.setInputServiceName(inputFileName);
        matchedWebServiceType.setOutputServiceName(outputFileName);

        for (Operation inputOperation : inputOperations) {
            MatchedOperationType matchedOperationType = new MatchedOperationType();
            matchedOperationType.setInputOperationName(inputOperation.getName());

            for (Operation outputOperation : outputOperations) {
                List<MatchedElementType> allMatchedElements = new ArrayList<>();

                if (inputOperation.getInputParameterList().size() == outputOperation.getOutputParameterList().size()) {
                    for (Parameter inputParameter : inputOperation.getInputParameterList()) {
                        MatchedElementType bestMatchedElement = new MatchedElementType();
                        bestMatchedElement.setInputElement(inputParameter.name);

                        for (Parameter outputParameter : outputOperation.getOutputParameterList()) {
                            double matchingScore = getMatchingScore(inputParameter, outputParameter);

                            if (matchingScore > bestMatchedElement.getScore()) {
                                bestMatchedElement.setScore(matchingScore);
                                bestMatchedElement.setOutputElement(outputParameter.name);
                            }
                        }

                        allMatchedElements.add(bestMatchedElement);
                    }

                    double operationScore = calculateOperationScore(allMatchedElements);

                    if (operationScore > matchedOperationType.getOpScore()) {
                        matchedOperationType.setOpScore(operationScore);
                        matchedOperationType.setOutputOperationName(outputOperation.getName());
                        matchedOperationType.getMacthedElement().clear();
                        matchedOperationType.getMacthedElement().addAll(allMatchedElements);
                    }
                }
            }

            if (matchedOperationType.getOutputOperationName() != null && !matchedOperationType.getOutputOperationName().isEmpty()) {
                matchedWebServiceType.getMacthedOperation().add(matchedOperationType);
            }
        }

        double webServiceScore = calculateServiceScore(matchedWebServiceType.getMacthedOperation());
        matchedWebServiceType.setWsScore(webServiceScore);

        return matchedWebServiceType;
    }


    protected abstract double getMatchingScore(Parameter inputParameter, Parameter outputParameter);

    private double calculateOperationScore(List<MatchedElementType> elementTypes) {
        double sum = 0d;

        for (MatchedElementType elementType : elementTypes) {
            sum = sum + elementType.getScore();
        }

        //calculate average
        if (sum > 0d) {
            return sum / elementTypes.size();
        }

        return sum;
    }

    private double calculateServiceScore(List<MatchedOperationType> operationTypes) {
        double sum = 0d;

        for (MatchedOperationType operationType : operationTypes) {
            sum = sum + operationType.getOpScore();
        }

        //calculate average
        if (sum > 0d) {
            return sum / operationTypes.size();
        }

        return sum;
    }
}
