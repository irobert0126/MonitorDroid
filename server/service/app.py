#!flask/bin/python
# -*- coding: utf-8 -*-
from __future__ import print_function

import base64
import datetime
import json

import flask_login
import os
import sys


from flask import Flask, jsonify, request, send_file, abort
from flask_mysqldb import MySQL
from flask_login import LoginManager, UserMixin
from werkzeug.utils import secure_filename

from util import encrypt_file, decrypt_file


app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = 'data'
app.config['MYSQL_USER'] = os.environ.get('MYSQL_USER', 'test')
app.config['MYSQL_PASSWORD'] = os.environ.get('MYSQL_PASSWORD', 'example')
app.config['MYSQL_DB'] = os.environ.get('MYSQL_DATABASE', 'plugins')
app.config['MYSQL_HOST'] = 'localhost'


# MySQL configurations
mysql = MySQL()
mysql.init_app(app)

login_manager = LoginManager()
login_manager.init_app(app)


class User(UserMixin):
    def __init__(self, uid, password, active):
        self.uid = uid
        self.password = password
        self.active = active
        self.authenticated = False

    def authenticate(self, uid=None, password=None):
        self.authenticated = False
        if id is None:
            pass
        else:
            if self.uid == uid and self.password.strip() == password.strip():
                self.authenticated = True
        return

    @property
    def is_active(self):
        try:
            return int(self.active) == 1
        except Exception as e:
            return False

    @property
    def is_authenticated(self):
        return self.authenticated


def load_users_from_file(user_file='users.txt'):
    users = {}
    with open(user_file) as f:
        for l in f:
            uid, token, active = l.split(':', 2)
            user = User(uid=uid, password=token, active=active)
            users[uid] = user

    return users

users = load_users_from_file()


def get_filename(user):
    filename = '%05d_%s' % (int(user.uid), user.password)
    abs_filename = os.path.join(app.config['UPLOAD_FOLDER'], filename)

    return abs_filename


def process_conf_file(data, user):
    abs_filename = get_filename(user)
    read_json = json.loads(data)

    if 'imei' in read_json:
        key = read_json['imei']
        encrypt_file('data/conf/apk.conf', abs_filename + '.enc', key)
        json.dump(read_json, open(abs_filename + '.json', 'w'))

        return True

    return False


@login_manager.user_loader
def load_user(user_id):
    uid, token = user_id.strip().split(':', 1)
    if uid not in users:
        return None

    user = users[uid]
    user.authenticate(uid=uid, password=token)
    return user


@login_manager.unauthorized_handler
def unauthorized_handler():
    return 'Unauthorized'


@login_manager.request_loader
def load_user_from_request(request):
    # next, try to login using Basic Auth
    api_key = request.headers.get('Authorization')
    if api_key:
        api_key = api_key.replace('Basic ', '', 1)
        try:
            api_key = base64.b64decode(api_key)
        except TypeError:
            pass

        user = load_user(api_key)
        if user:
            return user

    # finally, return None if both methods did not login the user
    return None


@app.route('/conf/v1.0/download', methods=['GET'])
@flask_login.login_required
def download_config():
    if request.method == 'GET':
        filename = get_filename(flask_login.current_user) + '.enc'
        return send_file(filename)

    return jsonify({'status': 'failed'})


@flask_login.login_required
@app.route('/conf/v1.0/upload', methods=['POST'])
def receive_config():
    if request.method == 'POST':
        data = request.data
        print('%s' % data, file=sys.stderr)

        if data:
            print('%s, %s' % (flask_login.current_user.uid, flask_login.current_user.password), file=sys.stderr)
            if process_conf_file(data, flask_login.current_user) is False:
                return jsonify({'status': 'failed'})

        return jsonify({'status': 'ok'})


@app.route('/conf/v1.0/getip', methods=['GET'])
def fetch_ip():
    return 'https://35.174.171.219/'


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')
