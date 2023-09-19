import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class ParserDovoleniek extends DefaultHandler 
{
  private StringBuffer rok = new StringBuffer(10);
  private StringBuffer miesto = new StringBuffer(50);
  private String krajina;
  private boolean spracovavamRok, spracovavamMiesto;
  private TreeMap<Integer,Dovolenka> dovolenky;

  private StringBuffer pocetDni = new StringBuffer(10);
  private boolean spracovavamPocetDni;

  public void vypisDovolenky()
  {
    int celkovyPocetDni = 0;
    for (Iterator<Map.Entry<Integer,Dovolenka>> i = dovolenky.entrySet().iterator(); i.hasNext(); )
    {
      Map.Entry<Integer,Dovolenka> v = i.next();
      System.out.println(v.getKey() + ": " + v.getValue().getMiesto() + " - " + v.getValue().getPocetDni());
      celkovyPocetDni += v.getValue().getPocetDni();
    }
    System.out.println("Celkovy pocet dni na dovolenkach: " + celkovyPocetDni);
  }

  public void startDocument() 
  {
    dovolenky = new TreeMap<Integer,Dovolenka>();
  }

  public void startElement(String uri, String localName, String qName, Attributes atts) 
  {
    if (qName.equals("rok") == true) 
    {
      spracovavamRok = true;
      rok.setLength(0);
    }
    else if (qName.equals("miesto") == true)
    {
      spracovavamMiesto = true;
      krajina = atts.getValue("krajina");
      miesto.setLength(0);
    }

    else if (qName.equals("pocetDni") == true)
    {
      spracovavamPocetDni = true;
      pocetDni.setLength(0);
    }
  }    

  public void endElement(String uri, String localName, 
                        String qName) {
    if (qName.equals("rok") == true) 
    {
      spracovavamRok = false; 
    }
    else if (qName.equals("miesto") == true)
    {
      spracovavamMiesto = false;
    }
    else if (qName.equals("pocetDni") == true)
    {
      spracovavamPocetDni = false;
    }
    else if (qName.equals("dovolenka") == true) 
    {
      miesto.append(" (");
      miesto.append(krajina);
      miesto.append(")");

      dovolenky.put(Integer.parseInt(rok.toString()), new Dovolenka(miesto.toString(), Integer.parseInt(pocetDni.toString())));
      //dovolenky.put(new Integer(Integer.parseInt(rok.toString())), new Dovolenka(miesto.toString(), Integer.parseInt(pocetDni.toString())));
    }
  }    

  public void characters(char[] ch, int start, int length) 
  {
    if (spracovavamRok == true) 
    {  
      rok.append(ch, start, length);
    }        
    else if (spracovavamMiesto == true)
    { 
      miesto.append(ch, start, length);
    }
    else if (spracovavamPocetDni == true)
    {
      pocetDni.append(ch, start, length);
    }
  }
}
