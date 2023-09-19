package com.example.xmlmapa;

import java.util.ArrayList;

public class MyWay {
    private ArrayList<Long> idsOfNodes = new ArrayList<>();

    public void addNodeId(long id){
        this.idsOfNodes.add(id);
    }


    public ArrayList<Long> getIdsOfNodes() {
        return idsOfNodes;
    }
}
