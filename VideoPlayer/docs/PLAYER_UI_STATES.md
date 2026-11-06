# Player UI States

## Step 1 – Player Open
- **Top bar:** back, title, lock, more
- **Center:** previous, rewind 10s, play/pause (light-blue button), forward 10s, next
- **Bottom:** seek bar with times; subtitles, speed, background music, aspect ratio, fullscreen

## Step 2 – Audio & Subtitle Settings (bottom sheet)
- Audio Track · Subtitles (on/off, track, add SRT, size) · Playback Speed · Background Music ON/OFF
- Note shown: *"Music removal works only when a supported dialogue-focused track or processing is available."*
- If only mixed audio exists, the toggle reverts to ON and a message explains full removal may not be available.

## Step 3 – Gesture Controls (overlay)
- Left side swipe = brightness · Right side swipe = volume
- Double tap left/right = rewind/forward 10s · Horizontal swipe = seek
- Single tap = show/hide controls · Lock button = ignore touches
- Shown on first launch; tap to dismiss.
