package com.jyoti.ws.project;

import java.util.List;

public class Parameter {
    public String name;
    public String type;
    public String ref;
    public String modelReference;
    public List<Parameter> subParameters;

    public Parameter(String name) {
        this.name = name;
    }

    public Parameter(String name, String modelReference) {
        this.name = name;
        this.modelReference = modelReference;
    }

    public boolean hasSubParameters() {
        return subParameters != null && !subParameters.isEmpty();
    }

    public boolean hasModelReference() {
        return modelReference != null && !modelReference.isEmpty();
    }
}
