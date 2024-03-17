from rest_framework import serializers
from .models import Usuari

class UsuariSerializer(serializers.ModelSerializer):
    class Meta:
        model = Usuari
        fields = '__all__'