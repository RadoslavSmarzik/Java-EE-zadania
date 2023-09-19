import org.xml.sax.*;

public class VypisChyb implements ErrorHandler 
{

  private String chyba(SAXParseException e) 
  {
    return e.getSystemId() + "\nriadok: " + e.getLineNumber() + 
           " stlpec: " + e.getColumnNumber() + "\n" + e.getMessage();
  }

  public void warning(SAXParseException e) 
  {
    System.out.println("Varovanie: " + chyba(e));
  }

  public void error(SAXParseException e) throws SAXException 
  {
    throw new SAXException("Chyba: " + chyba(e));
  }

  public void fatalError(SAXParseException e) throws SAXException 
  {
    throw new SAXException("Kriticka chyba: " + chyba(e));
  }
}
