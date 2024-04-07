from django.urls import path
from usuaris.views import sign_up, get_usuari
from viatges.views import create_or_get_viatge, get_or_edit_or_delete_viatge, eject_viatge, get_viatges_participant_or_join
from despeses.views import create_or_get_despesa, get_or_edit_or_delete_despesa
from recompenses.views import get_or_create_recompenses, get_recompensa

urlpatterns = [
    path('usuaris', sign_up),
    path('recompenses', get_or_create_recompenses, name='get_or_create_recompenses'),
    path('usuaris/<str:email>', get_usuari, name='get_usuari'),
    path('usuaris/<str:email>/viatges', create_or_get_viatge, name='create_or_get_viatge'),
    path('usuaris/<str:email>/viatges/share', get_viatges_participant_or_join, name='get_viatges_participant_or_join'),
    path('usuaris/<str:email>/viatges/<str:id>/share', eject_viatge, name='eject_viatge'),
    path('usuaris/<str:email>/viatges/<str:id>', get_or_edit_or_delete_viatge, name='get_or_edit_or_delete_viatge'),
    path('usuaris/<str:email>/viatges/<str:id>/despeses', create_or_get_despesa, name='create_or_get_despesa'),
    path('usuaris/<str:email>/viatges/<str:id>/despeses/<str:idDes>', get_or_edit_or_delete_despesa, name='get_or_edit_or_delete_despesa'),
    path('usuaris/<str:email>/recompenses/<str:idRec>', get_recompensa, name='get_recompensa')
]