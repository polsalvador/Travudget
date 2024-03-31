from django.urls import path
from usuaris.views import sign_up
from viatges.views import create_or_get_viatge, get_or_edit_or_delete_viatge

urlpatterns = [
    path('usuaris', sign_up),
    path('usuaris/<str:email>/viatges', create_or_get_viatge, name='create_or_get_viatge'),
    path('usuaris/<str:email>/viatges/<str:id>', get_or_edit_or_delete_viatge, name='get_or_edit_or_delete_viatge'),
]