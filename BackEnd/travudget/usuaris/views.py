from rest_framework import status
from rest_framework.decorators import api_view
from rest_framework.response import Response
from .models import Usuari

@api_view(['POST'])
def sign_up(request):
    name = request.data.get('name', None)
    email = request.data.get('email', None)

    try:
        usuari = Usuari.objects.get(email=email)
        return Response(status=status.HTTP_200_OK)
    except Usuari.DoesNotExist:
        usuari = Usuari(email=email, nom_usuari=name)
        usuari.save()
        return Response(status=status.HTTP_201_CREATED)
