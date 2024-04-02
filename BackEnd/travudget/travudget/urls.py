from django.urls import path
from usuaris.views import sign_up
from viatges.views import create_or_get_viatge, get_or_edit_or_delete_viatge, join_or_eject_viatge, get_viatges_participant, get_viatges_participant
from despeses.views import create_or_get_despesa, get_or_edit_or_delete_despesa

urlpatterns = [
    path('usuaris', sign_up),
    path('usuaris/<str:email>/viatges', create_or_get_viatge, name='create_or_get_viatge'),
    path('usuaris/<str:email>/viatges/<str:id>', get_or_edit_or_delete_viatge, name='get_or_edit_or_delete_viatge'),
    path('usuaris/<str:email>/viatges/<str:id>/despeses', create_or_get_despesa, name='create_or_get_despesa'),
    path('usuaris/<str:email>/viatges/<str:id>/despeses/<str:idDes>', get_or_edit_or_delete_despesa, name='get_or_edit_or_delete_despesa'),
    path('usuaris/<str:email>/viatges/<str:id>/share', join_or_eject_viatge, name='join_or_eject_viatge'),
    path('usuaris/<str:email>/viatges/share', get_viatges_participant, name='get_viatges_participant')
]