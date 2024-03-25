from django.test import TestCase
from rest_framework import status
from rest_framework.test import APIRequestFactory
from .views import create_or_get_viatge, get_or_edit_viatge
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

    def test_get_viatge_success(self):
        email = "polsalvador@gmail.com"

        usuari = Usuari.objects.create(email=email)
        viatge = Viatge.objects.create(
            nomViatge="Viatge de prova",
            dataInici="2024-06-23",
            dataFi="2024-06-28",
            divisa="EUR",
            creador=usuari,
            codi="ABCDE",
            pressupostTotal=200
        )

        request = self.factory.get(f'/usuaris/{email}/viatges/{viatge.id}')

        response = get_or_edit_viatge(request, email, viatge.id)

        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['nomViatge'], "Viatge de prova")

    def test_get_viatge_invalid_user(self):
        email = "usuari_no_existeix@gmail.com"

        request = self.factory.get(f'/usuaris/{email}/viatges/1')

        response = get_or_edit_viatge(request, email, 1)

        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)
        self.assertEqual(response.data['message'], "L'usuari no existeix")

    def test_get_viatge_invalid_viatge(self):
        email = "polsalvador@gmail.com"

        usuari = Usuari.objects.create(email=email)

        request = self.factory.get(f'/usuaris/{email}/viatges/999')

        response = get_or_edit_viatge(request, email, 999)

        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)
        self.assertEqual(response.data['message'], "El viatge no existeix o no pertany a l'usuari")
    
    def test_edit_viatge_success(self):
        email = "polsalvador@gmail.com"
        nom_viatge_original = "Viatge Original"
        nom_viatge_nou = "Viatge Editat"

        usuari = Usuari.objects.create(email=email)
        viatge = Viatge.objects.create(
            nomViatge=nom_viatge_original,
            dataInici="2024-06-23",
            dataFi="2024-06-28",
            divisa="EUR",
            creador=usuari,
            codi="ABCDE",
            pressupostTotal=200
        )

        request = self.factory.put(f'/usuaris/{email}/viatges/{viatge.id}', {
            'nomViatge': nom_viatge_nou,
            'dataInici': "2024-06-25",
            'dataFi': "2024-07-01",
            'divisa': "USD"
        })

        response = get_or_edit_viatge(request, email, viatge.id)

        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertTrue(Viatge.objects.filter(nomViatge=nom_viatge_nou).exists())

    def test_edit_viatge_invalid_user(self):
        email = "usuarinoexisteix@gmail.com"
        nom_viatge_original = "Viatge Original"

        request = self.factory.put(f'/usuaris/{email}/viatges/1', {
            'nomViatge': nom_viatge_original,
            'dataInici': "2024-06-25",
            'dataFi': "2024-07-01",
            'divisa': "USD"
        })

        response = get_or_edit_viatge(request, email, 1)

        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

    def test_edit_viatge_invalid_viatge(self):
        email = "polsalvador@gmail.com"
        nom_viatge_original = "Viatge Original"

        usuari = Usuari.objects.create(email=email)

        request = self.factory.put(f'/usuaris/{email}/viatges/999', {
            'nomViatge': nom_viatge_original,
            'dataInici': "2024-06-25",
            'dataFi': "2024-07-01",
            'divisa': "USD"
        })

        response = get_or_edit_viatge(request, email, 999)

        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)
    
    def test_edit_viatge_success_with_pressupostVariable(self):
        email = "polsalvador@gmail.com"
        nom_viatge_original = "Viatge Original"
        nom_viatge_nou = "Viatge Editat"

        usuari = Usuari.objects.create(email=email)
        viatge = Viatge.objects.create(
            nomViatge=nom_viatge_original,
            dataInici="2024-06-23",
            dataFi="2024-06-28",
            divisa="EUR",
            creador=usuari,
            codi="ABCDE",
            pressupostTotal=200
        )

        request = self.factory.put(f'/usuaris/{email}/viatges/{viatge.id}', {
            'nomViatge': nom_viatge_nou,
            'dataInici': "2024-06-25",
            'dataFi': "2024-07-01",
            'divisa': "USD",
            'pressupostVariable': {
                "2024-06-23": 100,
                "2024-06-24": 150
            }   
        }, format='json')

        response = get_or_edit_viatge(request, email, viatge.id)

        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertTrue(Viatge.objects.filter(nomViatge=nom_viatge_nou).exists())
        viatge_actualizado = Viatge.objects.get(pk=viatge.id)
        self.assertEqual(viatge_actualizado.pressupostVariable["2024-06-23"], 100)