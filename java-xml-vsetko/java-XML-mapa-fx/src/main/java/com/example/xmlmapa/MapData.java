package com.example.xmlmapa;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class MapData {
    String fileName;
    ArrayList<MyNode> allMyNodes = new ArrayList<>();
    ArrayList<MyWay> allMyWays = new ArrayList<>();


    public MapData(String fileName) {
        this.fileName = fileName;
    }

    public void generateData() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(this.fileName));
            doc.getDocumentElement().normalize();

            NodeList nodes = doc.getElementsByTagName("node");
            NodeList allWays = doc.getElementsByTagName("way");

            for(int i=0; i <nodes.getLength(); i++){
                Node node = nodes.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE){
                    Element e = (Element) node;
                    if(e.hasAttribute("id") && e.hasAttribute("lat") && e.hasAttribute("lon")){
                        MyNode newNode = new MyNode(Long.parseLong(e.getAttribute("id")), Double.parseDouble(e.getAttribute("lat")), Double.parseDouble(e.getAttribute("lon")));
                        this.allMyNodes.add(newNode);
                    }
                }
            }

            for(int i = 0; i < allWays.getLength(); i++){
                Node node = allWays.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE){
                    Element element = (Element) node;
                    NodeList elementsNodes = element.getElementsByTagName("tag");

                    for(int j = 0; j < elementsNodes.getLength(); j++){
                        Node n = elementsNodes.item(j);
                        if(n.getNodeType() == Node.ELEMENT_NODE){
                            Element e = (Element) n;
                            if(e.hasAttribute("k") && e.hasAttribute("v") &&
                                    e.getAttribute("k").equals("highway") && e.getAttribute("v").equals("footway")){
                                MyWay newWay = new MyWay();
                                NodeList nodesInWay = element.getElementsByTagName("nd");
                                for(int k = 0; k < nodesInWay.getLength(); k++){
                                    Node nn = nodesInWay.item(k);
                                    if(nn.getNodeType() == Node.ELEMENT_NODE){
                                        Element ee = (Element) nn;
                                        newWay.addNodeId(Long.parseLong(ee.getAttribute("ref")));
                                    }

                                }
                                this.allMyWays.add(newWay);
                            }
                        }
                    }

                }
            }

    } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }}
