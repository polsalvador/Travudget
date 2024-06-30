from rest_framework import serializers
from .models import Viatge

class ViatgeSerializer(serializers.ModelSerializer):
    creador_email = serializers.SerializerMethodField()

    class Meta:
        model = Viatge
        fields = '__all__'

    def get_creador_email(self, obj):
        return obj.creador.email
    
    def get_emails_participants(self, obj):
        return [participant.email for participant in obj.participants.all()]