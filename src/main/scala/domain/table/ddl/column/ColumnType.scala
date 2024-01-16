package domain.table.ddl.column

import scala.annotation.tailrec

/** referenced by https://docs.janusgraph.org/schema/#property-key-data-type
  */
sealed trait ColumnType {
  def toSqlSentence: String
}
case object ColumnTypeBoolean extends ColumnType {
  override def toSqlSentence: String = "BOOLEAN"
}

case class ColumnTypeByte(private val length: ColumnLength) extends ColumnType {
  override def toSqlSentence: String = s"TINYINT(${length.toSqlSentence})"
}

case class ColumnTypeInt(private val length: ColumnLength) extends ColumnType {
  override def toSqlSentence: String = s"INT(${length.toSqlSentence})"
}

case class ColumnTypeLong(private val length: ColumnLength) extends ColumnType {
  override def toSqlSentence: String = s"INT(${length.toSqlSentence})"
}

case class ColumnTypeDouble(private val length: ColumnLength)
    extends ColumnType {
  override def toSqlSentence: String = s"DOUBLE"
}

case class ColumnTypeCharacter(private val length: ColumnLength)
    extends ColumnType {
  override def toSqlSentence: String = s"CHAR(${length.toSqlSentence})"
}

case class ColumnTypeString(private val length: ColumnLength)
    extends ColumnType {
  override def toSqlSentence: String = s"VARCHAR(${length.toSqlSentence})"
}

case object ColumnTypeUnknown extends ColumnType {
  override def toSqlSentence: String = s"TEXT"
}

object ColumnType {

  def apply(value: Any): ColumnType = value match {
    case _: Boolean => ColumnTypeBoolean
    case valueByte: Byte =>
      ColumnTypeByte(ColumnLength(valueByte.toString.length))
    case valueInt: Int => ColumnTypeInt(ColumnLength(valueInt.toString.length))
    case valueLong: Long =>
      ColumnTypeLong(ColumnLength(valueLong.toString.length))
    case valueDouble: Double =>
      ColumnTypeDouble(
        ColumnLength(valueDouble.toString.replaceAll("0*$", "").length)
      )
    case _: Char =>
      ColumnTypeCharacter(ColumnLength(1))
    case valueString: String =>
      ColumnTypeString(ColumnLength(valueString.length))
    case _ => ColumnTypeUnknown // TODO: classify the type in detail
  }

  /** merges the attributes (type, length...) in two columns into one
    *
    * @param a
    *   target column
    * @param b
    *   target column
    * @return
    *   merged column attributes
    */
  @tailrec
  def merge(a: ColumnType, b: ColumnType): ColumnType = {
    (a, b) match {
      // ColumnTypeBoolean
      case (ColumnTypeBoolean, ColumnTypeBoolean) => ColumnTypeBoolean
      case (ColumnTypeBoolean, ColumnTypeByte(length)) =>
        ColumnTypeByte(length.max(5)) // 5 = false.toString
      case (ColumnTypeBoolean, ColumnTypeInt(length)) =>
        ColumnTypeInt(length.max(5)) // 5 = false.toString
      case (ColumnTypeBoolean, ColumnTypeLong(length)) =>
        ColumnTypeLong(length.max(5)) // 5 = false.toString
      case (ColumnTypeBoolean, ColumnTypeDouble(length)) =>
        ColumnTypeDouble(length.max(5)) // 5 = false.toString
      case (ColumnTypeBoolean, ColumnTypeCharacter(length)) =>
        ColumnTypeCharacter(length.max(5)) // 5 = false.toString
      case (ColumnTypeBoolean, ColumnTypeString(length)) =>
        ColumnTypeString(length.max(5)) // 5 = false.toString
      case (ColumnTypeBoolean, ColumnTypeUnknown) => ColumnTypeUnknown

      // ColumnTypeByte
      case (ColumnTypeByte(_), ColumnTypeBoolean) => merge(b, a)
      case (ColumnTypeByte(alength), ColumnTypeByte(blength)) =>
        ColumnTypeByte(alength.max(blength))
      case (ColumnTypeByte(alength), ColumnTypeInt(blength)) =>
        ColumnTypeInt(alength.max(blength))
      case (ColumnTypeByte(alength), ColumnTypeLong(blength)) =>
        ColumnTypeLong(alength.max(blength))
      case (ColumnTypeByte(alength), ColumnTypeDouble(blength)) =>
        ColumnTypeDouble(alength.max(blength))
      case (ColumnTypeByte(alength), ColumnTypeCharacter(blength)) =>
        ColumnTypeCharacter(alength.max(blength))
      case (ColumnTypeByte(alength), ColumnTypeString(blength)) =>
        ColumnTypeString(alength.max(blength))
      case (ColumnTypeByte(_), ColumnTypeUnknown) => ColumnTypeUnknown

      // ColumnTypeInt
      case (ColumnTypeInt(_), ColumnTypeBoolean) => merge(b, a)
      case (ColumnTypeInt(_), ColumnTypeByte(_)) => merge(b, a)
      case (ColumnTypeInt(alength), ColumnTypeInt(blength)) =>
        ColumnTypeInt(alength.max(blength))
      case (ColumnTypeInt(alength), ColumnTypeLong(blength)) =>
        ColumnTypeLong(alength.max(blength))
      case (ColumnTypeInt(alength), ColumnTypeDouble(blength)) =>
        ColumnTypeDouble(alength.max(blength))
      case (ColumnTypeInt(alength), ColumnTypeCharacter(blength)) =>
        ColumnTypeCharacter(alength.max(blength))
      case (ColumnTypeInt(alength), ColumnTypeString(blength)) =>
        ColumnTypeString(alength.max(blength))
      case (ColumnTypeInt(_), ColumnTypeUnknown) => ColumnTypeUnknown

      // ColumnTypeLong
      case (ColumnTypeLong(_), ColumnTypeBoolean) => merge(b, a)
      case (ColumnTypeLong(_), ColumnTypeByte(_)) => merge(b, a)
      case (ColumnTypeLong(_), ColumnTypeInt(_))  => merge(b, a)
      case (ColumnTypeLong(alength), ColumnTypeLong(blength)) =>
        ColumnTypeLong(alength.max(blength))
      case (ColumnTypeLong(alength), ColumnTypeDouble(blength)) =>
        ColumnTypeDouble(alength.max(blength))
      case (ColumnTypeLong(alength), ColumnTypeCharacter(blength)) =>
        ColumnTypeCharacter(alength.max(blength))
      case (ColumnTypeLong(alength), ColumnTypeString(blength)) =>
        ColumnTypeString(alength.max(blength))
      case (ColumnTypeLong(_), ColumnTypeUnknown) => ColumnTypeUnknown

      // ColumnTypeDouble
      case (ColumnTypeDouble(_), ColumnTypeBoolean) => merge(b, a)
      case (ColumnTypeDouble(_), ColumnTypeByte(_)) => merge(b, a)
      case (ColumnTypeDouble(_), ColumnTypeInt(_))  => merge(b, a)
      case (ColumnTypeDouble(_), ColumnTypeLong(_)) => merge(b, a)
      case (ColumnTypeDouble(alength), ColumnTypeDouble(blength)) =>
        ColumnTypeDouble(alength.max(blength))
      case (ColumnTypeDouble(alength), ColumnTypeString(blength)) =>
        ColumnTypeString(alength.max(blength))
      case (ColumnTypeDouble(alength), ColumnTypeCharacter(blength)) =>
        ColumnTypeCharacter(alength.max(blength))
      case (ColumnTypeDouble(_), ColumnTypeUnknown) => ColumnTypeUnknown

      // ColumnTypeCharacter
      case (ColumnTypeCharacter(_), ColumnTypeBoolean)   => merge(b, a)
      case (ColumnTypeCharacter(_), ColumnTypeByte(_))   => merge(b, a)
      case (ColumnTypeCharacter(_), ColumnTypeInt(_))    => merge(b, a)
      case (ColumnTypeCharacter(_), ColumnTypeLong(_))   => merge(b, a)
      case (ColumnTypeCharacter(_), ColumnTypeDouble(_)) => merge(b, a)
      case (ColumnTypeCharacter(alength), ColumnTypeCharacter(blength)) =>
        ColumnTypeCharacter(alength.max(blength))
      case (ColumnTypeCharacter(alength), ColumnTypeString(blength)) =>
        ColumnTypeString(alength.max(blength))
      case (ColumnTypeCharacter(_), ColumnTypeUnknown) => ColumnTypeUnknown

      // ColumnTypeString
      case (ColumnTypeString(_), ColumnTypeBoolean)      => merge(b, a)
      case (ColumnTypeString(_), ColumnTypeByte(_))      => merge(b, a)
      case (ColumnTypeString(_), ColumnTypeInt(_))       => merge(b, a)
      case (ColumnTypeString(_), ColumnTypeLong(_))      => merge(b, a)
      case (ColumnTypeString(_), ColumnTypeDouble(_))    => merge(b, a)
      case (ColumnTypeString(_), ColumnTypeCharacter(_)) => merge(b, a)
      case (ColumnTypeString(alength), ColumnTypeString(blength)) =>
        ColumnTypeString(alength.max(blength))
      case (ColumnTypeString(_), ColumnTypeUnknown) => ColumnTypeUnknown

      // ColumnTypeUnknown
      case (ColumnTypeUnknown, ColumnTypeBoolean)      => merge(b, a)
      case (ColumnTypeUnknown, ColumnTypeByte(_))      => merge(b, a)
      case (ColumnTypeUnknown, ColumnTypeInt(_))       => merge(b, a)
      case (ColumnTypeUnknown, ColumnTypeLong(_))      => merge(b, a)
      case (ColumnTypeUnknown, ColumnTypeDouble(_))    => merge(b, a)
      case (ColumnTypeUnknown, ColumnTypeCharacter(_)) => merge(b, a)
      case (ColumnTypeUnknown, ColumnTypeString(_))    => merge(b, a)
      case (ColumnTypeUnknown, ColumnTypeUnknown)      => ColumnTypeUnknown
    }
  }
}
