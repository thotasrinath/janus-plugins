package io.cdap.plugin.janus.examples;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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

        Map<String, Object> mapConfig = new HashMap<String, Object>();
        mapConfig.put("gremlin.remote.remoteConnectionClass",
                "org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection");
        mapConfig.put("gremlin.remote.driver.sourceName", "g");


        Configuration janusConf = ConfigurationUtil.loadMapConfiguration(mapConfig);

        Configuration configuration = ConfigurationUtil.createBaseConfiguration();
        configuration.addProperty("hosts", "localhost");
        configuration.addProperty("port", String.valueOf(8182));
        configuration.addProperty("serializer.className",
                "org.apache.tinkerpop.gremlin.driver.ser.GryoMessageSerializerV3d0");

        Map<String, Object> serializerConfig = new HashMap<String, Object>();
        mapConfig.put("ioRegistries", "org.janusgraph.graphdb.tinkerpop.JanusGraphIoRegistry");


        configuration.addProperty("serializer.config", serializerConfig);

        // using the remote driver for schema
        try {
            Cluster cluster = Cluster.open(configuration);
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

// properties
        s.append("PropertyKey ident = management.makePropertyKey(\"ident\").dataType(String.class).make(); ");
        s.append("PropertyKey type = management.makePropertyKey(\"type\").dataType(String.class).make(); ");
        s.append("PropertyKey name = management.makePropertyKey(\"name\").dataType(String.class).make(); ");
        // s.append("PropertyKey airport_name = management.makePropertyKey(\"airport_name\").dataType(String.class).make(); ");
        s.append(
                "PropertyKey airport_wikipedia_link = management.makePropertyKey(\"airport_wikipedia_link\").dataType(String.class).make(); ");
        s.append(
                "PropertyKey country_code = management.makePropertyKey(\"country_code\").dataType(String.class).make(); ");
        //  s.append("PropertyKey country_name = management.makePropertyKey(\"country_name\").dataType(String.class).make(); ");
        s.append(
                "PropertyKey country_continent = management.makePropertyKey(\"country_continent\").dataType(String.class).make(); ");
        s.append(
                "PropertyKey country_wikipedia_link = management.makePropertyKey(\"country_wikipedia_link\").dataType(String.class).make(); ");
        s.append("PropertyKey country_id = management.makePropertyKey(\"country_id\").dataType(String.class).make(); ");
        s.append("PropertyKey airport_id = management.makePropertyKey(\"airport_id\").dataType(String.class).make(); ");


        // vertex labels
        s.append("management.makeVertexLabel(\"Airport\").make(); ");
        s.append("management.makeVertexLabel(\"Country\").make(); ");


        // edge labels
        s.append("management.makeEdgeLabel(\"belongsTo\").multiplicity(Multiplicity.MANY2ONE).make(); ");


        // composite indexes
        // s.append("management.buildIndex(\"nameIndex\", Vertex.class).addKey(name).buildCompositeIndex(); ");

        // mixed indexes

        s.append("management.commit(); created = true; ");

        return s.toString();
    }
}


