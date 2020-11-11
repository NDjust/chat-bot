from bs4 import BeautifulSoup
from db_processor import db_config, insert_in_db
from multiprocessing import Pool

import os
import requests

BASE_URL = "https://gall.dcinside.com/board/lists"


def apply_multiprocessing(func, data: list, **kwargs) -> list:
    """ 파라미터로 입력받은 함수와 데이터를 병렬처리하는 함수

    :param func: 병렬처리할 함수
    :param data: 병렬처리할 데이터
    :return: 병렬처리 결과 값
    """
    pool = Pool(processes=os.cpu_count())
    result = pool.map(func, data)
    pool.close()

    return result


def save_data(page_num: int):
    print(f"page num : {page_num}")
    conn, cursor, insert_sql = db_config()
    # 파라미터 설정
    params = {
        'id': "mabi_heroes",
        'page': page_num
    }

    # 헤더 설정
    headers = {

        'User-Agent':
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.183 Safari/537.36"
    }

    req = requests.get(BASE_URL, params=params, headers=headers)
    soup = BeautifulSoup(req.text, "html.parser")
    contents = soup.find("tbody").find_all("tr")
    for content in contents:
        title = content.find("a").text
        gall_count = content.find("td", class_="gall_count").text
        recommend_count = content.find("td", class_="gall_recommend").text

        if gall_count == "-" or None:
            gall_count = 0

        if recommend_count == "-" or None:
            recommend_count = 0

        data = [title, gall_count, recommend_count]
        insert_in_db(data, conn, cursor, insert_sql)


if __name__ == '__main__':
    apply_multiprocessing(save_data, [i for i in range(1, 10_000)])
