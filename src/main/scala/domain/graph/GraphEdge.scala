package domain.graph

import domain.table.ddl.attribute.{ForeignKey, PrimaryKey, UniqueIndex}
import domain.table.ddl.column.{ColumnList, ColumnName, ColumnType}
import domain.table.ddl.{TableAttributes, TableList, TableName}
import domain.table.dml.{RecordId, RecordKey, RecordList, RecordValue}
import gremlin.scala.{Edge, GremlinScala}
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import utils.Config

import scala.jdk.CollectionConverters.MapHasAsScala

final case class GraphEdge(
    private val value: Edge,
    private val config: Config,
    private val g: GraphTraversalSource
) extends GraphElement {

  private val inVertex = value.inVertex()
  private val inVertexId = inVertex.id()
  private val inVertexLabel = inVertex.label()
  private val outVertex = value.outVertex()
  private val outVertexId = outVertex.id()
  private val outVertexLabel = outVertex.label()
  private val tableName = TableName(
    s"${config.tableName.edge}_${value.label()}_from_${outVertexLabel}_to_$inVertexLabel"
  )
  private val columnNamePrefixProperty = config.columnName.prefixProperty

  private val columnNameEdgeId = config.columnName.edgeId
  private val columnNameEdgeInVId = config.columnName.edgeInVId
  private val columnNameEdgeOutVId = config.columnName.edgeOutVId

  val id: AnyRef = value.id()

  /** convert to Database Table Information
    *
    * @return
    *   Database Table Information
    */
  override def toDdl: TableList =
    TableList {
      val idColumn = Map(ColumnName(columnNameEdgeId) -> ColumnType.apply(id))
      val inVColumn =
        Map(ColumnName(columnNameEdgeInVId) -> ColumnType.apply(inVertexId))
      val outVColumn =
        Map(ColumnName(columnNameEdgeOutVId) -> ColumnType.apply(outVertexId))

      // TODO: pull request for gremlin-scala
//      val propertyColumn = value
//        .keys()
//        .asScala
//        .map { key =>
//          ColumnName(s"$columnNamePrefixProperty$key") -> ColumnType.apply(
//            value.value[Any](key)
//          )
//        }
//        .toMap
      val propertyColumn =
        GremlinScala(g.E(value)).valueMap
          .toList()
          .head
          .asScala
          .map { case (columnName, value) =>
            ColumnName(s"$columnNamePrefixProperty$columnName") -> ColumnType
              .apply(value)
          }
          .toMap

      Map(
        tableName -> (
          ColumnList(
            idColumn ++ inVColumn ++ outVColumn ++ propertyColumn
          ),
          TableAttributes(
            PrimaryKey(Set(ColumnName(columnNameEdgeId))),
            ForeignKey(
              Map(
                ColumnName(columnNameEdgeInVId) -> (
                  TableName(s"${config.tableName.vertex}_$inVertexLabel"),
                  ColumnName(config.columnName.vertexId)
                ),
                ColumnName(columnNameEdgeOutVId) -> (
                  TableName(s"${config.tableName.vertex}_$outVertexLabel"),
                  ColumnName(config.columnName.vertexId)
                )
              )
            ),
            UniqueIndex(Map.empty)
          )
        )
      )
    }

  override def toDml: RecordList = {
    // TODO: pull request for gremlin-scala
//    val propertyColumnList = value
//      .keys()
//      .asScala
//      .map { key =>
//        (s"$columnNamePrefixProperty$key", value.value[Any](key))
//      }
//      .toMap
    val propertyColumnList =
      GremlinScala(g.E(value)).valueMap
        .toList()
        .head
        .asScala
        .map { case (columnName, value) =>
          (s"$columnNamePrefixProperty$columnName", value)
        }
        .toMap

    val recordValue = Map(columnNameEdgeId -> value.id()) ++
      Map(columnNameEdgeInVId -> inVertexId) ++
      Map(columnNameEdgeOutVId -> outVertexId) ++
      propertyColumnList

    RecordList(
      Map(RecordKey(tableName, RecordId(id)) -> RecordValue(recordValue))
    )
  }
}
