# ReactiveAudioRecord
A reactive (RxAndroid) implementation of the AudioRecord for recording raw (pcm) audio-data.

#How to use it?
* Create an Observable from the RecorderOnSubscribe
```java
//using the retrolambda syntax
Observable.create(recorderOnSubscribe)
                .observeOn(Schedulers.newThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(shortBuffer -> {
                          //process the shortBuffers here
                        },
                        //on error 
                        e -> Log.e("Error", e.getMessage()),
                        () -> {
                          //on completion
                        });
```
* After setting up the Observer, start the recording-process for observing Mic-data
```java
recorderOnSubscribe.start(sampleRate, //for example 44100
                          bufferSize, //usually calculated from AudioRecord.getMinBufferSize
                          channel,    //Mono or Stereo
                          audioSource //MIC
                         );
```
* You can pause/resume/stop the recording-process by calling these methods
```java
recorderOnSubscribe.pause();
recorderOnSubscribe.resume();
recorderOnSubscribe.stop();
```
And that's it!
