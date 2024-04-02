from django.test import TestCase
from rest_framework.test import APIRequestFactory
from rest_framework import status
from .models import Despesa
from viatges.models import Viatge
from usuaris.models import Usuari
from .views import create_or_get_despesa, get_or_edit_or_delete_despesa
import json
from datetime import date

class DespesaTestCase(TestCase):
    def setUp(self):
        self.factory = APIRequestFactory()

    def test_create_despesa(self):
        email = "polsalvador@gmail.com"
        nom_despesa = "Despesa a la FIB"

        usuari = Usuari.objects.create(email=email)
        viatge = Viatge.objects.create(
            nomViatge="Viatge a la FIB",
            creador=usuari,
            dataInici = "2024-06-23",
            dataFi = "2024-06-28",
            divisa="EUR",
            pressupostTotal=200
        )

        deutors = {"Pau": 100, "Marc": 50}

        request = self.factory.post(f'/usuaris/{email}/viatges/{viatge.id}/despeses', {
            'nomDespesa': nom_despesa,
            'creador': email,
            'descripcio': "Descripció de la despesa",
            'preu': 50,
            'categoria': 'Menjar',
            'dataInici': date.today(),
            'dataFi': date.today(),
            'ubicacio_lat': 40.7128,
            'ubicacio_long': -74.0060,
            'deutors': json.dumps(deutors),
        })

        response = create_or_get_despesa(request, email, viatge.id)

        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertTrue(Despesa.objects.filter(nomDespesa=nom_despesa, creador=usuari).exists())

    def test_get_despeses_empty_success(self):
        email = "polsalvador@gmail.com"

        usuari = Usuari.objects.create(email=email)
        viatge = Viatge.objects.create(
            nomViatge="Viatge a la FIB",
            creador=usuari,
            divisa="EUR",
            pressupostTotal=200
        )

        request = self.factory.get(f'/usuaris/{email}/viatges/{viatge.id}/despeses')
        response = create_or_get_despesa(request, email, viatge.id)

        self.assertEqual(response.status_code, status.HTTP_200_OK)
    
    def test_get_despeses_some_success(self):
        email = "polsalvador@gmail.com"

        usuari = Usuari.objects.create(email=email)
        viatge = Viatge.objects.create(
            nomViatge="Viatge a la FIB",
            creador=usuari,
            divisa="EUR",
            pressupostTotal=200
        )

        Despesa.objects.create(
            nomDespesa="Pitifli a la FIB",
            viatge=viatge,
            creador=email,
            preu=30,
            categoria="Menjar",
            dataInici=date.today(),
        )

        Despesa.objects.create(
            nomDespesa="Ordinador",
            viatge=viatge,
            creador=email,
            preu=200,
            categoria="Turisme",
            dataInici=date.today(),
        )

        request = self.factory.get(f'/usuaris/{email}/viatges/{viatge.id}/despeses')

        response = create_or_get_despesa(request, email, viatge.id)

        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 2)

    def test_get_despesa_success(self):
        email = "polsalvador@gmail.com"

        usuari = Usuari.objects.create(email=email)
        
        viatge = Viatge.objects.create(
            nomViatge="Viatge de prova",
            creador=usuari,
            divisa="EUR",
            pressupostTotal=200
        )

        despesa = Despesa.objects.create(
            nomDespesa="Dinar",
            viatge=viatge,
            creador=email,
            preu=20,
            categoria="Menjar",
            dataInici=date.today(),
        )

        request = self.factory.get(f'/usuaris/{email}/viatges/{viatge.id}/despeses/{despesa.id}')
        response = get_or_edit_or_delete_despesa(request, email, viatge.id, despesa.id)

        self.assertEqual(response.status_code, status.HTTP_200_OK)

    def test_edit_despesa_success(self):
        email = "polsalvador@gmail.com"

        usuari = Usuari.objects.create(email=email)
        
        viatge = Viatge.objects.create(
            nomViatge="Viatge de prova",
            creador=usuari,
            divisa="EUR",
            pressupostTotal=200
        )

        despesa = Despesa.objects.create(
            nomDespesa="Dinar",
            viatge=viatge,
            creador=email,
            preu=20,
            categoria="Menjar",
            dataInici=date.today(),
        )

        request = self.factory.put(f'/usuaris/{email}/viatges/{viatge.id}/despeses/{despesa.id}', {
            'nomDespesa': "Dinar modificat",
            'creador': email,
            'descripcio': "Descripció de la despesa modificada",
            'preu': 30,
            'categoria': 'Menjar',
            'dataInici': date.today(),
            'ubicacio_lat': 40.7128,
            'ubicacio_long': -74.0060,
            'deutors': json.dumps({"Pau": 100, "Marc": 50}),
        }, format='json')
        
        response = get_or_edit_or_delete_despesa(request, email, viatge.id, despesa.id)

        self.assertEqual(response.status_code, status.HTTP_200_OK)

    def test_delete_despesa_success(self):
        email = "polsalvador@gmail.com"

        usuari = Usuari.objects.create(email=email)
        
        viatge = Viatge.objects.create(
            nomViatge="Viatge de prova",
            creador=usuari,
            divisa="EUR",
            pressupostTotal=200
        )

        despesa = Despesa.objects.create(
            nomDespesa="Dinar",
            viatge=viatge,
            creador=email,
            preu=20,
            categoria="Menjar",
            dataInici=date.today(),
        )

        request = self.factory.delete(f'/usuaris/{email}/viatges/{viatge.id}/despeses/{despesa.id}')
        
        response = get_or_edit_or_delete_despesa(request, email, viatge.id, despesa.id)

        self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)
        self.assertFalse(Despesa.objects.filter(id=despesa.id).exists())
    
    def test_get_despeses_with_filters(self):
        email = "polsalvador@gmail.com"

        usuari = Usuari.objects.create(email=email)
        viatge = Viatge.objects.create(
            nomViatge="Viatge a la FIB",
            creador=usuari,
            divisa="EUR",
            pressupostTotal=200
        )

        Despesa.objects.create(
            nomDespesa="Pitifli a la FIB",
            viatge=viatge,
            creador=email,
            preu=30,
            categoria="Menjar",
            dataInici=date.today(),
        )

        Despesa.objects.create(
            nomDespesa="Ordinador",
            viatge=viatge,
            creador=email,
            preu=200,
            categoria="Turisme",
            dataInici=date.today(),
        )

        url = '/usuaris/polsalvador@gmail.com/viatges/1/despeses?preuMinim=10&preuMaxim=100&categoria=Menjar&categoria=Turisme'
        print("URL: ", url)
        request = self.factory.get(url)

        response = create_or_get_despesa(request, email, viatge.id)
        print("RESPONSE DATA: ", response.data)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 1)

        despesa = response.data[0]
        self.assertEqual(despesa['nomDespesa'], "Pitifli a la FIB")
