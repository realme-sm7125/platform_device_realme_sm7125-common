#!/system/bin/sh

# Disable HW overlays
service call SurfaceFlinger 1008 i32 1
