name := "play_glory"
 
version := "1.0" 
      
lazy val `play_glory` = (project in file(".")).enablePlugins(PlayJava)

      
scalaVersion := "2.11.11"

libraryDependencies ++= Seq( javaJdbc , cache , javaWs )
      