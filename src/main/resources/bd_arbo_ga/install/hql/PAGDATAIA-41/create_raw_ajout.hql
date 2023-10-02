USE ${database};
DROP TABLE IF EXISTS ${table};
CREATE EXTERNAL TABLE ${table}
(
    criteria_offercode 	        STRING  ,
    criteria_productId 	        STRING  ,
    depositId		            STRING  ,
    irregularities		        STRING  ,
    downgradedReason	        STRING
)
ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe'
WITH SERDEPROPERTIES ('field.delim'=';','line.delim'='\n','serialization.format'='\|')
STORED AS INPUTFORMAT 'org.apache.hadoop.mapred.TextInputFormat'
OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
LOCATION '${hdfs_path}'
TBLPROPERTIES ("skip.header.line.count"="1", 'serialization.null.format'='');
