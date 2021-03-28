from django.urls import path

from . import views

urlpatterns = [
    path('', views.homepage, name='index'),
    path('token', views.get_token, name='token'),
    path('videos/<str:archive_id>', views.video_stream, name='videostream'),
    path('videos-list', views.videos_list, name='videoslist'),
    path('video-start-archive/<str:session_id>', views.video_start_archive, name='startarchive'),
    path('video-stop-archive/<str:archive_id>', views.video_stop_archive, name='stoparchive'),
]