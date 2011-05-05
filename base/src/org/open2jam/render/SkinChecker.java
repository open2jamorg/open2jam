package org.open2jam.render;

import java.io.*;
import java.net.URL;
import java.util.logging.Level;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import org.open2jam.util.Logger;
import org.xml.sax.SAXException;


public class SkinChecker {

    String schemaLocation = "/resources/skin_schema.xsd";

    public boolean check(String xmlFile) throws SAXException, IOException {

        InputStream input_stream = null;
        URL url = SkinChecker.class.getResource(xmlFile);
        if(url == null)
        {
            File file = new File(xmlFile);
            if(!file.exists())
            {
                Logger.global.log(Level.SEVERE, "There is no xml file {0}", xmlFile);
                return false;
            }
            input_stream = new FileInputStream(file);
        }
        else
        {
            input_stream = url.openStream();
        }
        
        if(input_stream == null)
            Logger.global.log(Level.SEVERE, "There is no xml file {0}", xmlFile);

        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        URL schema_file = SkinChecker.class.getResource(schemaLocation);
        Schema schema = factory.newSchema(schema_file);

        Validator validator = schema.newValidator();

        Source source = new StreamSource(input_stream);

        try {
            validator.validate(source);
            System.out.println(xmlFile + " is valid! ");
            return true; //it's valid.
        }
        catch (SAXException ex) {
            System.out.println(xmlFile + " is not valid because ");
            System.out.println(ex.getMessage());
            return false;
        }
    }
}
