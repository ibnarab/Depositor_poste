package fr.laposte.bdl.bscc.depositor.spark

import fr.laposte.bdl.bscc.depositor.common.AppConfig
import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}

/**
 * Centralise les traitements pour alimenter le Shared
 */
object AlimSafeDepotAdmission extends SparkJob {

  val structColumnsNameOrAlias = List("depositor", "event")

  val arraysColumnsOfStruct = List("files","irregularities")


  // Data processing
  override def doit(spark: SparkSession): Unit = {

    // Class logs
    val logger : Logger = LoggerFactory.getLogger(getClass.getCanonicalName)

    // Loading the conf
    AppConfig.load()

    // Print the conf
    AppConfig.logInfo(logger)

    val firstDateToProcess : String = AppConfig.getFirstDateToProcess
    val lastDateToProcess  : String = AppConfig.getLastDateToprocess
    val targetRaw          : String = AppConfig.getTargetTableRawName
    val targetShared       : String = AppConfig.getTargetSharedName

    val dfRaw     = spark.table(targetRaw)
    val dfShared  = spark.table(targetShared)

    // Filtrage des données selon la configuration
    val dfRecover = CommonMethods.filterByPeriod(logger, dfRaw, dfShared, firstDateToProcess, lastDateToProcess)

    // Extraction des champs des structures
    val dfWithExtractedStruct = CommonMethods.extractFromStruct(dfRecover, structColumnsNameOrAlias, dropStruct = true)

    // Aplatissement des arrays
    val dfWithExplodedArrays = CommonMethods.explodeArraysOfStruct(dfWithExtractedStruct, arraysColumnsOfStruct, dropArray = true)

    // Partition
    val dfFinal = CommonMethods.createPartitionColumn(dfWithExplodedArrays, "event_eventdatetime", "eventdatetime_part")
                               .withColumnRenamed("timestamp", "timestamp_str")

    CommonMethods.appendData(spark, dfFinal, targetShared)
  }

}
