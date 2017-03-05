package com.jyoti.ws.project.util;

import com.jyoti.webservice.ObjectFactory;
import com.jyoti.webservice.TDefinitions;
import com.jyoti.webservice.WSMatchingType;

import javax.xml.bind.*;
import java.io.File;

public class JAXBUtils {
    public static TDefinitions createWsdlFileDefinitions(File file) {
        System.out.println("Parsing wsdl file: " + file.getName());
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance("com.jyoti.webservice");
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            JAXBElement<TDefinitions> definitions = (JAXBElement<TDefinitions>) unmarshaller.unmarshal(file);

            return definitions.getValue();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void createOutputFile(WSMatchingType wsMatchingType, String fileName) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance("com.jyoti.webservice");
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            ObjectFactory objectFactory = new ObjectFactory();

            JAXBElement<WSMatchingType> wsMatching = objectFactory.createWSMatching(wsMatchingType);
            marshaller.marshal(wsMatching, new File(fileName));
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
