#!/bin/bash
# Le message pour la recuperation des information des lanceurs

fichier=$(bdl_utlconfig_get "app.${LOC_NOM_DATASET}.file")
to_process=$(bdl_utlconfig_get "app.${LOC_NOM_DATASET}.hdfs.path")

## TRANSFERT UNIX -> HDFS

hdfs dfs -put ${ALIM}/${fichier} ${to_process}/.

_cr=${?}

if [  ${_cr} -ne 0 ]; then return 3; fi

return 0
