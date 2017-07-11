# ReactiveAudioRecord

[![Release](https://img.shields.io/badge/jCenter-1.0.2-brightgreen.svg)](https://bintray.com/sbrukhanda/maven/FragmentViewPager)
[![GitHub license](https://img.shields.io/badge/license-Apache%20Version%202.0-blue.svg)](https://github.com/sbrukhanda/fragmentviewpager/blob/master/LICENSE.txt)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-ReactiveAudioRecord-green.svg?style=flat)](https://android-arsenal.com/details/1/2084)

A reactive (Rx) implementation of the AudioRecord API for recording raw (pcm) audio-data. Also includes helper methods for the creation of Wave audio files. 

### How to use it?

##### Create an instance of RecorderOnSubscribe giving it the path to the file
```java
final String filePath = Environment.getExternalStorageDirectory() + "/sample.wav"; //dummy file 
RecorderOnSubscribe recorder = new RecorderOnSubscribe.Builder(filePath)
                                                      .sampleRate(22000)       //by default 44100
                                                      .stereo()                //by default mono
                                                      .audioSourceCamcorder()  //by default MIC
                                                      .build();
```
##### Use the recorder OnSubscribe to create an observable
```java
Observable.create(recorder)
          .subscribe( shorts -> {
              ...
              recorder.writeShortsToFile(shorts); //a helper method that writes the buffers to (wave) file
          });
```

#### After setting up the Observer, manipulate the recording-process by using these methods

| Name | Description |
|:----:|:-----------:|
| start() | Starts the recorder and moves it to *Recording* state |
| stop() | Stops the recorder and moves it to *Stopped* state |
| pause() | Pauses the recorder and moves it to *Paused* state |
| resume() | Resumes the recorder if it's in *Paused* state |
| isRecording() | Returns true if the recorder is in *Recording* state |
| isRecordingStopped() | Checks whether the recorder is in *Stopped* state or not |

#### Helper methods for wave file write operations

| Name | Description |
|:----:|:-----------:|
| writeShortsToFile(shorts) | Writes the short buffers to wave file |
| completeRecording() | Writes the Wave header info to the file (Call it after *stop()* method) |

And that's it! Check out the sample code for a working example!

## Download 
Repository available on jCenter

```Gradle
compile 'com.minimize.library:reactiveaudiorecord:1.0.2'
```

## License 
```
Copyright 2015 Ahmed Rizwan

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```


