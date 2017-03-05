package com.jyoti.ws.project;

import java.util.ArrayList;
import java.util.List;

public class Operation {
    private String name;
    private List<Parameter> inputParameterList;
    private List<Parameter> outputParameterList;

    public Operation(String name) {
        this.name = name;
        inputParameterList = new ArrayList<>();
        outputParameterList= new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Parameter> getInputParameterList() {
        return inputParameterList;
    }

    public void addInputParameterList(List<Parameter> parameters) {
        List<Parameter> nestedParameters = new ArrayList<>();
        for (Parameter parameter : parameters) {
            if (parameter.hasSubParameters() && !parameter.hasModelReference()) {
                nestedParameters.addAll(parameter.subParameters);
            } else {
                nestedParameters.add(parameter);
            }
        }
        inputParameterList.addAll(nestedParameters);
    }

    public void addOutputParameterList(List<Parameter> parameters) {
        List<Parameter> nestedParameters = new ArrayList<>();
        for (Parameter parameter : parameters) {
            if (parameter.hasSubParameters() && !parameter.hasModelReference()) {
                nestedParameters.addAll(parameter.subParameters);
            } else {
                nestedParameters.add(parameter);
            }
        }
        outputParameterList.addAll(nestedParameters);
    }


    public List<Parameter> getOutputParameterList() {
        return outputParameterList;
    }

}
