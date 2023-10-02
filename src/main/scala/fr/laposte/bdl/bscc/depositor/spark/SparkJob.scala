package fr.laposte.bdl.bscc.depositor.spark

import fr.laposte.bdl.bscc.utlv4scala.UtlLogging
import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}

import scala.util.Properties


trait SparkJob {

  def doit(spark: SparkSession): Unit

  def main(args: Array[String]): Unit = {
    // Debut du contexte Spark
    val logger : Logger = LoggerFactory.getLogger(getClass.getCanonicalName)

    UtlLogging.info(logger, Array(s"DEBUT DU TRAITEMENT - Scala=${Properties.versionNumberString}"))
    UtlLogging.info(logger, Array(s"DEMARRAGE DU CONTEXTE SPARK"))

    val spark = SparkSession.builder().enableHiveSupport().getOrCreate()

    UtlLogging.info(logger, Array(s"CONTEXTE SPARK DEMARRE - Spark=${spark.version}"))

    // Traitement des données
    doit(spark: SparkSession)

    // Arret du contexte Spark
    UtlLogging.info(logger, Array(s"ARRET DU CONTEXTE SPARK - Spark=${spark.version}"))

    spark.stop()

    UtlLogging.info(logger, Array(s"CONTEXTE SPARK ARRETE - Spark=${spark.version}"))
    UtlLogging.info(logger, Array(s"FIN DU TRAITEMENT - Scala=${Properties.versionNumberString}"))

    // surtout pas d'appel a "System.exit()" car cela shunterait le processus d'arret de Spark
  }

}
