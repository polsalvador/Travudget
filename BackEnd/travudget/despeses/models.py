from django.db import models
from viatges.models import Viatge

class Despesa(models.Model):
    nomDespesa = models.CharField(max_length=255)
    viatge = models.ForeignKey(Viatge, related_name='despesa_viatge', on_delete=models.CASCADE)
    creador = models.CharField(max_length=255)
    descripcio = models.CharField(max_length=255, blank=True, null=True)
    preu = models.IntegerField()
    TCategoria_choices = (
        ('Menjar', 'Menjar'),
        ('Compres', 'Compres'),
        ('Turisme', 'Turisme'),
        ('Allotjament', 'Allotjament'),
        ('Transport', 'Transport'),
        ('Altres', 'Altres'),
    )
    categoria = models.CharField(max_length=255, choices=TCategoria_choices)
    dataInici = models.DateField(blank=True, null=True)
    dataFi = models.DateField(blank=True, null=True)
    ubicacio_lat = models.FloatField(blank=True, null=True)
    ubicacio_long = models.FloatField(blank=True, null=True)
    deutors = models.JSONField(default=dict, blank=True, null=True)

    def __str__(self):
        return self.nomDespesa