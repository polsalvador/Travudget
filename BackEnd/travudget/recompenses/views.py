import django
django.setup()

from rest_framework.decorators import api_view
from rest_framework.response import Response
from rest_framework import status
from .models import Recompensa
from usuaris.models import Usuari
from .serializers import RecompensaSerializer
from django.http import JsonResponse

@api_view(['GET', 'POST'])
def get_or_create_recompenses(request):
    if request.method == 'GET':
        recompenses = Recompensa.objects.all()
        serializer = RecompensaSerializer(recompenses, many=True)
        return Response(serializer.data)
    elif request.method == 'POST':
        serializer = RecompensaSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

@api_view(['GET'])
def get_recompensa(request, email, idRec):
    usuari = Usuari.objects.get(email=email)

    try:
        recompensa = Recompensa.objects.get(id=idRec)
        
        if usuari.punts >= recompensa.preu:
            usuari.punts -= recompensa.preu
            usuari.save()

            serializer = RecompensaSerializer(recompensa)
            data = serializer.data
            data['punts_usuari'] = usuari.punts
            usuari.recompenses.add(recompensa)
            return Response(data)
        else:
            return Response({'error': 'No tens suficients punts'}, status=status.HTTP_400_BAD_REQUEST)

    except Recompensa.DoesNotExist:
        return Response({'error': 'La recompensa no existeix'}, status=status.HTTP_404_NOT_FOUND)