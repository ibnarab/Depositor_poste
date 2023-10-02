INSERT INTO TABLE ${database_safe}.${table_safe_ajout}
SELECT
    t.criteria_offercode                                                    ,
    t.criteria_productId                                                    ,
    t.depositId                                                             ,
    SPLIT(t.irregularities,'_')[0]    AS irregularities_offercode           ,
    SPLIT(t.irregularities,'_')[1]    AS irregularities_productcode         ,
    t.irregularities                  AS irregularities_refcode             ,
    t.downgradedReason

FROM

(SELECT
    criteria_offercode                                                       ,
    criteria_productId                                                       ,
    depositId                                                                ,
    exploded_nom as irregularities                                           ,
    downgradedReason

FROM ${database_raw}.${table_raw_ajout}
LATERAL VIEW EXPLODE(SPLIT(irregularities, ',')) exploded_table as exploded_nom

) AS t
;
