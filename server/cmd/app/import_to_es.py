#!/usr/bin/env python
import json
import logging
import sys
from elasticsearch import Elasticsearch
from elasticsearch import helpers
from datetime import datetime

logger = logging.getLogger()


class ElasticEngine:
    def __init__(self, index, host='localhost', timeout=10):
        self.index = index
        self.es = Elasticsearch(host, timeout=timeout)

    def reset(self):
        self.es.indices.delete(index='chat_record', ignore=[400, 404])

    def save(self, dict_obj):
        actions = [self.__index_action__(dict_obj)]
        try:
            helpers.bulk(self.es, actions, stats_only=True, params={"consistency": "all", "chunk_size": self.batch_size})
        except Exception as e:
            pass

    def save_doc(self, obj, doc_type='wechat_record'):
        obj["timestamp"] = datetime.now()
        x = self.es.index(index=self.index, doc_type=doc_type, body=obj)
        print(x)

        return

    def read(self, condition):
        return helpers.scan(self.es, condition, self.batch_size, True)

    def __index_action__(self, dic):
        """
        Encapsulate the index data
        :param index:
        :param type: Index Type
        :param dic: index data
        :return:
        """
        action = {
            "_op_type": "index",
            "_index": self.index,
            "_source": dic
        }

        return action

if __name__ == '__main__':
    esclient = ElasticEngine('chat_record')
    with open('data/1_2018_05_07_01_42_45.jpg.json') as f:
        for l in f:
            print(l)
            json_obj = json.loads(l, encoding='utf-8')
            print(json_obj)
            esclient.save_doc(json_obj)
