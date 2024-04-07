from django.db import models

class Recompensa(models.Model):
    nomRecompensa = models.CharField(max_length=100)
    preu = models.IntegerField()
    codi = models.CharField(max_length=50)

    def __str__(self):
        return self.nomRecompensa