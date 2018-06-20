# exoplayer-testing-samples

Samples to illustrate testing techniques for media playback on android using exoplayer

We use similar techniques to "test our integration" of Exoplayer.

The purpose is not to test Exoplayer itself, but rather, have we assembled the bits correctly.

They are very broad stroke tests, slow and painful.

This should help you abstract away Exoplayer letting you use a fast mock of this abstraction - Dont mock that which you dont own.

When pulling these together I accidentally assembled Exoplayer with a broken Bandwidth Meter resulting in ABR not working - lucky I had a test to illustrate it.

# The Tests

More for example and could do we a large amount of refinement, but they should get you going. I suspect these tests leak memory left right and center.

They are to be found in this folder ./app/src/androidTest/java and need to run on a device (or emulator).

A_JustWireMock
 - a wiremock bootstrap test to confirm your enviroment works, you can delete these once its green.

B_ExoplayerPreparesStream
 - asserts that Exoplayer can prepare playback of a dash stream.

C_ExoplayerStartsPlaybackOfAudio
 - uses an android.media.audiofx.Visualizer to capture a frequency chart and thus assert we are making some noise.
 - we use the global mixer as the capture device, so if some other noise is happening we get a false green test.
 - the volume also needs to be turned up!

D_ExoplayerStartsPlaybackOfVideo
 - uses a texture view and a video with just red or green pixels
 - samples the colour of pixels after a period of time to ensure its green

E_ABRsThroughRepresentations
 - roughly restricts the bandwidth of the network to match the bitrate of representation.
 - after about three segments it makes more bandwidth available
 - uses ChunkedDribbleDelay to achieve this
 - take a look in WireMockStaticFileFromRequestPathTransformerWithChunkedDelay


BinaryFileInAndroidAssetFolder and FileSourceAndroidAssetFolder are used to "serve" files from the android assets folder.
Its used in the wiremock rule configuration.

WireMockStaticFileFromRequestPathTransformer is used to map a url to a file on the disk, we need to do this to be able to apply stubbing rules to it such as ChunkedDribbleDelay.





The setup in these tests could be altered to verify an integration with a different player but keeping the same Player interface.

I'll leave that as an exercise for the reader.


# Stream Test Assets

Big Buck Bunny is available here https://peach.blender.org/

The audio50hz sample is kind to the ears

The red green video is all my own work :)


# TODO

Add contribution guidelines and review license. 