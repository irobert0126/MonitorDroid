#!/usr/bin/env python
from __future__ import print_function
from aip import AipOcr
import os
import sys

APP_ID = os.environ['BAIDU_APP_ID']
API_KEY = os.environ['BAIDU_API_KEY']
SECRET_KEY = os.environ['BAIDU_SECRET_KEY']

print(APP_ID, API_KEY, SECRET_KEY)


def image_to_text(filename):
    """
    convert image to text
    :param filename: filename of pics.
    :return: text
    """

    client = AipOcr(APP_ID, API_KEY, SECRET_KEY)
    def get_file_content(filePath):
        with open(filePath, 'rb') as fp:
            return fp.read()

    image = get_file_content(filename)
    #res=client.basicGeneralUrl(url);
    res = client.general(image)

    text = ''

    for item in res["words_result"]:
        text += "%s\n" % item["words"]

    return text


if __name__ == '__main__':
    print(image_to_text(sys.argv[1]))
