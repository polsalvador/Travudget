from rest_framework import status
from rest_framework.decorators import api_view
from rest_framework.response import Response
# from .models import Viatge
# from .serializers import ViatgeSerializer

from django.http import JsonResponse


# @api_view(['POST'])
# def create_trip(request):
#     serializer = ViatgeSerializer(data=request.data)
#     if serializer.is_valid():
#         serializer.save()
#         return Response(serializer.data, status=status.HTTP_201_CREATED)
#     return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

# def ping(request):
#     return JsonResponse({'message': 'The connection is working'}, status=200)