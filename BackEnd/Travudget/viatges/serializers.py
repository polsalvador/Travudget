from rest_framework import serializers
from .models import Viatge

class ViajeSerializer(serializers.ModelSerializer):
    class Meta:
        model = Viatge
        fields = '__all__'