import pymysql
import pandas as pd

host = "0.0.0.0"
port = 3306
username = "root"
db_name = "capstone_pjt"
password = ""


conn = pymysql.connect(
                host=host,
                user=username,
                charset="utf8",
                db=db_name,
                port=port)

print(conn.get_server_info())

cursor = conn.cursor()
sql = "select * from dc_data"
cursor.execute(sql)
data = cursor.fetchall()

print(data[0])

cursor.close()
conn.close()

df = pd.DataFrame(data=data, columns=["id", "title", "view_count", "recommend_count"])
print(df.to_csv("./data/dc_data.csv", index=False))