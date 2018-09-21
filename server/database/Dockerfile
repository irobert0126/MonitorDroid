FROM mysql:5.7

ENV MYSQL_ROOT_PASSWORD=example
ENV MYSQL_USER=test
ENV MYSQL_PASSWORD=example
ENV MYSQL_DATABASE=plugins

VOLUME ./dbdata:/var/lib/mysql
COPY init.sql /docker-entrypoint-initdb.d/

CMD ["mysqld"]