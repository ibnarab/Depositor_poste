package fr.laposte.bdl.bscc.depositor.common

import fr.laposte.bdl.bscc.utlv4scala.UtlConfig
import org.slf4j.Logger


/** Classe de gestion de la configuration applicative.
  *
  */
object AppConfig {

  // charger la conf
  def load() : Unit = {
    UtlConfig.load()
  }

  // tout logguer en niveau "info"
  def logInfo(logger: Logger) : Unit =  {
    UtlConfig.logInfo(logger, "app")
  }


  val Undefined: String = "<undefined>"
  val TableNameSeparator: String = "."
  val DateImportColumnName: String = "date_import"


  // Recover info
  def getFirstDateToProcess: String                     = UtlConfig.get("app.depositor.first_date_to_process","")
  def getLastDateToprocess: String                      = UtlConfig.get("app.depositor.last_date_to_process","")

  // Raw info
  def getDatabaseRawName: String                        = UtlConfig.get("app.database.raw", Undefined)
  def getTableRawName: String                           = UtlConfig.get("app.table.raw.admission", Undefined)
  def getTargetTableRawName: String                     = getDatabaseRawName + TableNameSeparator + getTableRawName

  // Shared
  def getDatabaseSharedName: String                     = UtlConfig.get("app.database.safe", Undefined)
  def getTableSharedName: String                        = UtlConfig.get("app.table.safe.bronze.admission", Undefined)
  def getTargetSharedName: String                       = getDatabaseSharedName + TableNameSeparator + getTableSharedName

}
