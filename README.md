# 3DSceneViewer

![Preview](https://i.imgur.com/n5aREP8.gif)
**3D Scene Viewer** is application for interacting with 3D scenes. It is created from scratch, without using any 3rd libraries.
Program includes whole 3D graphics rendering pipeline:
   - reading and processing vertices
   - transforming models from model space to world space
   - transforming models from world space to camera space
   - specifying clipping volume
   - mapping clipping volume to the 3D viewport
   - back-face culling
   - removing hidden surfaces and Z-buffering

