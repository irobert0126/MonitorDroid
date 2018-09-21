#!/usr/bin/env python
from __future__ import print_function

import base64
import hashlib
from Crypto.Cipher import AES


class AESCipher:

    def __init__(self, key):
        self.bs = 16
        self.key = hashlib.sha256(key.encode()).digest()

    def encrypt(self, message):
        message = self._pad(message)
        iv = hex(0).zfill(16)
        cipher = AES.new(self.key, AES.MODE_CBC, iv)
        return base64.b64encode(iv + cipher.encrypt(message)).decode('utf-8')

    def decrypt(self, enc):
        enc = base64.b64decode(enc)
        iv = enc[:AES.block_size]
        cipher = AES.new(self.key, AES.MODE_CBC, iv)
        return self._unpad(cipher.decrypt(enc[AES.block_size:])).decode('utf-8')

    def _pad(self, s):
        return s + (self.bs - len(s) % self.bs) * chr(self.bs - len(s) % self.bs)

    @staticmethod
    def _unpad(s):
        return s[:-ord(s[len(s)-1:])]


def encrypt_file(input_file, output_file, key):
    instance = AESCipher(key)
    with open(input_file) as f_read:
        text = ''.join(f_read.readlines())
        cipher_text = instance.encrypt(text)

    with open(output_file, 'w') as write_file:
        write_file.write(cipher_text)


def decrypt_file(input_file, output_file, key):
    instance = AESCipher(key)
    with open(input_file) as f_read:
        text = ''.join(f_read.readlines())
        decrypted_text = instance.decrypt(text)

    with open(output_file, 'w') as write_file:
        write_file.write(decrypted_text)


def encrypt_string(input, password, bs=16):
    raw = pkcs7padding(unicode(input, 'UTF-8'))
    key = generateKey(unicode(password, 'UTF-8'))

    iv = hex(0).zfill(16)
    cipher = AES.new(key, AES.MODE_CBC, iv)

    return base64.b64encode(cipher.encrypt(raw))


def decrypt_string(input, password):
    raw = base64.b64decode(input)
    key = generateKey(unicode(password, 'UTF-8'))

    iv = hex(0).zfill(16)
    cipher = AES.new(key, AES.MODE_CBC, iv)

    return pkcs7unpadding(cipher.decrypt(raw))


def pkcs7padding(data, bs=16):
    padding = bs - len(data) % bs
    padding_text = chr(padding) * padding
    return data + padding_text


def pkcs7unpadding(data):
    lengt = len(data)
    unpadding = ord(data[lengt - 1])
    return data[0:lengt-unpadding]


def generateKey(password):
    return hashlib.sha256(password).digest()


if __name__ == '__main__':
    # if True:
    #     message = encrypt_string('hello world', 'password', 32)
    #     print(message)
    #     message = encrypt_string('hello world', 'password', 16)
    #     print(message)
    #
    #     message = decrypt_string('lXYjQ1aVglqi8hb5MqrZuQ==', 'password')
    #     print(message)
    #     message = decrypt_string('Q+rfwdJ2MYMW3TvOP1N+oQ==', 'password')
    #     print(message)
    #     import sys
    #     sys.exit(0)

    import os

    f1 = '1.txt'
    f2 = '2.txt'
    f3 = '3.txt'

    with open(f1, 'w') as f:
        f.write('this is a test\nthis is another test')

    imei = '990000862471854'

    encrypt_file(f1, f2, imei)
    decrypt_file(f2, f3, imei)

    text1 = ''.join(open(f1).readlines())
    text2 = ''.join(open(f3).readlines())
    print(text1)
    print(text2)
    if text1 == text2:
        print('pass')
    else:
        print('failed')

    os.remove(f1)
    os.remove(f2)
    os.remove(f3)
