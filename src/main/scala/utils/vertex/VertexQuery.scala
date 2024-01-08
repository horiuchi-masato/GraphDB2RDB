package utils.vertex

import com.typesafe.scalalogging.StrictLogging
import gremlin.scala.{GremlinScala, Vertex}
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource

import scala.util.control.NonFatal

final case class VertexQuery(g: GraphTraversalSource) extends StrictLogging {

  /** get the number of all vertices
    *
    * @return
    *   the number of all vertices
    */
  def countAll: Long = GremlinScala(g.V()).count().head()

  /** get Vertices List
    *
    * @param start
    *   The position to start retrieving Vertices from (0-based index).
    * @param count
    *   The number of Vertices to retrieve.
    * @return
    *   A list of Vertices based on the specified pagination parameters.
    */
  def getVerticesList(start: Int, count: Int): Seq[Vertex] = {
    require(start >= 0, "start must be positive.")
    require(count >= 0, "count must be positive.")

    try {
      GremlinScala(g.V()).range(start, start + count).toList()
    } catch {
      case NonFatal(e) =>
        logger.error(
          s"An exception has occurred when getVerticesList is called. start: $start, count: $count",
          e
        )
        throw e
    }
  }
}
