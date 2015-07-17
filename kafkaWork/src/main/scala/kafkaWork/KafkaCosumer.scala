package kafkaWork


import kafka.serializer.StringDecoder

import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.sql._
import org.apache.spark.sql.SQLContext

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path

import java.io._
import java.text.SimpleDateFormat
import java.util.Date

import common.schema._

object KafkaCosumer {
  
  @transient private var instance: SQLContext = null
  
  def getSQLContext(sparkContext: SparkContext): SQLContext = synchronized{
    if(instance == null){
      instance = new SQLContext(sparkContext)
    }
    instance
  }
  
  
  def objectSplitter(obj: String) : Array[Array[String]] = {
    if(obj.contains("\\n"))
      obj.split("\\n").drop(1).map(_.split("\\|"))
    else
      Array(obj.split("\\|"))
  }
  
  def createContext(jobMaster: String, appName: String, slice: Int, checkpointPath: String ) : StreamingContext = {
      val conf = new SparkConf().setMaster(jobMaster).setAppName(appName).
                      set("spark.driver.memory", "6g").
                      set("spark.executor.memory","6g").
                      //set("spark.default.parallelism", "4").
                      set("spark.serializer","org.apache.spark.serializer.KryoSerializer")
      val ssc = new StreamingContext(conf,Seconds(slice))
      ssc.checkpoint(checkpointPath)
      val kafkaParams = Map("metadata.broker.list" -> "cheng-vpc-1:9092,cheng-vpc-2:9092,cheng-vpc-3:9092",
                        "fetch.message.max.bytes" -> "104857600",
                        "group.id" -> "pi_ifutil_raw"
    )

   
    val topic_pi_ifutil_data = Set("pi_ifutil_data")
    val topic_pi_ifutil_raw = Set("pi_ifutil_raw_bkp")
    
//    val kafkaIfUtilDataStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topic_pi_ifutil_data)
    val kafkaIfUtilRawStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topic_pi_ifutil_raw)
    
    
//    val broadcastSdf =ssc.sparkContext.broadcast(new SimpleDateFormat("yyyy/MM/dd/hh/mm"))
//    val broadcastFormat = ssc.sparkContext.broadcast(Array("year","month","day","hour","minute"))
//    
//   kafkaIfUtilDataStream.foreachRDD(rdd => 
//        if(!rdd.isEmpty()){ 
//            val sqlContext = getSQLContext(rdd.sparkContext)
//            import sqlContext.implicits._
//          
//          val logData  = rdd.flatMap{case (host,doc) => 
//            doc.split("\\n").drop(1)}.
//            map(line => 
//              line.split("\\|")).
//              map(e => {
//                val parsedDate = broadcastFormat.value.zip(broadcastSdf.value.format(new Date(e(3).toLong)).split("\\/").map(_.trim.toInt))
//                (parsedDate.map{case (a,b) => a +"="+ b}.mkString("/"), 
//                    List(ifUtilRecord(
//                        parsedDate(0)._2,
//                        parsedDate(1)._2,
//                        parsedDate(2)._2,
//                        parsedDate(3)._2,
//                        parsedDate(4)._2,
//                        BaseInfo(e(0),e(1),e(2),e(31).trim().toDouble),
//                        SampleInfo(e(3).toLong,e(4).toInt),
//                        BaseMeasure(e(5).trim().toInt,
//                            e(6).trim().toInt,
//                            e(7).trim().toInt,
//                            e(8).trim().toInt, 
//                            e(9).trim().toInt, 
//                            e(10).trim().toInt),
//                        DetailMeasure(e(11).trim().toDouble,
//                            e(12).trim().toDouble,
//                            e(13).trim().toDouble,
//                            e(14).trim().toDouble,
//                            e(15).trim().toDouble,
//                            e(16).trim().toDouble,
//                            e(17).trim().toDouble,
//                            e(18).trim().toDouble,
//                            e(19).trim().toDouble,
//                            e(20).trim().toDouble,
//                            e(21).trim().toDouble,
//                            e(22).trim().toDouble,
//                            e(23).trim().toDouble,
//                            e(24).trim().toDouble,
//                            e(25).trim().toDouble,
//                            e(26).trim().toDouble,
//                            e(27).trim().toDouble,
//                            e(28).trim().toDouble,
//                            e(29).trim().toDouble,
//                            e(30).trim().toDouble
//                            )
//                         )
//                      )
//                )}
//                ).reduceByKey((a,b) => a:::b)
//           
//           logData.collect().foreach{
//            case (path, records) =>
//              println("ifutilData " + path)
//              records.toDF().saveAsParquetFile("hdfs://cheng-vpc-storage-0/user/hadoop/ifUtildata/"+path+s"/data-${java.lang.System.currentTimeMillis()}.parquet")
//          }
//        
////          val pw = new PrintWriter(new File("/home/hadoop/result.txt"))
////          pw.write(logDataFrame.count().toString())
////          pw.close()
////          
////          
////          logDataFrame.saveAsParquetFile(s"hdfs://cheng-vpc-storage-0/user/hadoop/ifUtildata/data-${java.lang.System.currentTimeMillis()}.parquet")
//////          val timeWithIdBlock = logData.map(r => (((r.sampleinfo.eventtime.substring(0,10).toInt/3600+1)*3600,r.baseinfo),(r.sampleinfo.num_samples,r.basemeasure))).reduceByKey{case ((s1,oe),(s2,bm)) => (s1+s2,oe+bm) }
//////      
////          val filtered = timeWithIdBlock.filter{case (a,(sum,mb)) => mb.ingt50>0 }.map{ case (a,(sum,mb)) => (a, (mb.ingt50/sum.toDouble,mb.ingt75/sum.toDouble,mb.ingt90/sum.toDouble))}
////      
////          var result = ""
////          filtered.collect().foreach(rec => {result += rec+"\n"; println(rec)})
//
//         
//        }
//        else println("EMPTY if_util_data queue") 
//      )
//    
    val minAggregatedIfUtilRawStream = kafkaIfUtilRawStream.transform(rdd => {
        rdd.map{case (host, msg) => msg.split("\\|") }
      }.map(e =>{
                    val parsedDate = Array("year","month","day","hour","minute").zip(new SimpleDateFormat("yyyy/MM/dd/hh/mm").format(new Date(e(3).toLong)).split("\\/").map(_.trim.toInt))
                    (parsedDate.map{case (a,b) => a +"="+ b}.mkString("/"), 
                        List(ifUtilRawRecord(
                            parsedDate(0)._2,
                            parsedDate(1)._2,
                            parsedDate(2)._2,
                            parsedDate(3)._2,
                            parsedDate(4)._2,
                            BaseInfo4Raw(e(0),e(1),e(2),e(4),e(5).trim.toDouble),
                            DetailMeasure4Raw(e(3).trim.toLong,
                                e(6).trim.toDouble,
                                e(7).trim.toDouble,
                                e(8).trim.toInt,
                                e(9).trim.toInt,
                                e(10).trim.toInt,
                                e(11).trim.toInt,
                                e(12).trim.toLong,
                                e(13).trim.toLong)
                             ))
                        )
                     }
    )).reduceByKey((a,b) => a:::b)
    
    minAggregatedIfUtilRawStream.foreachRDD(rdd => {
        val sqlContext = getSQLContext(rdd.sparkContext)
        import sqlContext.implicits._
          rdd.collect.foreach{ case (path, records) => 
            if(!records.isEmpty)
                
                //records.toDF().saveAsParquetFile("hdfs://cheng-vpc-storage-0/user/hadoop/ifUtilRaw/"+path+s"/raw-${java.lang.System.currentTimeMillis()}.parquet")
                println( "STEAMING processing " + records.size)
          }})
          
//    minAggregatedIfUtilRawStream.count().print();

//      
//      
//    kafkaIfUtilRawStream.foreachRDD(rdd => 
//        if(!rdd.isEmpty()){
//          val sqlContext = getSQLContext(rdd.sparkContext)
//            import sqlContext.implicits._
//            val logData = rdd.flatMap{case (host,doc) => 
//              doc.split("\\n").drop(1)}.
//              map(line => line.split("\\|")).map(
//                  e =>{
//                    val parsedDate = broadcastFormat.value.zip(broadcastSdf.value.format(new Date(e(3).toLong)).split("\\/").map(_.trim.toInt))
//                    (parsedDate.map{case (a,b) => a +"="+ b}.mkString("/"), 
//                        List(ifUtilRawRecord(
//                            parsedDate(0)._2,
//                            parsedDate(1)._2,
//                            parsedDate(2)._2,
//                            parsedDate(3)._2,
//                            BaseInfo4Raw(e(0),e(1),e(2),e(4),e(5).trim.toDouble),
//                            DetailMeasure4Raw(e(3).trim.toLong,
//                                e(6).trim.toDouble,
//                                e(7).trim.toDouble,
//                                e(8).trim.toInt,
//                                e(9).trim.toInt,
//                                e(10).trim.toInt,
//                                e(11).trim.toInt,
//                                e(12).trim.toLong,
//                                e(13).trim.toLong)
//                             ))
//                        )}
//                  ).reduceByKey((a,b) => a:::b)
//            
//            logData.collect().foreach{
//              case (path, records) =>
//               println("ifutilRaw " + path)
//              records.toDF().saveAsParquetFile("hdfs://cheng-vpc-storage-0/user/hadoop/ifUtilRaw/"+path+s"/raw-${java.lang.System.currentTimeMillis()}.parquet")
//          }
//        }
//        else println("EMPTY if_util_raw queue")
//     )
     ssc
  }
  
  def main(args: Array[String]){
    val jobMaster ="spark://cheng-vpc-spark-0:7077"
    val appName = "kafka-consumer-pi-ifutil"
    val checkpointPath = "hdfs://cheng-vpc-storage-0:8020/user/hadoop/ssc_checkpoint"
    
    val ssc = StreamingContext.getOrCreate(checkpointPath, 
        () => 
          createContext(jobMaster,appName,20,checkpointPath)
          )
  
    ssc.start()
    ssc.awaitTermination()
    
  }
    
  
  
  
}