import django
django.setup()

from rest_framework.decorators import api_view
from rest_framework.response import Response
from rest_framework import status
from .models import Despesa
from .serializers import DespesaSerializer
from viatges.models import Viatge
from usuaris.models import Usuari
from django.db.models import Q

@api_view(['POST', 'GET'])
def create_or_get_despesa(request, email, id):

    if request.method == 'POST':
        nom_despesa = request.data.get('nomDespesa')
        creador = request.data.get('creador')
        descripcio = request.data.get('descripcio', None)
        preu = int(request.data.get('preu'))
        categoria = request.data.get('categoria')
        data_inici = request.data.get('dataInici', None)
        data_fi = request.data.get('dataFi', None)
        hora = request.data.get('hora', None)
        ubicacio_lat = request.data.get('ubicacio_lat', None)
        ubicacio_long = request.data.get('ubicacio_long', None)
        deutors = request.data.get('deutors', None)

        try:
            usuari = Usuari.objects.get(email=email)
            viatge = Viatge.objects.get(id=int(id), creador_id=usuari.id)

        except Usuari.DoesNotExist:
            return Response({"message": "L'usuari no existeix"}, status=status.HTTP_404_NOT_FOUND)
        except Viatge.DoesNotExist:
            return Response({"message": "El viatge no existeix o no pertany a l'usuari"}, status=status.HTTP_404_NOT_FOUND)

        serializer = DespesaSerializer(data={
            'nomDespesa': nom_despesa,
            'viatge': viatge.id,
            'creador': creador,
            'descripcio': descripcio,
            'preu': preu,
            'categoria': categoria,
            'dataInici': data_inici,
            'dataFi': data_fi,
            'hora': hora,
            'ubicacio_lat': ubicacio_lat,
            'ubicacio_long': ubicacio_long,
            'deutors': deutors,
        })

        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        else:
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
    elif request.method == 'GET':
        try:
            usuari = Usuari.objects.get(email=email)
            viatge = Viatge.objects.get(id=int(id), creador_id=usuari.id)
            despeses = Despesa.objects.filter(viatge=viatge)

            preu_min = request.GET.get('preuMinim', None)
            preu_max = request.GET.get('preuMaxim', None)
            categories = request.GET.getlist('categoria')

            despeses = Despesa.objects.filter(viatge=viatge)

            if preu_min is not None:
                despeses = despeses.filter(preu__gte=int(preu_min))
            if preu_max is not None:
                despeses = despeses.filter(preu__lte=int(preu_max))
            if categories:
                category_filters = [Q(categoria=category) for category in categories]
                combined_filter = category_filters.pop() if category_filters else Q()
                for q in category_filters:
                    combined_filter |= q
                despeses = despeses.filter(combined_filter)

            serializer = DespesaSerializer(despeses, many=True)
            return Response(serializer.data)
        except Usuari.DoesNotExist:
            return Response({"message": "L'usuari no existeix"}, status=status.HTTP_404_NOT_FOUND)
        except Viatge.DoesNotExist:
            return Response({"message": "El viatge no existeix o no pertany a l'usuari"}, status=status.HTTP_404_NOT_FOUND)

@api_view(['GET',  'PUT', 'DELETE'])
def get_or_edit_or_delete_despesa(request, email, id, idDes):
    if request.method == 'GET':
        try:
            usuari = Usuari.objects.get(email=email)
            viatge = Viatge.objects.get(id=int(id), creador_id=usuari.id)
            despesa = Despesa.objects.get(id=int(idDes), viatge=viatge)
            serializer = DespesaSerializer(despesa)
            return Response(serializer.data)
        except Usuari.DoesNotExist:
            return Response({"message": "L'usuari no existeix"}, status=status.HTTP_404_NOT_FOUND)
        except Viatge.DoesNotExist:
            return Response({"message": "El viatge no existeix o no pertany a l'usuari"}, status=status.HTTP_404_NOT_FOUND)
        except Despesa.DoesNotExist:
            return Response({"message": "La despesa no existeix"}, status=status.HTTP_404_NOT_FOUND)
    elif request.method == 'PUT':
        try:
            usuari = Usuari.objects.get(email=email)
            viatge = Viatge.objects.get(id=int(id), creador_id=usuari.id)
            despesa = Despesa.objects.get(id=int(idDes), viatge=viatge)
        except Usuari.DoesNotExist:
            return Response({"message": "L'usuari no existeix"}, status=status.HTTP_404_NOT_FOUND)
        except Viatge.DoesNotExist:
            return Response({"message": "El viatge no existeix o no pertany a l'usuari"}, status=status.HTTP_404_NOT_FOUND)
        except Despesa.DoesNotExist:
            return Response({"message": "La despesa no existeix"}, status=status.HTTP_404_NOT_FOUND)

        serializer = DespesaSerializer(despesa, data=request.data, partial=True)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        else:
            print(serializer)
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    elif request.method == 'DELETE':
        try:
            usuari = Usuari.objects.get(email=email)
            viatge = Viatge.objects.get(id=int(id), creador_id=usuari.id)
            despesa = Despesa.objects.get(id=int(idDes), viatge=viatge)
        except Usuari.DoesNotExist:
            return Response({"message": "L'usuari no existeix"}, status=status.HTTP_404_NOT_FOUND)
        except Viatge.DoesNotExist:
            return Response({"message": "El viatge no existeix o no pertany a l'usuari"}, status=status.HTTP_404_NOT_FOUND)
        except Despesa.DoesNotExist:
            return Response({"message": "La despesa no existeix"}, status=status.HTTP_404_NOT_FOUND)
        
        despesa.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)