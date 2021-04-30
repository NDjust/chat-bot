# Clean Chat (Capstone SCH)
[![code](https://img.shields.io/badge/Code-Python3.6-blue)](https://docs.python.org/3/license.html)
[![data](https://img.shields.io/badge/Data-dc-blueviolet)](https://news.chosun.com/ranking/list.html)
[![member](https://img.shields.io/badge/Project-Member-brightgreen)](https://gall.dcinside.com/)
[![DBMS](https://img.shields.io/badge/DBMS-MySQL-orange)](https://www.mysql.com/downloads/)
> 채팅에 발생하는 비속어를 탐지하고 필터링 해주는 프로젝트

## 📖 Introduction  

게임, 스트리밍, 라이브 방송등 채팅으로 소통을 많이 하는 플랫폼이 많아짐에 따라, 채팅에서 발생하는 욕설과 비속어 문제가 제기됨  다양한 커뮤니티 용어 등 다양한 형태의 비속어들이 끊임없이 생기고 있고, 기존 금칙어 기반 비속어 탐지는 ㅅ1 발와 같은 기존 금칙어 기반으로 필터링 되는 로직을 피해가는  다양한 형태의 욕설이 채팅에서 많이 사용되어 금칙어 기반으로 일일이 탐색 후 적용해야하므로 비용이 소모가 크고 한계가 존재

## 📂 Directory structure
```
  |-chat-app                         # chat application (spring-boot & React)   
  |  |...
  |-api           
  |  |-predict_api.py
  |  |-load_model.py
  |
  |-crwaler
  |  |-db_processor.py                  
  |  |-dc_cralwer.py
  |  |-db_config.json
  |  |-db_query.sql                    
  |
  |-data_preprocessing 
  |  |-data                            # data folder
  |  |  |...
  |  |-labeling.ipynb                  # data preprocessing & labeling
  |  |-load_abuse_data.py                               
  |  |-load_dc_data_in_db.py               
  |
  |-modeling
  |  |-data                            # data folder
  |  |  |...                           
  |  |-data_helper.py                  # load data & labeling
  |  |-eval.py                         # eval model
  |  |-text_cnn.py                     # text-cnn model
  |  |-train.py                        # train model
  |
  |-TextPreprocessing 
  |  |-TextSummarizer.py                # 문장요약해주는 코드
  |  |-preprocessing.py                 # 토큰화, 품사 태깅 해주는 코드
  |  |-main.py                          # 전처리 실행 해주는 코드
  |  |-stopword.txt                     # 불용어 목록
  |  |-한국어불용어100.txt                 # 한국어 불용어 목록
  |
  |-Vectorization
  |  |-Vectorizer.py                    # 벡터화 모델 세팅하는 코드
  |  |-train.py                         # 모델 학습시키는 코드
  |
  |-.gitignore                               
  |
  |-README.md                           # 해당 문서
  |
  |-requirements.txt                    # 사전 설치 목록

```

## 🌐 Dependency Build Instructions
```
- beautifulsoup4==4.6.0
- konlpy==0.5.2
- numpy==1.18.1
- pandas==0.25.3
- pymysql==0.9.3
- requests==2.22.0
- scikit-learn==0.22.1
- selenium==3.141.0
- tensorflow==1.14.0
- mecab==1.0.3


```
## 📋 Progress
![process](https://user-images.githubusercontent.com/47904523/102562472-7d2d2d80-411a-11eb-9caa-e833f16ea913.png)

## 💻 Getting Started (Installation)
```
pip3 install -r requirements.txt
```
### Modeling Reference
- [Text-CNN](https://github.com/dennybritz/cnn-text-classification-tf)

## 🔍 Architecture
![Architecture](https://user-images.githubusercontent.com/47904523/102562712-05133780-411b-11eb-9a4a-0db45a60d98b.png)
