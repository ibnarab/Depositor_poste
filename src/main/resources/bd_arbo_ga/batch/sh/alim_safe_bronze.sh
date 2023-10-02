#!/bin/bash

database=$(bdl_utlconfig_get "app.${LOC_NOM_DATASET}.database")
spark_class=$(bdl_utlconfig_get "app.${LOC_NOM_DATASET}.spark.class")

bdl_lanclogger_info "Alimentation '${database}.${LOC_NOM_DATASET}'"

bdl_utlspark_submit "spk.common" "spk.spark.${spark_class}"

_cr=${?}
if [ ${_cr} -eq 0 ]; then
  bdl_lanclogger_info "Successfully populated '${database}.${LOC_NOM_DATASET}'"
else
  bdl_lanclogger_error "Failed to populate '${database}.${LOC_NOM_DATASET}'"
  return 3;
fi

return 0
