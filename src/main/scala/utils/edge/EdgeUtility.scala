package utils.edge

import com.typesafe.config.ConfigFactory
import domain.table.{TableList, TableName}
import domain.table.column.{
  ColumnList,
  ColumnName,
  ColumnType,
  ColumnTypeBoolean,
  ColumnTypeDouble,
  ColumnTypeInt,
  ColumnTypeLong,
  ColumnTypeString,
  ColumnTypeUnknown
}
import gremlin.scala.Edge

import scala.jdk.CollectionConverters.SetHasAsScala

object EdgeUtility {

  private val config = ConfigFactory.load()

  // TODO: Set a more detailed table name
  private val tableName = TableName(config.getString("table_name_edge"))

  /** convert to Database Table Information
    *
    * @param edge
    *   [[Edge]]
    * @return
    *   Database Table Information
    */
  def toTableList(edge: Edge): TableList =
    TableList {
      val inVColumn =
        Map(
          ColumnName(config.getString("column_name_edge_in_v_id")) -> ColumnType
            .apply(edge.inVertex().id())
        )
      val outVColumn =
        Map(
          ColumnName(
            config.getString("column_name_edge_out_v_id")
          ) -> ColumnType.apply(edge.outVertex().id())
        )

      // TODO: pull request for gremlin-scala
      val column = edge
        .keys()
        .asScala
        .map { key =>
          ColumnName(key) -> ColumnType.apply(edge.value[Any](key))
        }
        .toMap

      Map(tableName -> ColumnList(inVColumn ++ outVColumn ++ column))
    }

  def toSqlSentence(edge: Edge): String = {
    // TODO: pull request for gremlin-scala
    val (columnList, valueList) =
      edge.keys().asScala.map { key => (key, edge.value[Any](key)) }.unzip
    val valueListForSql = valueList.map { value =>
      ColumnType.apply(value) match {
        case ColumnTypeBoolean   => value
        case ColumnTypeInt(_)    => value
        case ColumnTypeLong(_)   => value
        case ColumnTypeDouble(_) => value
        case ColumnTypeString(_) => s"\"$value\""
        case ColumnTypeUnknown   => s"\"$value\""
      }
    }
    s"INSERT INTO ${tableName.toSqlSentence} (${config.getString("column_name_edge_in_v_id")}, ${config
        .getString("column_name_edge_out_v_id")}, ${columnList
        .mkString(",")}) VALUES (${edge.inVertex().id()}, ${edge.outVertex().id()}, ${valueListForSql
        .mkString(", ")});"
  }
}
