# mis-2019-exercise-3b-opencv

The app detects the front face of the person in the camera and draws a blue rectangle around the face. It does this using the dront face haar cascade file.
The nose is detected by finding the approximate center of the face. I find the center of front face rectangle and then draw a circle with this as the center of the circle and radius as width/4 of the rectangle.
