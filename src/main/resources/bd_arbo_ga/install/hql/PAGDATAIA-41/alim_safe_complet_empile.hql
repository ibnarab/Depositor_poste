INSERT OVERWRITE TABLE ${database_safe}.${table_safe_admission}
SELECT

    TRIM(a.timestamp_str),
    TRIM(a.realtime),
    TRIM(a.valuationdate),
    TRIM(a.respecteddepositdeadlineflag),
    TRIM(a.producer),
    TRIM(a.downgradeditemquantity),
    TRIM(b.downgradedreason) AS rectifiedreason,
    TRIM(a.depositor_depositid),
    TRIM(a.depositor_custaccnumber),
    TRIM(a.depositor_ptdate),
    TRIM(a.depositor_mpsid),
    TRIM(a.depositor_depot),
    TRIM(a.depositor_deposit_actionrealdate),
    TRIM(a.event_eventcode),
    TRIM(a.event_eventdatetime),
    TRIM(a.event_reasoncode),
    TRIM(a.event_commitmentreleaseflag),
    TRIM(a.event_servicereleaseflag),
    TRIM(a.files_contractnumber),
    TRIM(a.files_custaccnumber),
    TRIM(a.files_depositsheetid),
    TRIM(a.files_depositproofid),
    CASE WHEN TRIM(b.downgradedreason) IS NULL THEN NULL ELSE TRIM(b.irregularities_offercode) END AS irregularities_offercode,
    CASE WHEN TRIM(b.downgradedreason) IS NULL THEN NULL ELSE TRIM(b.irregularities_productcode) END AS irregularities_productcode,
    CASE WHEN TRIM(b.downgradedreason) IS NULL THEN NULL ELSE CONCAT_WS('_', SPLIT(b.irregularities_refcode,'_')[2], SPLIT(b.irregularities_refcode,'_')[3], SPLIT(b.irregularities_refcode,'_')[4], SPLIT(b.irregularities_refcode,'_')[5]) END AS irregularities_refcode,
    TRIM(a.date_import),
    TRIM(a.eventdatetime_part)

FROM ${database_safe}.${table_safe_admission} a
LEFT JOIN ${database_safe}.${table_safe_ajout} b
ON TRIM(a.depositor_depositid) = TRIM(b.depositid)
AND TRIM(a.event_eventcode) = 'ORX'
;
