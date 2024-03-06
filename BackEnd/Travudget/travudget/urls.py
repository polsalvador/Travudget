from django.contrib import admin
from django.urls import path
from viatges.views import create_trip, ping

urlpatterns = [
    path('viatges', create_trip, name='create_trip'),
    path('viatges/ping', ping),
]