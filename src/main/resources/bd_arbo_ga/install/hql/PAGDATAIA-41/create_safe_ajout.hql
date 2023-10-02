USE ${database};
DROP TABLE IF EXISTS ${table};
CREATE EXTERNAL TABLE ${table}
(
    criteria_offercode 	             STRING  ,
    criteria_productId 	             STRING  ,
    depositId		                 STRING  ,
    irregularities_offercode         STRING  ,
    irregularities_productcode       STRING  ,
    irregularities_refcode           STRING  ,
    downgradedReason	             STRING
)

STORED AS PARQUET
LOCATION '${hdfs_path}'
;
