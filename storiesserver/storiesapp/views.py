from django.http import HttpResponse, JsonResponse

import os, time

from opentok import OpenTok, MediaModes, ArchiveModes

api_key = os.environ["OPENTOK_API_KEY"]
api_secret = os.environ["OPENTOK_API_SECRET"]
opentok = OpenTok(api_key, api_secret)

videos = [
]

index = 1

def get_token(request):
    global opentok
    session = opentok.create_session(media_mode=MediaModes.routed, archive_mode=ArchiveModes.manual)
    token = session.generate_token(expire_time=int(time.time()) + 200)
    return JsonResponse({"token": token, "session": session.session_id,
                        "api_key": api_key})

def video_stream(request, archive_id):
    global opentok
    video = opentok.get_archive(archive_id)
    return HttpResponse(video.url)

def videos_list(request):
    global videos
    return JsonResponse(videos, safe=False)

def video_start_archive(request, session_id):
    global index, videos
    name = f"Story {index}"
    archive = opentok.start_archive(session_id)
    index += 1
    videos.append({
        "name": name,
        "archive_id": archive.id
    })
    return HttpResponse(f"{archive.id}")

def video_stop_archive(request, archive_id):
    global opentok
    opentok.stop_archive(archive_id)
    return HttpResponse("Stop Archiving")

def homepage(request):
    return HttpResponse("Hello")