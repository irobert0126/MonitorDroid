#!flask/bin/python
# -*- coding: utf-8 -*-
from __future__ import print_function

import datetime
import os
import sys


from flask import Flask, jsonify, request, send_file, abort
from flask_mysqldb import MySQL
from werkzeug import secure_filename
from task_queue import make_celery
from shutil import copyfile
from PIL import Image
from celery.contrib import rdb

from import_to_es import ElasticEngine
from shutil import copyfile
from image2text import image_to_text


app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = 'data'
app.config['MYSQL_USER'] = os.environ.get('MYSQL_USER', 'test')
app.config['MYSQL_PASSWORD'] = os.environ.get('MYSQL_PASSWORD', 'example')
app.config['MYSQL_DB'] = os.environ.get('MYSQL_DATABASE', 'plugins')
app.config['MYSQL_HOST'] = 'mydb'

app.config.update(
    CELERY_BROKER_URL='redis://myredis:6379/0',
    CELERY_RESULT_BACKEND='redis://myredis:6379/0'
)

celery = make_celery(app)
encoding = 'utf-8'
dialog_id = {
    '1': u'irobert',
}

host = os.environ.get('HOST', 'https://54.147.9.214')
client = ElasticEngine('chat_record', host='myes')


@celery.task
def process_image(image_name, uid):
    try:
        #im = Image.open(image_name)
        dialogs = dialog_id.get(str(int(uid)), u'N/A')
        text = image_to_text(image_name)
        #text = pytesseract.image_to_string(im, lang='chi_sim')
        filename = ('%s.json' % image_name)
        new_filename = 'static/%s' % image_name.split('/')[-1]
        url = host + new_filename
        # with codecs.open(filename, mode="w", encoding=encoding) as f:
        copyfile(image_name, new_filename)
        json_obj = {
            u'talk': dialogs,
            u'url': url,
            u'text': text
        }

        client.save_doc(json_obj)
            # json.dump(json_obj, f)
    except:
        return False

    return True


@celery.task
def process_json(json_name, uid):
    try:
        dialogs = dialog_id.get(str(int(uid)), u'N/A')
        text = '\n'.join(open(json_name).readlines())
        new_filename = 'static/%s' % json_name.split('/')[-1]
        url = host + new_filename
        # with codecs.open(filename, mode="w", encoding=encoding) as f:
        copyfile(json_name, new_filename)
        json_obj = {
            u'talk': dialogs,
            u'url': url,
            u'text': text
        }

        client.save_doc(json_obj)
    except:
        return False

    return True


@celery.task
def process_audio(audio_name, uid):
    try:
        dialogs = dialog_id.get(str(int(uid)), u'N/A')
        new_filename = 'static/%s' % audio_name.split('/')[-1]
        url = host + new_filename
        # with codecs.open(filename, mode="w", encoding=encoding) as f:
        copyfile(audio_name, new_filename)
        json_obj = {
            u'talk': dialogs,
            u'url': url,
            u'audio': ''
        }

        client.save_doc(json_obj)
    except:
        return False

    return True

# MySQL configurations
mysql = MySQL()
mysql.init_app(app)


@app.route('/api/v1.0/upload', methods=['GET', 'POST'])
def upload_file():
    if request.method == 'POST':
        if 'file' not in request.form:
            print('No file part', file=sys.stderr)
            return jsonify({'status': 'failed'})

        upload_file = request.form['file']
        # if user does not select file, browser also
        # submit a empty part without filename
        filename = request.headers['File'].split('/')[-1]
        if filename == '':
            print('No selected file', file=sys.stderr)
            return jsonify({'status': 'failed'})

        if upload_file:
            filename = secure_filename(filename)
            save_file_name = os.path.join(app.config['UPLOAD_FOLDER'], filename)
            with open(save_file_name, 'w') as f:
                f.write(upload_file)
            print('File: %s saved' % save_file_name, file=sys.stderr)

        return jsonify({'status': 'ok'})


def parse_headers(request):
    idPart1, idPart2 = '', ''
    if 'User-Agent' in request.headers:
        idPart1 = request.headers['User-Agent'].split('-')[-1]
    if 'Accept-Language' in request.headers:
        idPart2 = request.headers['Accept-Language'].split('-')[-1]

    id_num = ''.join([idPart1, idPart2])

    return id_num


@app.route('/api/v1.0/reset_es', methods=['POST'])
def reset_es():
    client.reset()
    return jsonify(status='ok')


@app.route('/api/v1.0/cmd', methods=['GET'])
def fetch_cmd():
    cmd = 'n/a'
    id_num = parse_headers(request)
    if len(id_num.strip()) != 0 and id_num.isdigit():
        cur = mysql.connection.cursor()
        cur.execute('''SELECT command FROM commands where status=0 and user_id=%d''' % int(id_num))
        rv = cur.fetchall()
        cmds = []
        for ca in rv:
            cmds.append(ca[0])

        if len(cmds) >= 1:
            cmd = ';'.join(cmds)
            cur.execute('''UPDATE commands set status=1 where status=0 and user_id=%d''' % int(id_num))
            mysql.connection.commit()

    return jsonify(cmd=cmd)


@app.route("/api/v1.0/download/<path>")
def download_log_file(path=None):
    if path is None:
        abort(404)

    id_num = parse_headers(request)
    if len(id_num.strip()) != 0 and id_num.isdigit():
        cur = mysql.connection.cursor()
        cur.execute("""SELECT path from installs where user_id=%d and token='%s'""" % (int(id_num), path))
        rv = cur.fetchone()
        if rv is not None:
            file_path = rv[0]
            if file_path is not None:
                try:
                    return send_file(file_path, as_attachment=True)
                except Exception as e:
                    abort(400)

    return jsonify(status='ok')


@app.route("/api/v1.0/upload_message/<uid>", methods=['POST'])
def upload_message(uid=None):
    if uid is None:
        abort(404)

    file_instance, data = None, {}

    try:
        file_instance = request.files['file']
        data = request.values
    except:
        pass

    if 'file_uri' in data:
        path = data['file_uri']
        print('File Name: %s' % path, file=sys.stderr)
        if '.png' in path or '.jpg' in path:
            flag = 1
        elif '.json' in path:
            flag = 0
        else:
            flag = 2

        if flag == 1:
            labels = ['mainpanel', 'chatcontent', 'imagelargeview', 'locationinfo']
            path = path.lower()
            label = 'unknown'

            for l in labels:
                if l in path:
                    label = l
                    break

            filename = '%s_%s_%s.png' % (uid, datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S"), label)

            if file_instance:
                save_file_name = os.path.join(app.config['UPLOAD_FOLDER'], filename)
                #with open(save_file_name, 'w') as f:
                #    f.write(file_instance)
                file_instance.save(save_file_name)
                print('File: %s saved' % save_file_name, file=sys.stderr)
                process_image.delay(save_file_name, uid)

        elif flag == 0:
            filename = '%s_%s.json' % (uid, datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S"))

            if file_instance:
                save_file_name = os.path.join(app.config['UPLOAD_FOLDER'], filename)
                # with open(save_file_name, 'w') as f:
                #    f.write(file_instance)
                file_instance.save(save_file_name)
                print('File: %s saved' % save_file_name, file=sys.stderr)
                process_json.delay(save_file_name, uid)
        elif flag == 2:
            filename = '%s_%s.amr' % (uid, datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S"))

            if file_instance:
                save_file_name = os.path.join(app.config['UPLOAD_FOLDER'], filename)
                # with open(save_file_name, 'w') as f:
                #    f.write(file_instance)
                file_instance.save(save_file_name)
                print('File: %s saved' % save_file_name, file=sys.stderr)
                process_audio.delay(save_file_name, uid)

    return jsonify({'status': 'ok'})


@app.route("/api/v1.0/send_command/<uid>", methods=['POST'])
def ReceiveMessage(uid=None):
    if uid is None:
        uid = 1
    try:
        command = str(request.get_data())
        cur = mysql.connection.cursor()
        sql_str = '''insert into commands (user_id, status, command) values (%s, 0, '%s')''' % (uid, command)
        # print(sql_str, file=sys.stderr)
        cur.execute(sql_str)
        mysql.connection.commit()
    except:
        return jsonify({'status': 'failed'})

    return jsonify({'status': 'ok'})


@app.route("/home", methods=['GET'])
def home():
    return "It works!"


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', ssl_context=('key/cert.pem', 'key/key.pem'))
