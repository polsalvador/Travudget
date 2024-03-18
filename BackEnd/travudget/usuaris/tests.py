import os
from django.conf import settings

# os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'travudget.settings')
# settings.configure()

from django.test import TestCase
from rest_framework import status
from rest_framework.test import APIRequestFactory
from .views import sign_up
from .models import Usuari

class SignUpTestCase(TestCase):

    def setUp(self):
        self.factory = APIRequestFactory()

    def test_sign_up_created(self):
        name = "Pol Salvador"
        email = "polsalvadorupc@gmail.com"

        request = self.factory.post('/sign_up', {'name': name, 'email': email})

        response = sign_up(request)

        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertTrue(Usuari.objects.filter(email=email).exists())

    def test_sign_up_existing_user(self):
        Usuari.objects.create(email="upcfib@gmail.com", nom_usuari="Upc fib")

        request = self.factory.post('/sign_up', {'email': "upcfib@gmail.com", 'name': "Upc fib"})

        response = sign_up(request)

        self.assertEqual(response.status_code, status.HTTP_200_OK)
