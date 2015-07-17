import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

trait Addable {
  def +(other: Addable): Addable {}
}

case class BaseInfo(
  src_id: String,
  own_entity: String,
  ifindex: String
)

case class SampleInfo(
  eventtime: String,
  num_samples: Int
)

case class BaseMeasure(
  ingt50: Int,
  ingt75: Int,
  ingt90: Int,
  outgt50: Int,
  outgt75: Int,
  outgt90: Int
) {
  def +(other: BaseMeasure) = BaseMeasure(this.ingt50+other.ingt50,
        this.ingt75+other.ingt75,
        this.ingt90+other.ingt90,
        this.outgt50+other.outgt50,
        this.outgt50+other.outgt50,
        this.outgt50+other.outgt50)
  
}

case class DetailMeasure(
  maxin:  Double,
  maxout: Double,
  pin:  Double,
  pout: Double,
  avgin: Double,
  avgout: Double,
  maxinerr: Double,
  maxouterr: Double,
  maxindis: Double,
  maxoutdis: Double,
  avginerr: Double,
  avgouterr: Double,
  avgindis: Double,
  avgoutdis: Double,
  maxinoctrate: Double,
  maxoutoctrate: Double,
  pinoctrate: Double,
  poutoctrate: Double,
  avginoctrate: Double,
  avgoutoctrate: Double
)



case class ifUtilRecord(
  baseinfo: BaseInfo,
  sampleinfo: SampleInfo,
  basemeasure: BaseMeasure,
  details: DetailMeasure,
  ifspeed: Double
)



object SimpleApp {
  def main(args: Array[String]) {
    val logFile = "hdfs://cheng-vpc-storage-0/user/root/ifUtil"
    val conf = new SparkConf().setAppName("kafkaWork")
    val sc = new SparkContext(conf)
    val logData = sc.textFile(logFile).map(_.split("\\|")).map(e => ifUtilRecord(BaseInfo(e(0),e(1),e(2)),SampleInfo(e(3),e(4).toInt),
        BaseMeasure(e(5).trim().toInt,e(6).trim().toInt,e(7).trim().toInt,e(8).trim().toInt, e(9).trim().toInt, e(10).trim().toInt),
        DetailMeasure(e(11).trim().toDouble,e(12).trim().toDouble,e(13).trim().toDouble,e(14).trim().toDouble,e(15).trim().toDouble,e(16).trim().toDouble,e(17).trim().toDouble,e(18).trim().toDouble
            ,e(19).trim().toDouble,e(20).trim().toDouble,e(21).trim().toDouble,e(22).trim().toDouble,e(23).trim().toDouble,e(24).trim().toDouble,e(25).trim().toDouble,e(26).trim().toDouble,e(27).trim().toDouble,e(28).trim().toDouble,e(29).trim().toDouble,e(30).trim().toDouble)
            ,e(31).trim().toDouble))
    
    val s1 = logData.filter(r => r match {
      case ifUtilRecord(BaseInfo(src,oe,ind),_,_,_,ip) => src.equals("1")
    })
    
    val groupById = logData.map(r => (r.baseinfo.own_entity,1)).reduceByKey((a,b) => a+b )
    
    val idgt50 = logData.map(r => if(r.basemeasure.ingt50 == 1) (r.baseinfo.own_entity,1) else (r.baseinfo.own_entity,0)).reduceByKey((a,b) => a+b).filter{case (a, b) => b>0}
    
    val timeBlock = logData.map(r => ((r.sampleinfo.eventtime.substring(0,10).toInt/3600+1)*3600,List((r.baseinfo.own_entity,r.basemeasure)))).reduceByKey((oe,bm)=> oe++bm)
    
    
    val timeWithIdBlock = logData.map(r => (((r.sampleinfo.eventtime.substring(0,10).toInt/3600+1)*3600,r.baseinfo.own_entity),(r.sampleinfo.num_samples,r.basemeasure))).reduceByKey{case ((s1,oe),(s2,bm)) => (s1+s2,oe+bm) }
    
    val filtered = timeWithIdBlock.filter{case (a,(sum,mb)) => mb.ingt50>0 }.map{ case (a,(sum,mb)) => (a, (mb.ingt50/sum.toDouble,mb.ingt75/sum.toDouble,mb.ingt90/sum.toDouble))}
    
  }
}