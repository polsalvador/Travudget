from django.db import models
from usuaris.models import Usuari

class Viatge(models.Model):
    nomViatge = models.CharField(max_length=255)
    dataInici = models.DateField()
    dataFi = models.DateField(null=True, blank=True)
    divisa = models.CharField(max_length=255)
    pressupostTotal = models.IntegerField()
    pressupostVariable = models.JSONField(default=list)
    deutes = models.JSONField(default=list)
    codi = models.CharField(max_length=255, unique=True)
    creador = models.ForeignKey(Usuari, related_name='viatges_creats', on_delete=models.CASCADE)
    participants = models.ManyToManyField(Usuari, related_name='viajes_on_participa')