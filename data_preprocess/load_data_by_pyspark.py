import pymysql
import os
import json
from pyspark import SparkContext, SparkConf
from pyspark.sql import SQLContext

SUBMIT_ARGS = "--packages mysql:mysql-connector-java:5.1.39 pyspark-shell"
os.environ["PYSPARK_SUBMIT_ARGS"] = SUBMIT_ARGS
conf = SparkConf()
sc = SparkContext(conf=conf)

print(type(sc))
print(dir(sc))
print(help(sc))
print(sc.version)

data = range(1, 10001)
range_RDD = sc.parallelize(data, 8)

print(f"type of range RDD = {type(range_RDD)}")
print(help(sc.parallelize))

print(range_RDD.getNumPartitions())
print(range_RDD.toDebugString())
print(f"range RDD : {range_RDD.id()}")
print(range_RDD.setName("My first RDD"))
print(range_RDD.toDebugString())
help(range_RDD)


hostname = "localhost"
db_name = "capstone_pjt"
jdbc_port = 3306
username = "root"
password = ""
jdbc_url = f"jdbc:mysql://{hostname}:{jdbc_port}/{db_name}?user={username}&password={password}"

sql_context = SQLContext(sc)

query = "(select * from dc_data) dc_data"
df1 = sql_context.read.format("jdbc").options(driver="com.mysql.jdbc.Driver", url=jdbc_url, dbtable=query).load()
df1.show()

to_json = df1.toJSON()

for d in to_json:
    pr



