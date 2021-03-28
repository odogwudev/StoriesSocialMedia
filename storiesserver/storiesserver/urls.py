from django.contrib import admin
from django.urls import include, path

urlpatterns = [
    path('stories/', include('storiesapp.urls')),
]