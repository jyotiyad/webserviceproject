package com.jyoti.ws.project.parser;

import com.jyoti.webservice.*;
import com.jyoti.ws.project.Operation;
import com.jyoti.ws.project.Parameter;
import org.apache.xerces.dom.ElementNSImpl;
import org.apache.xerces.dom.TextImpl;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.*;

public class WsdlParser {
    private String messageTypeInput = "INPUT";
    private String messageTypeOutput = "OUTPUT";
    protected Map<String, Parameter> elementParametersMap = new HashMap<>();
    protected Map<String, Parameter> typeParametersMap = new HashMap<>();
    Set<String> validTypes = new HashSet<>();

    public WsdlParser() {
        validTypes.add("boolean");
        validTypes.add("int");
        validTypes.add("long");
        validTypes.add("float");
        validTypes.add("double");
        validTypes.add("decimal");
        validTypes.add("string");
    }

    public List<Operation> getOperationsList(TDefinitions tDefinitions) {
        TTypes serviceTypes = null;
        Map<String, TMessage> serviceMessages = new HashMap<>();
        List<TOperation> serviceOperations = new ArrayList<>();

        //iterate and collect all different types of operations
        for (TDocumented tDocumented : tDefinitions.getAnyTopLevelOptionalElement()) {
            if (tDocumented instanceof TTypes) {
                serviceTypes = (TTypes) tDocumented;
            } else if (tDocumented instanceof TPortType) {
                //e.g <wsdl:portType
                TPortType tDocumented1 = (TPortType) tDocumented;
                List<TOperation> operations = tDocumented1.getOperation();
                serviceOperations.addAll(operations);
            } else if (tDocumented instanceof TMessage) {
                //e.g <wsdl:message
                TMessage message = (TMessage) tDocumented;
                serviceMessages.put(message.getName(), message);
            }
        }

        getServiceElementNodes(serviceTypes);

        List<Operation> operations = new ArrayList<>();
        for (TOperation tOperation : serviceOperations) {
            List<TMessage> outputMessages = getMessagesFromOperation(tOperation, serviceMessages, messageTypeOutput);
            List<Parameter> outputParameters = getParametersFromMessages(outputMessages);
            List<TMessage> inputMessages = getMessagesFromOperation(tOperation, serviceMessages, messageTypeInput);
            List<Parameter> inputParameters = getParametersFromMessages(inputMessages);

            Operation operation = new Operation(tOperation.getName());
            operation.addInputParameterList(inputParameters);
            operation.addOutputParameterList(outputParameters);

            if (!(operation.getInputParameterList().isEmpty() && operation.getOutputParameterList().isEmpty())) {
                operations.add(operation);
            }
        }

        return operations;
    }

    private void getServiceElementNodes(TTypes types) {
        for (Object object : types.getAny()) {
            if (object instanceof ElementNSImpl) {
                ElementNSImpl element = (ElementNSImpl) object;
                Node node = element.getFirstChild();
                while (node != null && !(node instanceof TextImpl)) {
                    switch (node.getLocalName()) {
                        case "element": {
                            handleElementTypes(node);
                            break;
                        }
                        case "complexType": {
                            handleComplexTypes(node);
                            break;
                        }
                        case "simpleType": {
                            handleSimpleTypes(node);
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                    node = node.getNextSibling();
                }
            }
        }
    }

    //hanldes <s:simpleType
    private void handleSimpleTypes(Node node) {
        String name = getNodeNameAttributeValue(node);
        Parameter parameter = new Parameter(name);
        parameter.modelReference = getNodeModelReferenceAttributeValue(node);
        typeParametersMap.put(parameter.name, parameter);
    }

    //handles <s:complexType and populates typeParameters
    private void handleComplexTypes(Node node) {
        Parameter parameter = new Parameter(node.getAttributes().getNamedItem("name").getNodeValue(), null);
        parameter.modelReference = getNodeModelReferenceAttributeValue(node);
        Node sequenceNode = node.getFirstChild();
        if (sequenceNode != null) {
            if (sequenceNode.getNodeName().contains("sequence") || sequenceNode.getNodeName().contains("all")) {
                Node sequenceElement = sequenceNode.getFirstChild();
                if (sequenceElement != null) {
                    parameter.subParameters = new ArrayList<>();
                    while (sequenceElement != null && !(sequenceElement instanceof TextImpl)) {
                        String name = getNodeNameAttributeValue(sequenceElement);
                        Parameter nestedParameter = new Parameter(name);
                        nestedParameter.modelReference = getNodeModelReferenceAttributeValue(sequenceElement);
                        parameter.subParameters.add(nestedParameter);
                        sequenceElement = sequenceElement.getNextSibling();
                    }
                }
            }
        }

        typeParametersMap.put(parameter.name, parameter);
    }

    //handles  <s:element types and poulates elementParameters
    private void handleElementTypes(Node elementNode) {
        Parameter parameter = new Parameter(elementNode.getAttributes().getNamedItem("name").getNodeValue(), null);
        // get first child of element
        Node complexTypeNode = elementNode.getFirstChild();
        if (complexTypeNode != null) {
            if (complexTypeNode.getNodeName().contains("complexType")) {
                parameter.modelReference = getNodeModelReferenceAttributeValue(elementNode);
                Node sequenceNode = complexTypeNode.getFirstChild();
                if (sequenceNode != null) {
                    if (sequenceNode.getNodeName().contains("sequence") || sequenceNode.getNodeName().contains("all")) {
                        Node sequenceChild = sequenceNode.getFirstChild();
                        if (sequenceChild != null) {
                            parameter.subParameters = new ArrayList<>();
                            while (sequenceChild != null && !(sequenceChild instanceof TextImpl)) {
                                String name = getNodeNameAttributeValue(sequenceChild);
                                Parameter nestedParameter = new Parameter(name);
                                nestedParameter.modelReference = getNodeModelReferenceAttributeValue(sequenceChild);
                                parameter.subParameters.add(nestedParameter);
                                sequenceChild = sequenceChild.getNextSibling();
                            }
                        }
                    }
                }
            }
        } else {
            //e.g.  <s:element minOccurs="0" maxOccurs="1" name="IsValidSimpleResult" type="tns:CreditCardValidationResponse" />
            parameter.type = getNodeAttributeValue(elementNode, "type");
            parameter.ref = getNodeAttributeValue(elementNode, "ref");
        }

        elementParametersMap.put(parameter.name, parameter);
    }

    private String getNodeAttributeValue(Node elementNode, String attributeName) {
        Node node = elementNode.getAttributes().getNamedItem(attributeName);
        String attributeValue = null;
        if (node != null) {
            String value = node.getNodeValue();
            String[] split = value.split(":");

            if (split.length > 1) {
                attributeValue = split[1];
            } else {
                attributeValue = split[0];
            }
        }
        return attributeValue;
    }

    private List<TMessage> getMessagesFromOperation(TOperation operation, Map<String, TMessage> messages, String messageType) {
        List<TMessage> messagesList = new ArrayList<>();
        for (JAXBElement<? extends TExtensibleAttributesDocumented> element : operation.getRest()) {
            if (element.getName().getLocalPart().equalsIgnoreCase(messageType)) {
                if (element.getValue() instanceof TParam) {
                    TParam param = (TParam) element.getValue();
                    TMessage message = messages.get(param.getMessage().getLocalPart());
                    if (message != null) {
                        messagesList.add(message);
                    }
                }
            }
        }

        return messagesList;
    }

    private List<Parameter> getParametersFromMessages(List<TMessage> tMessages) {
        List<Parameter> parameterList = new ArrayList<>();
        for (TMessage tMessage : tMessages) {
            for (TPart tPart : tMessage.getPart()) {
                if (tPart.getType() != null) {
                    if (validTypes.contains(tPart.getType().getLocalPart())) {
                        Parameter parameter = new Parameter(tPart.getName());
                        for (QName qName : tPart.getOtherAttributes().keySet()) {
                            if (qName.getLocalPart().equals("modelReference")) {
                                String value = tPart.getOtherAttributes().get(qName);
                                String[] split = value.split("#");
                                if (split.length > 1) {
                                    parameter.modelReference = split[1];
                                }
                            }
                        }
                        parameterList.add(parameter);
                    } else {
                        Parameter parameter = typeParametersMap.get(tPart.getType().getLocalPart());
                        if (parameter != null) {
                            parameterList.add(parameter);
                        }
                    }
                } else if (tPart.getElement() != null) {
                    Parameter parameter = elementParametersMap.get(tPart.getElement().getLocalPart());
                    if (parameter != null) {
                        if (parameter.type != null && !parameter.hasModelReference()) {
                            Parameter nestedParameter = typeParametersMap.get(parameter.type);
                            if (nestedParameter != null) {
                                parameter = nestedParameter;
                            }
                        } else if (parameter.ref != null) {
                            Parameter nestedParameter = elementParametersMap.get(parameter.ref);
                            if (nestedParameter != null) {
                                parameter = nestedParameter;
                            }
                        }

                        parameterList.add(parameter);
                    }
                }
            }
        }

        return parameterList;
    }

    private String getNodeNameAttributeValue(Node node) {
        String value = getNodeAttributeValue(node, "name");
        if (value == null) {
            //look for ref attribute
            value = getNodeAttributeValue(node, "ref");
        }
        return value;
    }


    private String getNodeModelReferenceAttributeValue(Node node) {
        String attributeValue = "";
        Node attributeNode = node.getAttributes().getNamedItem("sawsdl:modelReference");
        if (attributeNode != null) {
            String value = attributeNode.getNodeValue();
            String[] split = value.split("#");
            if (split.length > 1) {
                attributeValue = split[1];
            } else {
                attributeValue = split[0];
            }
        }
        return attributeValue;
    }


}
