from rest_framework import serializers
from .models import Viatge

class ViatgeSerializer(serializers.ModelSerializer):
    class Meta:
        model = Viatge
        fields = '__all__'