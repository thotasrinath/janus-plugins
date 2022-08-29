package io.cdap.plugin.janus.examples;


import io.cdap.plugin.janus.common.JanusCustomConfiguration;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.janusgraph.util.system.ConfigurationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JanusGraphSchemaMgmtApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(JanusGraphSchemaMgmtApp.class);

    public static void main(String[] args) throws ConfigurationException, IOException {
        JanusGraphSchemaMgmtApp janusGraphSchemaMgmtApp = new JanusGraphSchemaMgmtApp();

        janusGraphSchemaMgmtApp.configureSchema();

    }


    public void configureSchema() throws ConfigurationException, IOException {


        final JanusCustomConfiguration janusCustomConfiguration = new JanusCustomConfiguration();

        janusCustomConfiguration.addPropertyConfigDirect("hosts",
                Arrays.stream("localhost".split(",")).collect(Collectors.toList()));
        janusCustomConfiguration.addPropertyConfigDirect("port", "8182");
        janusCustomConfiguration.addPropertyConfigDirect("serializer.className",
                "org.apache.tinkerpop.gremlin.driver.ser.GryoMessageSerializerV3d0");
        janusCustomConfiguration.addPropertyConfigDirect("serializer.config.ioRegistries",
                Arrays.stream("org.janusgraph.graphdb.tinkerpop.JanusGraphIoRegistry".split(",")).collect(
                        Collectors.toList())); // (e.g. [ org.janusgraph.graphdb.tinkerpop.JanusGraphIoRegistry) ]
        janusCustomConfiguration.addPropertyConfigDirect("gremlin.remote.remoteConnectionClass",
                "org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection");
        janusCustomConfiguration.addPropertyConfigDirect("gremlin.remote.driver.sourceName",
                "g");


        // using the remote driver for schema
        try {
            Cluster cluster = Cluster.open(janusCustomConfiguration);
            Client client = cluster.connect();
            LOGGER.info("creating schema");
            // get the schema request as a string
            final String req = createSchemaRequest();
            // submit the request to the server
            final ResultSet resultSet = client.submit(req);
            // drain the results completely
            Stream<Result> futureList = resultSet.stream();
            futureList.map(Result::toString).forEach(System.out::println);
            client.close();
            cluster.close();

            System.out.println("Done");
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Error while connecting", e);
        }

    }


    protected String createSchemaRequest() {
        final StringBuilder s = new StringBuilder();

        s.append("JanusGraphManagement management = graph.openManagement(); ");
        s.append("boolean created = false; ");

        // naive check if the schema was previously created
        s.append(
                "if (management.getRelationTypes(RelationType.class).iterator().hasNext()) { management.rollback(); created = false; } else { ");

// properties
        s.append("PropertyKey id = management.makePropertyKey(\"id\").dataType(String.class).make(); ");
        s.append("PropertyKey ident = management.makePropertyKey(\"ident\").dataType(String.class).make(); ");
        s.append("PropertyKey type = management.makePropertyKey(\"type\").dataType(String.class).make(); ");
        s.append("PropertyKey name = management.makePropertyKey(\"name\").dataType(String.class).make(); ");
        s.append(
                "PropertyKey airport_wikipedia_link = management.makePropertyKey(\"airport_wikipedia_link\").dataType(String.class).make(); ");
        s.append(
                "PropertyKey country_code = management.makePropertyKey(\"country_code\").dataType(String.class).make(); ");
        s.append(
                "PropertyKey country_continent = management.makePropertyKey(\"country_continent\").dataType(String.class).make(); ");
        s.append(
                "PropertyKey country_wikipedia_link = management.makePropertyKey(\"country_wikipedia_link\").dataType(String.class).make(); ");
        s.append("PropertyKey countryId = management.makePropertyKey(\"countryId\").dataType(String.class).make(); ");
        s.append("PropertyKey airportId = management.makePropertyKey(\"airportId\").dataType(String.class).make(); ");


        // vertex labels
        s.append("VertexLabel airportLabel = management.makeVertexLabel(\"Airport\").make(); ");
        s.append("VertexLabel countryLabel = management.makeVertexLabel(\"Country\").make(); ");


        // edge labels
        s.append("management.makeEdgeLabel(\"belongsTo\").multiplicity(Multiplicity.MANY2ONE).make(); ");


        // composite indexes
        s.append("management.buildIndex(\"airportIdIndex\", Vertex.class).addKey(airportId).indexOnly(airportLabel).buildCompositeIndex(); ");
        s.append("management.buildIndex(\"countryIdIndex\", Vertex.class).addKey(countryId).indexOnly(countryLabel).buildCompositeIndex(); ");


        // mixed indexes
        s.append("management.commit(); created = true; }");

        return s.toString();
    }
}


