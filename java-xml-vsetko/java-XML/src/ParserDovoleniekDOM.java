import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.io.OutputStream;

public class ParserDovoleniekDOM {
    ArrayList<Dovolenka> dovolenky = new ArrayList<>();
    int spoluDni = 0;

    public void parse(String fileName) throws ParserConfigurationException, IOException, SAXException {
        //Get Document Builder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        //Build Document
        Document document = builder.parse(new File(fileName));

        //Normalize the XML Structure; It's just too important !!
        document.getDocumentElement().normalize();

        //Here comes the root node
        Element root = document.getDocumentElement();
        System.out.println(root.getNodeName());

        //Get all employees
        NodeList nList = document.getElementsByTagName("dovolenka");

        this.spoluDni = 0;
        for (int temp = 0; temp < nList.getLength(); temp++)
        {
            Node node = nList.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                Element eElement = (Element) node;
                Element miesto = (Element) eElement.getElementsByTagName("miesto").item(0);
                System.out.print(eElement.getElementsByTagName("rok").item(0).getTextContent() + ": ");
                System.out.print(eElement.getElementsByTagName("miesto").item(0).getTextContent() + " " );
                System.out.print("(" + miesto.getAttribute("krajina") + ") - ");
                System.out.println(eElement.getElementsByTagName("pocetDni").item(0).getTextContent());

                Dovolenka newDovolenka = new Dovolenka(eElement.getElementsByTagName("miesto").item(0).getTextContent(), miesto.getAttribute("krajina"),
                        Integer.parseInt(eElement.getElementsByTagName("pocetDni").item(0).getTextContent()), Integer.parseInt(eElement.getElementsByTagName("rok").item(0).getTextContent()));
                this.dovolenky.add(newDovolenka);

                this.spoluDni += Integer.parseInt(eElement.getElementsByTagName("pocetDni").item(0).getTextContent());

            }
        }
        System.out.println("Celkovy pocet dni na dolovenkach: " + spoluDni);
    }


    public void writeXMLToFile(String fileName) throws ParserConfigurationException, TransformerException {
        //System.out.println(this.dovolenky);

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("dovolenky2");
        doc.appendChild(rootElement);


        int id = 1;
        for(int i = 0; i<this.dovolenky.size(); i++){
            Dovolenka d = this.dovolenky.get(i);
            Element eDovolenka = doc.createElement("dovolenka");
            eDovolenka.setAttribute("id",id+"");


            Element miesto = doc.createElement("miesto");
            miesto.setAttribute("krajina", d.getKrajina());
            miesto.setTextContent(d.getMiesto());
            Element rok = doc.createElement("rok");
            rok.setTextContent(d.getRok()+"");
            Element pocetDni = doc.createElement("pocetDni");
            pocetDni.setTextContent(d.getPocetDni()+"");

            eDovolenka.appendChild(miesto);
            eDovolenka.appendChild(rok);
            eDovolenka.appendChild(pocetDni);
            rootElement.appendChild(eDovolenka);
            id++;
        }

        Element spoluDni = doc.createElement("spoluDni");
        spoluDni.setTextContent(this.spoluDni+"");
        rootElement.appendChild(spoluDni);



        //...create XML elements, and others...

        // write dom document to a file
        try (FileOutputStream output =
                     new FileOutputStream("src\\dovolenky2.xml")) {
            writeXml(doc, output);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeXml(Document doc, OutputStream output) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        // pretty print XML
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);

        transformer.transform(source, result);

        System.out.println();
        System.out.println("Uspesne zapisane do suboru dovolenky2.xml");
    }
}
