import org.xml.sax.*;

import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public class VypisDovoleniek 
{
  public static void main(String[] args) throws ParserConfigurationException, IOException, TransformerException, SAXException {

    try
    {
      SAXParserFactory spf = SAXParserFactory.newInstance();
      spf.setValidating(false);
      SAXParser p = spf.newSAXParser();
      XMLReader parser = p.getXMLReader();
      parser.setErrorHandler(new VypisChyb());
      ParserDovoleniek d = new ParserDovoleniek();
      parser.setContentHandler(d);
      parser.parse("src\\dovolenky.xml");
      d.vypisDovolenky();
    }
    catch (Exception e) 
    {
      e.printStackTrace();
    }
    System.out.println();


    dovolenkyDom();


  }

  public static void dovolenkyDom() throws ParserConfigurationException, IOException, SAXException, TransformerException {
    ParserDovoleniekDOM parser = new ParserDovoleniekDOM();
    parser.parse("src\\dovolenky.xml");
    parser.writeXMLToFile("dovolenky2.xml");
  }


}

