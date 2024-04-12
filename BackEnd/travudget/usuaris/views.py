import django
django.setup()

from rest_framework import status
from rest_framework.decorators import api_view
from rest_framework.response import Response
from .models import Usuari
from .serializers import UsuariSerializer

@api_view(['POST'])
def sign_up(request):
    name = request.data.get('name', None)
    email = request.data.get('email', None)

    try:
        usuari = Usuari.objects.get(email=email)
        return Response(status=status.HTTP_200_OK)
    except Usuari.DoesNotExist:
        usuari = Usuari(email=email, nom_usuari=name, username=name)
        usuari.save()
        return Response(status=status.HTTP_201_CREATED)
    
@api_view(['GET'])
def get_usuari(request, email):
    try:
        usuari = Usuari.objects.get(email=email)
        serializer = UsuariSerializer(usuari)
        return Response(serializer.data, status=status.HTTP_200_OK)
    except Usuari.DoesNotExist:
        return Response({"message": "L'usuari no existeix"}, status=status.HTTP_404_NOT_FOUND)