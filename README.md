# ReactiveAudioRecord

[![Release](https://img.shields.io/badge/jCenter-1.0.1-brightgreen.svg)](https://bintray.com/sbrukhanda/maven/FragmentViewPager)
[![GitHub license](https://img.shields.io/badge/license-Apache%20Version%202.0-blue.svg)](https://github.com/sbrukhanda/fragmentviewpager/blob/master/LICENSE.txt)

A reactive (Rx) implementation of the AudioRecord API for recording raw (pcm) audio-data. Also includes helper methods for the creation of Wave audio files. 

###How to use it?
#####Create an instance of RecorderOnSubscribe giving it the path to the file
```java
final String filePath = Environment.getExternalStorageDirectory() + "/sample.wav"; //dummy file 
RecorderOnSubscribe recorder = new RecorderOnSubscribe.Builder(filePath)
                                                      .sampleRate(22000)       //by default it's 44100
                                                      .stereo()                //by default it's mono
                                                      .audioSourceCamcorder()  //by default it's MIC
                                                      .createSubscription();
```
#####Use the recorder OnSubscribe to create an observable
```java
Observable.create(recorder)
          .subscribe( shorts -> {
              recorder.writeShortsToFile(shorts); //helper method for writing shorts to file 
          });
```

####After setting up the Observer, manipulate the recording-process by using these methods
```java

recorder.start();

recorder.stop();

recorder.pause();

recorder.resume();

recorder.isRecording(); //returns a boolean 

recorder.isRecordingStopped(); //to check whether the recorder is in Stopped state

```
####Helper methods for wave file write operations
```java
recorder.writeShortsToFile(shorts); //write the short buffers to file

recorder.completeRecording(); //writes the wave header to file... Call it after the stop() method
```

And that's it! Check out the sample code for a working example!

##Download 
Repository available on jCenter

```Gradle
compile 'com.minimize.library:reactiveaudiorecord:1.0.1'
```
*If the dependency fails to resolve, add this to your project repositories*
```Gradle
repositories {
  maven {
      url  "http://dl.bintray.com/ahmedrizwan/maven" 
  }
}
```

##License 
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


