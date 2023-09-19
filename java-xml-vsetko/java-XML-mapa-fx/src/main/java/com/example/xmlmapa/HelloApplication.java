package com.example.xmlmapa;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class HelloApplication extends Application {
    ArrayList<MyNode> allMyNodes;
    ArrayList<MyWay> allMyWays;
    Double xStart;
    Double yStart;
    Double lonScale;
    Double latScale;

    @Override
    public void start(Stage stage) throws IOException, ParserConfigurationException {

        MapData mapData = new MapData("src\\map.osm");
        mapData.generateData();
        this.allMyNodes = mapData.allMyNodes;
        this.allMyWays = mapData.allMyWays;
        Group root = new Group();
        calculateLatAndLonData();

        for(int i=0; i<allMyWays.size();i++){
            MyWay way = allMyWays.get(i);
            ArrayList<Line> linesForOneWay = drawOneWay(way, allMyNodes);
            for (int j = 0; j < linesForOneWay.size(); j++){
                root.getChildren().add(linesForOneWay.get(j));
            }

        }

        //Creating a Scene
        Scene scene = new Scene(root, 1000, 800);

        //Setting title to the scene
        stage.setTitle("Sample application");

        //Adding the scene to the stage
        stage.setScene(scene);

        //Displaying the contents of a scene
        stage.show();

    }


    public void calculateLatAndLonData(){
        MyNode first = getNodeById(this.allMyWays.get(0).getIdsOfNodes().get(0), this.allMyNodes);
        Double maxLon = first.getLon();
        Double minLon = first.getLon();
        Double maxLat = first.getLat();
        Double minLat = first.getLat();

        for(int i = 0; i < this.allMyWays.size();i++){
            for (int j = 0; j<this.allMyWays.get(i).getIdsOfNodes().size(); j++){
                MyNode n = getNodeById(this.allMyWays.get(i).getIdsOfNodes().get(j), this.allMyNodes);
                if(n.getLon() > maxLon){
                    maxLon = n.getLon();
                }
                if(n.getLon() < minLon){
                    minLon = n.getLon();
                }
                if(n.getLat() > maxLat){
                    maxLat = n.getLat();
                }
                if(n.getLat() < minLat){
                    minLat = n.getLat();
                }
            }
        }

        Double lonDifference = maxLon - minLon;
        Double latDifference = maxLat - minLat;

        this.lonScale = (1000 - 1) / lonDifference;
        this.latScale = (800 - 1) / latDifference;

        this.xStart = - minLon * lonScale;
        this.yStart = maxLat * latScale;

    }

    public Double calculateX(Double lon){
        return lon * this.lonScale + this.xStart;
    }

    public Double calculateY(Double lat){
        return this.yStart - lat * this.latScale;
    }

    public static void main(String[] args) {launch();}

    public ArrayList<Line> drawOneWay(MyWay way, ArrayList<MyNode> allNodes){
        ArrayList<Line> lines = new ArrayList<>();
        for(int i = 0; i<way.getIdsOfNodes().size() - 1; i++){
            MyNode startNode = getNodeById(way.getIdsOfNodes().get(i), allNodes);
            MyNode endNode = getNodeById(way.getIdsOfNodes().get(i+1),allNodes);
            Line newLine = drawOneLine(startNode, endNode);
            lines.add(newLine);

        }
        return lines;

    }


    public static MyNode getNodeById(Long id, ArrayList<MyNode> allNodes){
        for(int i = 0 ;i < allNodes.size(); i++){
            MyNode node = allNodes.get(i);
            if(node.getId() == id){
                return node;
            }
        }
        return null;
    }

    public Line drawOneLine(MyNode startNode, MyNode endNode){
        Line line = new Line();
        line.setStartX(calculateX(startNode.getLon()));
        line.setStartY(calculateY(startNode.getLat()));
        line.setEndX(calculateX(endNode.getLon()));
        line.setEndY(calculateY(endNode.getLat()));
        return line;

    }
}