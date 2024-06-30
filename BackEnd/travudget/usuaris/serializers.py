from rest_framework import serializers
from .models import Usuari

class UsuariSerializer(serializers.ModelSerializer):
    recompenses_bescanviades = serializers.SerializerMethodField()

    class Meta:
        model = Usuari
        fields = '__all__'
    
    def get_recompenses_bescanviades(self, obj):
        return [recompensa.codi for recompensa in obj.recompenses.all()]