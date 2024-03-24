from django.test import TestCase
from rest_framework import status
from rest_framework.test import APIRequestFactory
from .views import create_or_get_viatge
from .models import Viatge
from usuaris.models import Usuari

class ViatgeTestCase(TestCase):
    def setUp(self):
        self.factory = APIRequestFactory()

    def test_create_viatge(self):
        email = "polsalvador@gmail.com"
        nomViatge= "Viatge a la FIB"

        usuari = Usuari.objects.create(email=email)

        request = self.factory.post(f'/usuaris/{email}/viatges', {
            'nomViatge': nomViatge,
            'dataInici': "2024-06-23",
            'dataFi': "2024-06-28",
            'divisa': "EUR"
        })

        response = create_or_get_viatge(request, email)

        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertTrue(Viatge.objects.filter(nomViatge=nomViatge, creador=usuari).exists())
    
    def test_create_viatge_missing_attribute(self):
        email = "polsalvador@gmail.com"

        usuari = Usuari.objects.create(email=email)

        request = self.factory.post(f'/usuaris/{email}/viatges', {
            'dataInici': "2024-06-23",
            'dataFi': "2024-06-28",
            'divisa': "EUR"
        })

        response = create_or_get_viatge(request, email)

        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

    def test_get_viatges_empty_success(self):
        email = "polsalvador@gmail.com"

        usuari = Usuari.objects.create(email=email)

        request = self.factory.get(f'/usuaris/{email}/viatges')

        response = create_or_get_viatge(request, email)

        self.assertEqual(response.status_code, status.HTTP_200_OK)
    
    def test_get_viatges_some_success(self):
        email = "polsalvador@gmail.com"

        usuari = Usuari.objects.create(email=email)

        Viatge.objects.create(
            nomViatge="Viatge 1",
            dataInici="2024-07-01",
            dataFi="2024-07-05",
            divisa="EUR",
            creador=usuari,
            codi="ABCDE",
            pressupostTotal=100
        )

        Viatge.objects.create(
            nomViatge="Viatge 2",
            dataInici="2024-07-10",
            dataFi="2024-07-15",
            divisa="USD",
            creador=usuari,
            codi="FGHIJK",
            pressupostTotal=200
        )

        request = self.factory.get(f'/usuaris/{email}/viatges')

        response = create_or_get_viatge(request, email)

        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 2)

    def test_get_viatges_nonexistent_user(self):
        email = "usuarinoexisteix@gmail.com"

        request = self.factory.get(f'/usuaris/{email}/viatges')

        response = create_or_get_viatge(request, email)

        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)