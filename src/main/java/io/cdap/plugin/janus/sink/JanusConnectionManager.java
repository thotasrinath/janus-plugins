package io.cdap.plugin.janus.sink;

import io.cdap.plugin.janus.common.JanusCustomConfiguration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;

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
}
