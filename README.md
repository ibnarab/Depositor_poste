# bdl-bscc-depositor
Consomme le topic Kafka vt1_cDepositOrRaw_in_v1.


## *Résumé des Steps*
| **Step**                | **Description**                                                                                                       | **Notes** |
|-------------------------|-----------------------------------------------------------------------------------------------------------------------|-----------|
| Alimentation Raw        | Intégration via suezlib                                                                                               ||
| Alimentation Safe       | Extraction des données des structures                                                                                 ||
| Alimentation Optimized  | Copie du Safe sans traitement supplémentaire                                                                          ||
| Compact Safe            | Compacte les fichiers générés afin d'en réduire le nombre et de se rapprocher de la taille cible de l'infrastructure. ||
| Compact Optimized       | Compacte les fichiers générés afin d'en réduire le nombre et de se rapprocher de la taille cible de l'infrastructure. ||
| INSTALL RAW PAGDATAIA41 | Creation  et alimentation de la table temporaire , phase du raw                                                       ||
| INSTALL SAFE PAGDATAIA41| Creation  et alimentation de la table temporaire , phase du safe à partir du raw                                      ||
| ALIM SAFE PAGDATAIA41   | Recriture de la table principale  avec le safe principale et le safe temporaire                                       ||

## *Principaux Paramètres*
| **Paramètre**                        | **Description**             |**Notes**|
|--------------------------------------|-----------------------------|---:|
| app.table.raw.annonce.*              | Config liée au Raw          ||
| app.table.safe.bronze.annonce.*      | Config liée au Safe         ||
| app.table.optimized.silver.annonce.* | Config liée à l'Optimized   ||
| hcp.*                                | Config liée à la copie HDFS ||
| cpt.*                                | Config liée au compactage   ||

## *Input Data*
* Topic kafka : vt1_cDepositOrRaw_in_v1

* bd_aa_ttd_raw.r_depositor_vt1_cdepositorraw_in_v1  (/data/bd_/aa/ttd/raw/depositor/processed)

* bd_aa_ttd_safe.b_depositor_admission               (/data/bd_/aa/ttd/safe/depositor/b_depositor_admission)
**table temporaire ***
* bd_aa_ttd_raw.r_depositor_ajout_raw  (/data/bd_/aa/ttd/raw/depositor/to_process/r_depositor_ajout_raw)
* bd_aa_ttd_safe.t_depositor_ajout_safe              (/data/bd_/aa/ttd/safe/depositor/t_depositor_ajout_safe)
## *Output Data*
* bd_aa_traitement_optimized.s_trt_depot_admission   (/data/bd_/business_area_aa/traitement/optimized/s_trt_depot_admission)


## *Historique des Evolutions Fonctionnelles et Techniques*
* Le 2022-07-05 MEP initiale : version 02_00_00
* Le 2023-07-26 MEP nouvelle : version 02_02_00