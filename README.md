# Janus-Plugins for CDAP

## Features
- A generic plugin for data ingestion into JanusGraph.
- Configuration based Vertex and Edge creation.
- Connection Management for Janusgraph.

## Configuration for JanusSkin Plugin
- Reference Name 
- Hosts
- Port
- Remote Connection Class
- Serializer Classname
- IO Registry
- Graph Source Name
- Record To Vertex Configurer

## Record To Vertex Configurer :-
- Record to vertex configuration is the Json based configuration which helps to know how the Structured Record needs to be processed.
- Below is the format
```json
{
  "NODE_LIST": [
    {
      "label": "Field1",
      "id": "Field2",
      "isHardCodedLabel": true,
      "properties": [
        "Field3",
        "Field4"
      ]
    }
  ],
  "EDGE_LIST": [
    {
      "label": "Field1",
      "id": "Field2",
      "isHardCodedLabel": true,
      "fromLabel": "fromLabel",
      "toLabel": "toLabel",
      "properties": [
        "Field3",
        "Field4"
      ]
    }
  ]
}
```
## Sample Run

### Pipeline
[CDAP Pipeline](Test1_v4-cdap-data-pipeline.json)

![alt text](FirstRun-Pipeline.png)

### JanusSink Configuration
![alt text](JanusConfig.png)

### Results

![alt text](FirstRun-Data.png)


## Connector Plugin

### Janus Connection Listing

![alt text](Connection_Listing.png)

### Populating Config for Janus Connection

![alt text](Populating_Config.png)
