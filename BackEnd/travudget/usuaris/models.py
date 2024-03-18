from django.db import models
from django.contrib.contenttypes.models import ContentType
from django.contrib.auth.models import AbstractUser

class Usuari(AbstractUser):
    email = models.EmailField(unique=True)
    nom_usuari = models.CharField(max_length=255, blank=True, null=True)
    punts = models.IntegerField(default=0)
    groups = models.ManyToManyField('auth.Group', related_name='abstractUser_group', blank=True, verbose_name='groups')
    user_permissions = models.ManyToManyField('auth.Permission', related_name='abstractUser_permission', blank=True, verbose_name='user permissions')

    def __str__(self):
        return self.email
