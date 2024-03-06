FROM python:3.9-slim-buster

WORKDIR /app

COPY . /app

ENV DJANGO_SETTINGS_MODULE=travudget.settings

RUN apt-get update && apt-get install -y \
    gcc \
    default-libmysqlclient-dev \
    pkg-config \
    && pip install daphne \
    && pip install django \
    && pip install mysqlclient \
    && pip install djangorestframework \ 
    && rm -rf /var/lib/apt/lists/*

# RUN pip install --no-cache-dir -r requirements.txt

EXPOSE 8000

CMD ["daphne", "-b", "0.0.0.0", "-p", "8000", "travudget.asgi:application"]