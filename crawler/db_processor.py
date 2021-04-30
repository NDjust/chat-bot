import pymysql


def insert_in_db(data: list, conn, cursor, sql):
    """ 데이터를 디비에 삽입하는 함수.

    :param data: 삽입할 데이터
    :param conn: 디비 conn
    :param cursor: 디비 cursor
    :param sql: sql 쿼리
    :return: None
    """
    print("==========SAVE data==============\n")
    print(data)
    cursor.execute(sql, tuple([d for d in data]))
    conn.commit()


def __connect_db(host, user, password, db, port):
    """ 데이터베이스 연결하는 함수

    """
    conn = pymysql.connect(host=host,
                           user=user,
                           password=password,
                           charset="utf8",
                           db=db,
                           port=port)
    print(conn.get_server_info())
    cursor = conn.cursor()

    return conn, cursor


def db_config():
    """ config.json 파일에 저장된 DB 설정값을 불러와 처리.

    :return: 설정된 데이터 베이스 연결된 pysql connector와 Cursor 그리고 insert sql 쿼리문.
    """
    import json
    with open("db_config.json") as js:
        json_data = json.load(js)
        host = json_data["host"]
        user = json_data["user"]
        password = json_data["password"]
        db = json_data["db"]
        port = json_data["port"]
        cols = json_data["columns"][1:-1].split(",")
        table_name = json_data["table_name"]
        conn, cursor = __connect_db(host=host, user=user, password=password, db=db, port=port)
        # cols = ['company_name', 'product_name', 'report_num', 'register_date',
        #         'expiry_date', 'properties', 'daily_dose', 'package_type',
        #         'storage_caution', 'warning_info', 'function_content',
        #         'standard_info', 'materials_info']
        values = ("(" + ("%s," * len(cols))[:-1] + ")")
        columns = "(" + ",".join(cols) + ")"
        print(columns)
        sql = f"insert into {table_name}{columns} values {values}"

        return conn, cursor, sql
