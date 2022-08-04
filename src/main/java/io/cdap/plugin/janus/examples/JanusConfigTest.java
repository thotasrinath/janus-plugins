package io.cdap.plugin.janus.examples;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.tinkerpop.gremlin.process.traversal.Bindings;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.util.system.ConfigurationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;

public class JanusConfigTest {

    private static final String NAME = "name";
    private static final String AGE = "age";
    private static final String TIME = "time";
    private static final String REASON = "reason";
    private static final String PLACE = "place";
    private static final String LABEL = "label";
    private static final String OUT_V = "outV";
    private static final String IN_V = "inV";

    private static final Logger LOGGER = LoggerFactory.getLogger(JanusConfigTest.class);

    public static void main(String[] args) throws ConfigurationException, IOException {
        JanusConfigTest janusGraphSchemaMgmtApp = new JanusConfigTest();

        janusGraphSchemaMgmtApp.configureSchema();

    }

    public void configureSchema() throws ConfigurationException, IOException {


        Map<String, Object> mapConfig = new HashMap<String, Object>();
        mapConfig.put("gremlin.remote.remoteConnectionClass", "org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection");
        mapConfig.put("gremlin.remote.driver.sourceName", "g");
        mapConfig.put("gremlin.remote.driver.clusterFile","remote-objects.yaml");


        Configuration janusConf = ConfigurationUtil.loadMapConfiguration(mapConfig);

        Configuration configuration = ConfigurationUtil.createBaseConfiguration();
        configuration.addProperty("hosts", "localhost");
        configuration.addProperty("port", String.valueOf(8182));
        configuration.addProperty("serializer.className", "org.apache.tinkerpop.gremlin.driver.ser.GryoMessageSerializerV3d0");

        Map<String, Object> serializerConfig = new HashMap<String, Object>();
        serializerConfig.put("ioRegistries", "org.janusgraph.graphdb.tinkerpop.JanusGraphIoRegistry");


        configuration.addProperty("serializer.config", serializerConfig);

        DefaultListDelimiterHandler COMMA_DELIMITER_HANDLER = new DefaultListDelimiterHandler(',');

        final MyConfiguration config = new MyConfiguration();
        config.setListDelimiterHandler(COMMA_DELIMITER_HANDLER);

        config.addPropertyNew("clusterConfiguration.hosts", Arrays.asList("localhost"));
        config.addPropertyNew("clusterConfiguration.port",  String.valueOf(8182));
        config.addPropertyNew("clusterConfiguration.serializer.className", "org.apache.tinkerpop.gremlin.driver.ser.GryoMessageSerializerV1d0");
        config.addPropertyNew("clusterConfiguration.serializer.config.ioRegistries", Arrays.asList("org.janusgraph.graphdb.tinkerpop.JanusGraphIoRegistry")); // (e.g. [ org.janusgraph.graphdb.tinkerpop.JanusGraphIoRegistry) ]
        config.addPropertyNew("gremlin.remote.remoteConnectionClass", "org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection");
        config.addPropertyNew("gremlin.remote.driver.sourceName", "g");


        GraphTraversalSource graphTraversalSource = traversal().withRemote(janusConf) ;
        createElements(graphTraversalSource);
        System.out.println("Done");

    }

    public void createElements(GraphTraversalSource g) {
        LOGGER.info("creating elements");

        // Use bindings to allow the Gremlin Server to cache traversals that
        // will be reused with different parameters. This minimizes the
        // number of scripts that need to be compiled and cached on the server.
        // https://tinkerpop.apache.org/docs/3.2.6/reference/#parameterized-scripts
        final Bindings b = Bindings.instance();

        // see GraphOfTheGodsFactory.java

        Vertex saturn = g.addV(b.of(LABEL, "titan")).property(NAME, b.of(NAME, "saturn"))
                .property(AGE, b.of(AGE, 10000)).next();
        Vertex sky = g.addV(b.of(LABEL, "location")).property(NAME, b.of(NAME, "sky")).next();
        Vertex sea = g.addV(b.of(LABEL, "location")).property(NAME, b.of(NAME, "sea")).next();
        Vertex jupiter = g.addV(b.of(LABEL, "god")).property(NAME, b.of(NAME, "jupiter")).property(AGE, b.of(AGE, 5000))
                .next();
        Vertex neptune = g.addV(b.of(LABEL, "god")).property(NAME, b.of(NAME, "neptune")).property(AGE, b.of(AGE, 4500))
                .next();
        Vertex hercules = g.addV(b.of(LABEL, "demigod")).property(NAME, b.of(NAME, "hercules"))
                .property(AGE, b.of(AGE, 30)).next();
        Vertex alcmene = g.addV(b.of(LABEL, "human")).property(NAME, b.of(NAME, "alcmene")).property(AGE, b.of(AGE, 45))
                .next();
        Vertex pluto = g.addV(b.of(LABEL, "god")).property(NAME, b.of(NAME, "pluto")).property(AGE, b.of(AGE, 4000))
                .next();
        Vertex nemean = g.addV(b.of(LABEL, "monster")).property(NAME, b.of(NAME, "nemean")).next();
        Vertex hydra = g.addV(b.of(LABEL, "monster")).property(NAME, b.of(NAME, "hydra")).next();
        Vertex cerberus = g.addV(b.of(LABEL, "monster")).property(NAME, b.of(NAME, "cerberus")).next();
        Vertex tartarus = g.addV(b.of(LABEL, "location")).property(NAME, b.of(NAME, "tartarus")).next();

        g.V(b.of(OUT_V, jupiter)).as("a").V(b.of(IN_V, saturn)).addE(b.of(LABEL, "father")).from("a").next();
        g.V(b.of(OUT_V, jupiter)).as("a").V(b.of(IN_V, sky)).addE(b.of(LABEL, "lives"))
                .property(REASON, b.of(REASON, "loves fresh breezes")).from("a").next();
        g.V(b.of(OUT_V, jupiter)).as("a").V(b.of(IN_V, neptune)).addE(b.of(LABEL, "brother")).from("a").next();
        g.V(b.of(OUT_V, jupiter)).as("a").V(b.of(IN_V, pluto)).addE(b.of(LABEL, "brother")).from("a").next();

        g.V(b.of(OUT_V, neptune)).as("a").V(b.of(IN_V, sea)).addE(b.of(LABEL, "lives"))
                .property(REASON, b.of(REASON, "loves waves")).from("a").next();
        g.V(b.of(OUT_V, neptune)).as("a").V(b.of(IN_V, jupiter)).addE(b.of(LABEL, "brother")).from("a").next();
        g.V(b.of(OUT_V, neptune)).as("a").V(b.of(IN_V, pluto)).addE(b.of(LABEL, "brother")).from("a").next();

        g.V(b.of(OUT_V, hercules)).as("a").V(b.of(IN_V, jupiter)).addE(b.of(LABEL, "father")).from("a").next();
        g.V(b.of(OUT_V, hercules)).as("a").V(b.of(IN_V, alcmene)).addE(b.of(LABEL, "mother")).from("a").next();


        g.V(b.of(OUT_V, pluto)).as("a").V(b.of(IN_V, jupiter)).addE(b.of(LABEL, "brother")).from("a").next();
        g.V(b.of(OUT_V, pluto)).as("a").V(b.of(IN_V, neptune)).addE(b.of(LABEL, "brother")).from("a").next();
        g.V(b.of(OUT_V, pluto)).as("a").V(b.of(IN_V, tartarus)).addE(b.of(LABEL, "lives"))
                .property(REASON, b.of(REASON, "no fear of death")).from("a").next();
        g.V(b.of(OUT_V, pluto)).as("a").V(b.of(IN_V, cerberus)).addE(b.of(LABEL, "pet")).from("a").next();

        g.V(b.of(OUT_V, cerberus)).as("a").V(b.of(IN_V, tartarus)).addE(b.of(LABEL, "lives")).from("a").next();
    }
}
