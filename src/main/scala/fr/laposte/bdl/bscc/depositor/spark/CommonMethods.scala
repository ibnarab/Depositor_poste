package fr.laposte.bdl.bscc.depositor.spark

import fr.laposte.bdl.bscc.depositor.common.AppConfig
import org.apache.spark.sql.functions.{col, explode_outer, max, substring_index}
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.slf4j.Logger

/** Classe qui centralise les méthodes communes
  *
  */
object CommonMethods {

  /** Ajoute les données de la dataframe source dans la table cible
   * @param spark            SparkSession
   * @param dfSource         Dataframe source
   * @param targetTable      Au format "Database.table"
   */
  def appendData(spark: SparkSession, dfSource: DataFrame, targetTable: String): Unit = {
    val targetCols = spark.table(targetTable).columns
    dfSource.select(targetCols.head, targetCols.tail: _*)
      .write.mode(SaveMode.Append)
      .insertInto(targetTable)
  }


  /**
   * Aplatit les structures des colonnes cibles et les supprime si demandé
   * @param dfSource dataframe source
   * @param columnsStructNameOrAlias liste des colonnes de type structure à aplatir, contient le nom seul ou le nom source et l'alias à utiliser
   * @param dropStruct indique si les colonnes sources sont à supprimer
   * @return nouvelle dataframe
   */
  def extractFromStruct(dfSource: DataFrame, columnsStructNameOrAlias: List[Any], dropStruct: Boolean = false): DataFrame = {
    var dfTarget = dfSource
    val existingColumns = dfTarget.columns

    columnsStructNameOrAlias.foreach { structColumnNameOrAlias =>
      val (sourceColumnName, targetAliasName) = getColumnNameAndAlias(structColumnNameOrAlias)

      // Pour chaque structure demandée, on transforme les champs internes en colonne
      val topLevelStructName = sourceColumnName.split("\\.")(0)
      // On ne fait le traitement que si la colonne de premier niveau existe
      if (existingColumns.contains(topLevelStructName)) {
        val targetAliasNameNormalized = targetAliasName.replace(".", "_")
        val innerColumns = dfTarget.select(col(sourceColumnName + ".*")).columns
        innerColumns.foreach { innerColumnName =>
          // Pour chaque colonne interne, on génère la colonne nomDeLaColonneParent_nomDeLaColonneEnfant si elle n'est pas elle-même une structure à aplatir
          val absoluteSourceColumnName = sourceColumnName + "." + innerColumnName
          if (!listNameOrAliasContains(columnsStructNameOrAlias, absoluteSourceColumnName)) {
            val targetColumnName = targetAliasNameNormalized + "_" + innerColumnName
            dfTarget = dfTarget.withColumn(targetColumnName, col(absoluteSourceColumnName))
          }
        }
      }
    }

    if (dropStruct) {
      // Suppression des structures initiales si demandé (uniquement après toutes les extractions pour gérer les
      // éventuelles structures internes à d'autres structures parentes)
      val sourceStructColumns = columnsStructNameOrAlias.collect {
        case (sourceName: String, _: String) => sourceName
        case sourceName:String => sourceName
      }
      dfTarget = dropColumns(dfTarget, sourceStructColumns)
    }

    dfTarget
  }

  /**
   * Aplatit les tableaux contenant des structures d'après les colonnes cibles et les supprime si demandé
   * @param dfSource dataframe source
   * @param columnsToExplode liste des colonnes de type Array à aplatir
   * @param dropArray indique si les colonnes sources sont à supprimer
   * @return nouvelle dataframe
   */
  def explodeArraysOfStruct(dfSource: DataFrame, columnsToExplode: List[String], dropArray: Boolean = false): DataFrame = {
    var dfTarget = dfSource
    columnsToExplode.foreach { arrayColumnName =>
      dfTarget = dfTarget.withColumn(arrayColumnName, explode_outer(col(arrayColumnName)))
      val dfTargetColumns = dfTarget.select(col(arrayColumnName + ".*")).columns
      dfTargetColumns.foreach { innerColumnName =>
        val absoluteSourceColumnName = arrayColumnName + "." + innerColumnName
        val targetColumnName = arrayColumnName + "_" + innerColumnName
        dfTarget = dfTarget.withColumn(targetColumnName, col(absoluteSourceColumnName))
      }
    }

    if (dropArray) {
      // Suppression des colonnes si demandé (uniquement après le traitement d'extraction de tableau pour gérer les
      // éventuelles structures internes)
      dfTarget = dropColumns(dfTarget, columnsToExplode)
    }

    dfTarget
  }

  /**
   * Supprime les colonnes demandées de la dataframe source
   * @param dfSource dataframe source
   * @param columnsToDrop liste des colonnes à supprimer
   * @return nouvelle dataframe
   */
  def dropColumns(dfSource: DataFrame, columnsToDrop: List[String]): DataFrame = {
    var dfTarget = dfSource
    columnsToDrop.foreach { columnName =>
      dfTarget = dfTarget.drop(col(columnName))
    }
    dfTarget
  }

  /**
   * Retourne le nom de la colonne et son alias d'après la chaine ou le tuple en paramètre.
   * Si l'alias n'est pas spécifié, il sera égal au nom de la colonne
   * @param columnNameOrAlias chaine indiquant le nom de la colonne ou tuple(nomColonne, alias)
   */
  def getColumnNameAndAlias(columnNameOrAlias: Any): (String, String) = {
    columnNameOrAlias match {
      case (sourceName: String, aliasName: String) => (sourceName, aliasName)
      case sourceName:String => (sourceName, sourceName)
    }
  }

  /**
   * Retourne vrai si la liste en paramètre contient la colonne en paramètre.
   * @param columnsWithOptionalAlias liste des configurations de colonnes (avec ou sans alias)
   * @param value nom de la colonne à tester
   */
  def listNameOrAliasContains(columnsWithOptionalAlias: List[Any], value: String): Boolean = {
    val results = columnsWithOptionalAlias.filter { columnNameOrAlias =>
      val (sourceColumnName, _) = getColumnNameAndAlias(columnNameOrAlias)
      sourceColumnName == value
    }
    results.nonEmpty
  }

  /**
   * Crée une colonne utilisée pour le partitionnement depuis un champ de type String représentant une date au format YYYY-MM-DD
   * @param dfSource dataframe source
   * @param columnSourceName nom de la colonne à utiliser
   * @param columnTargetName nom de la colonne en sortie
   * @return nouvelle dataframe
   */
  def createPartitionColumn(dfSource: DataFrame, columnSourceName: String, columnTargetName: String): DataFrame = {
    dfSource.withColumn(columnTargetName, substring_index(col(columnSourceName), "-", 2))
  }

  /**
   * Filtre les lignes de la dataframe source selon la date d'import et la période de date souhaitée. <br>
   * Si la dataframe cible est vide, aucun filtre n'est appliqué sur la source.<br>
   * Si aucune période n'est explicitement demandée, filtre la source pour ne conserver que les nouvelles lignes
   * depuis la date la plus récente de la dataframe cible.
   *
   * @return dataframe source filtrée
   */
  def filterByPeriod(logger: Logger, dfSource: DataFrame, dfTarget: DataFrame, firstDateToProcess: String = "", lastDateToProcess: String = ""): DataFrame = {
    if (dfTarget.limit(1).count() == 0) {
      logger.info("INFO_LOG, First alimentation")
      dfSource
    } else if ((firstDateToProcess != "") && (lastDateToProcess != "")) {
      logger.info("INFO_LOG, Manual alimentation")
      logger.info("INFO_LOG, first_date_to_process = " + firstDateToProcess)
      logger.info("INFO_LOG, last_date_to_process = " + lastDateToProcess)
      dfSource.filter(col(AppConfig.DateImportColumnName) >= firstDateToProcess && col(AppConfig.DateImportColumnName) <= lastDateToProcess)
    } else {
      logger.info("INFO_LOG, Automatic alimentation")
      val maxDate = dfTarget.select(max(AppConfig.DateImportColumnName)).collect().map(_.getString(0)).mkString(" ")
      dfSource.filter(col(AppConfig.DateImportColumnName) > maxDate)
    }
  }

}
