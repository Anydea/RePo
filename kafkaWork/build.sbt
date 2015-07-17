name := "kafkaWork"

version := "1.0"

scalaVersion := "2.10.5"

javacOptions += "-g:none"

javacOptions ++= Seq("-source","1.7")

compileOrder := CompileOrder.JavaThenScala


resolvers += "Apache HBase" at "https://repository.apache.org/content/repositories/releases"

libraryDependencies ++= Seq(
		"org.apache.spark" %% "spark-core" % "1.3.1"  %"provided",
		"org.apache.spark" %% "spark-streaming" % "1.3.1"  %"provided",
		"org.apache.spark" %% "spark-sql" % "1.3.1" %"provided",
		("org.apache.hadoop" % "hadoop-common" % "2.2.0"),
		"com.101tec" % "zkclient" % "0.3",
		"com.yammer.metrics" % "metrics-core" % "2.2.0",
		"com.yammer.metrics" % "metrics-annotation" % "2.2.0",
   		("org.apache.spark" %% "spark-streaming-kafka" % "1.3.1").exclude("org.apache.spark", "spark-core_2.10").
   			exclude("org.apache.spark", "spark-streaming_2.10").
   			exclude("org.apache.kafka","kafka_2.10").
   			exclude("org.spark-project.spark","unused"),
   		"org.apache.kafka" % "kafka_2.10" % "0.8.1.1"
    		exclude("javax.jms", "jms")
    		exclude("com.sun.jdmk", "jmxtools")
    		exclude("com.sun.jmx", "jmxri")
    		exclude("org.slf4j", "slf4j-simple")
    		exclude("log4j", "log4j")
    		exclude("org.apache.zookeeper", "zookeeper")
    		exclude("com.101tec", "zkclient")
)
javaOptions ++= Seq(
  "-Xms2g",
  "-Xmx2g",
  "-XX:+UseG1GC",
  "-XX:MaxGCPauseMillis=20",
  "-XX:InitiatingHeapOccupancyPercent=35",
  "-Djava.awt.headless=true",
  "-Djava.net.preferIPv4Stack=true")

val meta = """META.INF(.)*""".r

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
    case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
    case PathList("org", "apache", xs @ _*) => MergeStrategy.last
    case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
    case PathList("plugin.properties") => MergeStrategy.last
    case meta(_) => MergeStrategy.discard
    case x => old(x)
  }
}


