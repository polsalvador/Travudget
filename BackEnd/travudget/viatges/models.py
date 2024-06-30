from django.db import models
from usuaris.models import Usuari

class Viatge(models.Model):
    nomViatge = models.CharField(max_length=255)
    dataInici = models.DateField(null=True, blank=True)
    dataFi = models.DateField(null=True, blank=True)
    divisa = models.CharField(max_length=255)
    pressupostTotal = models.IntegerField()
    pressupostVariable = models.JSONField(default=dict)
    deutes = models.JSONField(default=dict)
    codi = models.CharField(max_length=255, unique=True)
    creador = models.ForeignKey(Usuari, related_name='viatges_creats', on_delete=models.CASCADE)
    participants = models.ManyToManyField(Usuari, related_name='viajes_on_participa', blank=True)