# Youtube DL Gui
This application is a simple GUI that wraps youtube-dlc and ffmpeg in order to download and convert youtube videos as mp4 or mp3.

It comes with a queue system and can also fetch whole playlists.

## Build
This is a maven project, you can use any IDE, I recommend Intellij IDEA for this though.

To build:
```
mvn clean install
```
## Usage
To run:
- Put the generated jar (in target/) in a folder of your choice
- Copy the dependencies found in target/package into the same folder
- Run the Java application from there:
  ```
  java -jar YoutubeDLGui-2.0.jar
  ```
  
## Dependencies
- youtube-dlc is available here: https://github.com/blackjack4494/yt-dlc
- ffmpeg is available here: https://ffmpeg.org/
