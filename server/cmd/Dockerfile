############################################################
# Dockerfile to build Flask App
# Based on
############################################################

# Set the base image
FROM debian:latest

# File Author / Maintainer
MAINTAINER Zhxu


RUN apt-get update && apt-get install -y apache2 \
    libapache2-mod-wsgi \
    build-essential \
    python \
    python-dev\
    python-pip \
    default-libmysqlclient-dev \
    python-celery-common \
 && apt-get clean \
 && apt-get autoremove \
 && rm -rf /var/lib/apt/lists/*

# Copy over and install the requirements
COPY app/requirements.txt /var/www/apache-flask/app/requirements.txt
RUN pip install -r /var/www/apache-flask/app/requirements.txt

# Copy over the apache configuration file and enable the site
COPY apache-flask.conf /etc/apache2/sites-available/apache-flask.conf
RUN a2ensite apache-flask
RUN a2enmod headers
RUN a2enmod ssl

# Copy over the wsgi file
COPY apache-flask.wsgi /var/www/apache-flask/apache-flask.wsgi

COPY run.py /var/www/apache-flask/run.py
COPY app /var/www/apache-flask/app/
COPY key /var/www/apache-flask/key/

RUN a2dissite 000-default.conf
RUN a2ensite apache-flask.conf


EXPOSE 443

WORKDIR /var/www/apache-flask

# CMD ["/bin/bash"]
CMD  sleep 40s && /usr/sbin/apache2ctl start && cd /var/www/apache-flask/app && celery -A app.celery --loglevel=DEBUG worker
# The commands below get apache running but there are issues accessing it online
# The port is only available if you go to another port first
# ENTRYPOINT ["/sbin/init"]
# CMD ["/usr/sbin/apache2ctl"]