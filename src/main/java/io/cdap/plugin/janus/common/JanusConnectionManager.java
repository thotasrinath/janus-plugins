package io.cdap.plugin.janus.common;

import static org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;

import io.cdap.plugin.janus.error.ConnectionFailure;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JanusConnectionManager {

    private static final Logger LOG = LoggerFactory.getLogger(JanusConnectionManager.class);

    private static GraphTraversalSource graphTraversalSource;

    public static GraphTraversalSource getGraphTraversalSource(JanusCustomConfiguration janusCustomConfiguration) {

        if (graphTraversalSource == null) {
            synchronized (JanusConnectionManager.class) {
                if (graphTraversalSource == null) {
                    LOG.info("Opening JanusGraph Connection");
                    graphTraversalSource = traversal().withRemote(janusCustomConfiguration);
                }
            }
        }

        return graphTraversalSource;
    }

    public static void closeGraphTraversalSource() {
        try {
            if (graphTraversalSource != null) {
                synchronized (JanusConnectionManager.class) {
                    if (graphTraversalSource != null) {
                        LOG.info("Closing JanusGraph Connection");
                        graphTraversalSource.close();
                        graphTraversalSource = null;
                    }
                }
            }

        } catch (Exception e) {
            LOG.error("Error while closing connection", e);
        }

    }

    public static void validateConnection(JanusCustomConfiguration janusCustomConfiguration) throws ConnectionFailure {

        try (GraphTraversalSource traversalSource = traversal().withRemote(janusCustomConfiguration)) {

            Vertex vertex = traversalSource.V().hasLabel("TestConnection")
                    .fold()
                    .coalesce(__.unfold(), __.addV("TestConnection"))
                    .next();
            if (vertex == null || !"TestConnection".equals(vertex.label())) {
                throw new Exception("Fetched vertex not appropriate");
            }
            traversalSource.tx().commit();

        } catch (Throwable e) {
            LOG.error("Error while testing Janus Connection", e);
            throw new ConnectionFailure(e.getMessage());
        }
    }
}
