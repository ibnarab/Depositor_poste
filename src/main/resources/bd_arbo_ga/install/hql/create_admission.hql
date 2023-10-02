USE ${database};

DROP TABLE IF EXISTS ${table};

CREATE EXTERNAL TABLE ${table}
(
    timestamp_str                           STRING,
    realtime                                STRING,
    valuationdate                           STRING,
    respecteddepositdeadlineflag            STRING,
    producer                                STRING,
    downgradeditemquantity                  STRING,
    rectifiedreason                         STRING,

    depositor_depositid                     STRING,
    depositor_custaccnumber                 STRING,
    depositor_ptdate                        STRING,
    depositor_mpsid                         STRING,
    depositor_depot                         STRING,
    depositor_deposit_actionrealdate        STRING,

    event_eventcode                         STRING,
    event_eventdatetime                     STRING,
    event_reasoncode                        STRING,
    event_commitmentreleaseflag             STRING,
    event_servicereleaseflag                STRING,

    files_contractnumber                    STRING,
    files_custaccnumber                     STRING,
    files_depositsheetid                    STRING,
    files_depositproofid                    STRING,

    irregularities_offercode                STRING,
    irregularities_productcode              STRING,
    irregularities_refcode                  STRING,

    date_import                             STRING
)
PARTITIONED BY (eventdatetime_part STRING)
ROW FORMAT SERDE
  'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat'
LOCATION
  '${hdfs_path}'
;
