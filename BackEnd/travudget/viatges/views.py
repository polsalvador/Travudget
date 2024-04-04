import django
django.setup()

from rest_framework import status
from rest_framework.decorators import api_view
from rest_framework.response import Response
from .models import Viatge
from usuaris.models import Usuari
from .serializers import ViatgeSerializer
import random
import string

@api_view(['POST', 'GET'])
def create_or_get_viatge(request, email):
    if request.method == 'POST':
        nom_viatge = request.data.get('nomViatge')
        data_inici = request.data.get('dataInici')
        data_fi = request.data.get('dataFi')
        divisa = request.data.get('divisa')

        try:
            usuari = Usuari.objects.get(email=email)

        except Usuari.DoesNotExist:
            return Response({"message": "L'usuari no existeix"}, status=status.HTTP_404_NOT_FOUND)

        codi = None
        while not codi or Viatge.objects.filter(codi=codi).exists():
            codi = ''.join(random.choice(string.ascii_uppercase) for _ in range(6))

        serializer = ViatgeSerializer(data={
            'nomViatge': nom_viatge,
            'dataInici': data_inici,
            'dataFi': data_fi,
            'divisa': divisa,
            'creador': usuari.id,
            'codi': codi,
            'pressupostTotal': 0,
        })

        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        else:
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
    elif request.method == 'GET':
        try:
            usuari = Usuari.objects.get(email=email)
            viatges = Viatge.objects.filter(creador=usuari.id)
            serializer = ViatgeSerializer(viatges, many=True)
            return Response(serializer.data)
        except Usuari.DoesNotExist:
            return Response({"message": "L'usuari no existeix"}, status=status.HTTP_404_NOT_FOUND)

@api_view(['GET',  'PUT', 'DELETE'])
def get_or_edit_or_delete_viatge(request, email, id):
    if request.method == 'GET':
        try:
            usuari = Usuari.objects.get(email=email)
            viatge = Viatge.objects.get(id=int(id), creador_id=usuari.id)
            serializer = ViatgeSerializer(viatge)
            return Response(serializer.data)
        except Usuari.DoesNotExist:
            return Response({"message": "L'usuari no existeix"}, status=status.HTTP_404_NOT_FOUND)
        except Viatge.DoesNotExist:
            return Response({"message": "El viatge no existeix o no pertany a l'usuari"}, status=status.HTTP_404_NOT_FOUND)
    elif request.method == 'PUT':
        try:
            usuari = Usuari.objects.get(email=email)
            viatge = Viatge.objects.get(id=int(id), creador_id=usuari.id)
        except Usuari.DoesNotExist:
            return Response({"message": "L'usuari no existeix"}, status=status.HTTP_404_NOT_FOUND)
        except Viatge.DoesNotExist:
            return Response({"message": "El viatge no existeix o no pertany a l'usuari"}, status=status.HTTP_404_NOT_FOUND)

        serializer = ViatgeSerializer(viatge, data=request.data, partial=True)
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
        except Usuari.DoesNotExist:
            return Response({"message": "L'usuari no existeix"}, status=status.HTTP_404_NOT_FOUND)
        except Viatge.DoesNotExist:
            return Response({"message": "El viatge no existeix o no pertany a l'usuari"}, status=status.HTTP_404_NOT_FOUND)
        viatge.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)

@api_view(['DELETE'])
def eject_viatge(request, email, id):
    try:
        usuari = Usuari.objects.get(email=email)
        viatge = Viatge.objects.get(id=int(id))
        viatge.participants.remove(usuari)
        return Response(status=status.HTTP_204_NO_CONTENT)
    except Viatge.DoesNotExist:
        return Response({"message": "El viatge no existeix"}, status=status.HTTP_404_NOT_FOUND)
        
        
@api_view(['GET', 'POST'])
def get_viatges_participant_or_join(request, email):
    if request.method == 'GET':
        try:
            usuari = Usuari.objects.get(email=email)
            viatges_ids = Viatge.objects.filter(participants=usuari).values_list('id', flat=True)
            viatges = Viatge.objects.filter(id__in=viatges_ids)
            serializer = ViatgeSerializer(viatges, many=True)
            
            for data in serializer.data:
                creador_id = data['creador']
                creador = Usuari.objects.get(id=creador_id)
                data['creador_email'] = creador.email

            return Response(serializer.data)
        except Usuari.DoesNotExist:
            return Response({"message": "L'usuari no existeix"}, status=status.HTTP_404_NOT_FOUND)
    
    elif request.method == 'POST':
        codi = request.data.get('codi', None)
        if codi is not None:
            try:
                usuari = Usuari.objects.get(email=email)
                viatge = Viatge.objects.get(codi=codi)

                if usuari != viatge.creador:
                    viatge.participants.add(usuari)
                    serializer = ViatgeSerializer(viatge)
                    return Response(serializer.data, status=status.HTTP_201_CREATED)
                else: 
                    return Response({"message": "El codi no és correcte"}, status=status.HTTP_404_NOT_FOUND)
            except Viatge.DoesNotExist:
                return Response({"message": "El viatge no existeix"}, status=status.HTTP_404_NOT_FOUND)
        else:
            return Response({"message": "No s'ha inclós el codi"}, status=status.HTTP_400_BAD_REQUEST)
        
