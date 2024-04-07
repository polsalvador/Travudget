from django.test import TestCase
from rest_framework import status
from rest_framework.test import APIRequestFactory
from .views import get_or_create_recompenses, get_recompensa
from .models import Recompensa
from usuaris.models import Usuari

class RecompensaTestCase(TestCase):
    def setUp(self):
        self.factory = APIRequestFactory()

    def test_get_or_create_recompenses_get(self):
        request = self.factory.get('/recompenses')
        response = get_or_create_recompenses(request)
        self.assertEqual(response.status_code, status.HTTP_200_OK)

    def test_get_or_create_recompenses_post(self):
        data = {
            'nomRecompensa': 'Recompensa 1',
            'preu': 10,
            'codi': 'COD001'
        }
        request = self.factory.post('/recompenses', data)
        response = get_or_create_recompenses(request)
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertTrue(Recompensa.objects.filter(nomRecompensa='Recompensa 1').exists())

    def test_get_recompensa(self):
        email = "polsalvador@gmail.com"
        nomViatge= "Viatge a la FIB"

        usuari = Usuari.objects.create(email=email)

        recompensa = Recompensa.objects.create(
            nomRecompensa='Recompensa 2',
            preu=20,
            codi='COD002'
        )

        request = self.factory.get(f'/usuaris/{email}/recompenses/{recompensa.id}')

        response = get_recompensa(request, email, recompensa.id)
        self.assertEqual(response.status_code, status.HTTP_200_OK)

        recompensa_data = response.data
        self.assertEqual(recompensa_data['nomRecompensa'], recompensa.nomRecompensa)
        self.assertEqual(recompensa_data['preu'], recompensa.preu)
        self.assertEqual(recompensa_data['codi'], recompensa.codi)
